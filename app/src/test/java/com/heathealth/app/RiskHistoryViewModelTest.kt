package com.heathealth.app

import com.heathealth.app.data.local.dao.HeatRiskLogDao
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.network.dto.HeatRiskResponseDto
import com.heathealth.app.network.dto.LocationDto
import com.heathealth.app.network.dto.MetadataDto
import com.heathealth.app.network.dto.RiskDto
import com.heathealth.app.network.dto.WeatherDto
import com.heathealth.app.viewmodel.RiskHistoryViewModel
import com.heathealth.app.viewmodel.TimePeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RiskHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class InMemoryHeatRiskLogDao : HeatRiskLogDao {
        val logs = mutableListOf<HeatRiskLogEntity>()
        val flow = MutableStateFlow<List<HeatRiskLogEntity>>(emptyList())

        override suspend fun insertLog(log: HeatRiskLogEntity): Long {
            val newId = (logs.size + 1).toLong()
            val inserted = log.copy(id = newId)
            logs.add(0, inserted)
            flow.value = logs.toList()
            return newId
        }

        override fun getRecentLogs(limit: Int): Flow<List<HeatRiskLogEntity>> {
            return flow.map { list -> list.sortedByDescending { it.timestamp }.take(limit) }
        }

        override fun getAllLogs(): Flow<List<HeatRiskLogEntity>> {
            return flow.map { list -> list.sortedByDescending { it.timestamp } }
        }

        override fun getLogsSince(minTimestamp: Long): Flow<List<HeatRiskLogEntity>> {
            return flow.map { list -> list.filter { it.timestamp >= minTimestamp }.sortedByDescending { it.timestamp } }
        }

        override suspend fun getLatestLog(): HeatRiskLogEntity? {
            return logs.maxByOrNull { it.timestamp }
        }

        override suspend fun deleteLogById(id: Long) {
            logs.removeAll { it.id == id }
            flow.value = logs.toList()
        }

        override suspend fun clearAllLogs() {
            logs.clear()
            flow.value = emptyList()
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSavingValidPredictionRecordAndRetrieval() = runTest {
        val mockDao = InMemoryHeatRiskLogDao()
        val repository = HeatHealthRepository(
            heatRiskLogDao = mockDao,
            enableCandidateFailover = false
        )

        val log = HeatRiskLogEntity(
            timestamp = System.currentTimeMillis(),
            isoTimestamp = "2025-05-15T12:00:00Z",
            riskLevel = "High",
            riskScore = 38.5,
            temperatureCelsius = 35,
            humidityPercent = 60,
            feelsLikeCelsius = 38,
            locationLabel = "IISc Campus, Bengaluru"
        )

        mockDao.insertLog(log)

        val retrievedLogs = repository.getAllRiskLogs().first()
        assertEquals(1, retrievedLogs.size)
        assertEquals("High", retrievedLogs[0].riskLevel)
        assertEquals("IISc Campus, Bengaluru", retrievedLogs[0].locationLabel)
    }

    @Test
    fun testFilteringRecordsByDateRange() = runTest {
        val mockDao = InMemoryHeatRiskLogDao()
        val now = System.currentTimeMillis()

        // 1. Log from 2 days ago (within 7 days)
        mockDao.insertLog(
            HeatRiskLogEntity(
                timestamp = now - (2 * 86400 * 1000L),
                isoTimestamp = "2025-05-13T12:00:00Z",
                riskLevel = "Moderate",
                riskScore = 31.0,
                temperatureCelsius = 30,
                humidityPercent = 50,
                feelsLikeCelsius = 31,
                locationLabel = "Campus A"
            )
        )

        // 2. Log from 15 days ago (outside 7 days, within 30 days)
        mockDao.insertLog(
            HeatRiskLogEntity(
                timestamp = now - (15 * 86400 * 1000L),
                isoTimestamp = "2025-04-30T12:00:00Z",
                riskLevel = "High",
                riskScore = 38.0,
                temperatureCelsius = 35,
                humidityPercent = 60,
                feelsLikeCelsius = 38,
                locationLabel = "Campus A"
            )
        )

        val repository = HeatHealthRepository(heatRiskLogDao = mockDao, enableCandidateFailover = false)

        val logs7Days = repository.getRiskLogsSince(now - TimePeriod.LAST_7_DAYS.durationMs).first()
        assertEquals(1, logs7Days.size)
        assertEquals("Moderate", logs7Days[0].riskLevel)

        val logs30Days = repository.getRiskLogsSince(now - TimePeriod.LAST_30_DAYS.durationMs).first()
        assertEquals(2, logs30Days.size)
    }

    @Test
    fun testRiskCategoryDistributionCalculation() {
        val now = System.currentTimeMillis()
        val logs = listOf(
            HeatRiskLogEntity(timestamp = now, isoTimestamp = "", riskLevel = "High", riskScore = 38.0, temperatureCelsius = 35, humidityPercent = 60, feelsLikeCelsius = 38, locationLabel = "A"),
            HeatRiskLogEntity(timestamp = now - 1000, isoTimestamp = "", riskLevel = "Extreme", riskScore = 42.0, temperatureCelsius = 40, humidityPercent = 65, feelsLikeCelsius = 44, locationLabel = "A"),
            HeatRiskLogEntity(timestamp = now - 2000, isoTimestamp = "", riskLevel = "Low", riskScore = 24.0, temperatureCelsius = 22, humidityPercent = 40, feelsLikeCelsius = 23, locationLabel = "A")
        )

        val summary = RiskHistoryViewModel.calculateTrendsSummary(logs, TimePeriod.LAST_7_DAYS)

        assertEquals(3, summary.totalPredictions)
        assertEquals(RiskLevel.EXTREME, summary.highestRiskCategory)
        assertEquals(1, summary.riskDistribution[RiskLevel.LOW])
        assertEquals(1, summary.riskDistribution[RiskLevel.HIGH])
        assertEquals(1, summary.riskDistribution[RiskLevel.EXTREME])
        assertEquals(0, summary.riskDistribution[RiskLevel.MODERATE])
    }

    @Test
    fun testStudentInsightsGenerationFromRealLogs() {
        val now = System.currentTimeMillis()
        val logsEscalated = listOf(
            HeatRiskLogEntity(timestamp = now, isoTimestamp = "", riskLevel = "Extreme", riskScore = 42.0, temperatureCelsius = 40, humidityPercent = 65, feelsLikeCelsius = 44, locationLabel = "A"),
            HeatRiskLogEntity(timestamp = now - 3600000, isoTimestamp = "", riskLevel = "High", riskScore = 38.0, temperatureCelsius = 35, humidityPercent = 60, feelsLikeCelsius = 38, locationLabel = "A")
        )

        val summary = RiskHistoryViewModel.calculateTrendsSummary(logsEscalated, TimePeriod.LAST_7_DAYS)

        assertTrue(summary.insights.any { it.contains("escalated", ignoreCase = true) })
        assertTrue(summary.insights.any { it.contains("recorded 2 high or extreme-risk predictions", ignoreCase = true) })
    }

    @Test
    fun testEmptyHistoryHandlingAndNoFabricatedPredictions() {
        val emptySummary = RiskHistoryViewModel.calculateTrendsSummary(emptyList(), TimePeriod.LAST_7_DAYS)

        assertEquals(0, emptySummary.totalPredictions)
        assertNull(emptySummary.highestRiskCategory)
        assertNull(emptySummary.averageTemperature)
        assertNull(emptySummary.averageFeelsLike)
        assertTrue(emptySummary.insights.first().contains("No heat-risk predictions recorded", ignoreCase = true))
    }

    @Test
    fun testDuplicateInsertionPreventionWithin60Seconds() = runTest {
        val mockDao = InMemoryHeatRiskLogDao()
        val dummyResponse = HeatRiskResponseDto(
            location = LocationDto(28.6139, 77.2090, "IISc Campus"),
            weather = WeatherDto(temperature = 35.0, relativeHumidity = 60.0, apparentTemperature = 38.0, timestamp = "2025-05-15T12:00:00Z", source = "Mock", isForecast = false),
            hourlyForecast = emptyList(),
            risk = RiskDto(level = "High", score = 38.0, scoreInterpretation = "High Risk", method = "Baseline Rule Engine"),
            factors = emptyList(),
            recommendations = emptyList(),
            metadata = MetadataDto(rulesVersion = "1.0", modelVersion = "1.0", predictionTimestamp = "2025-05-15T12:00:00Z", dataQuality = "Good")
        )

        val mockApi = object : com.heathealth.app.network.HeatHealthApiService {
            override suspend fun getHealth(): Map<String, Any> = emptyMap()
            override suspend fun predictHeatRisk(request: com.heathealth.app.network.dto.PredictRequestDto): com.heathealth.app.network.dto.PredictResponseDto = throw UnsupportedOperationException()
            override suspend fun getWeather(latitude: Double, longitude: Double, label: String?): com.heathealth.app.network.dto.WeatherResponseDto = throw UnsupportedOperationException()
            override suspend fun getHeatRisk(latitude: Double, longitude: Double, label: String?): HeatRiskResponseDto = dummyResponse
            override suspend fun getColleges(query: String?, city: String?): com.heathealth.app.network.dto.CollegesResponseDto = throw UnsupportedOperationException()
            override suspend fun getCampusFacilities(collegeId: String?, category: String?): com.heathealth.app.network.dto.CampusFacilitiesResponseDto = throw UnsupportedOperationException()
            override suspend fun addCampusFacility(request: com.heathealth.app.network.dto.AddFacilityRequestDto): com.heathealth.app.network.dto.CampusFacilityDto = throw UnsupportedOperationException()
            override suspend fun updateFacilityStatus(facilityId: String, request: com.heathealth.app.network.dto.ReportStatusRequestDto): com.heathealth.app.network.dto.CampusFacilityDto = throw UnsupportedOperationException()
            override suspend fun getAlertHistory(limit: Int, offset: Int): com.heathealth.app.network.dto.AlertHistoryResponseDto = throw UnsupportedOperationException()
            override suspend fun createAlertHistory(request: com.heathealth.app.network.dto.CreateAlertRequestDto): com.heathealth.app.network.dto.AlertLogDto = throw UnsupportedOperationException()
            override suspend fun clearAlertHistory(): Map<String, Any> = throw UnsupportedOperationException()
            override suspend fun getAlertPreferences(): com.heathealth.app.network.dto.AlertPreferencesDto = throw UnsupportedOperationException()
            override suspend fun updateAlertPreferences(preferences: com.heathealth.app.network.dto.AlertPreferencesDto): com.heathealth.app.network.dto.AlertPreferencesDto = throw UnsupportedOperationException()
            override suspend fun evaluateAlert(request: com.heathealth.app.network.dto.AlertEvaluationRequestDto): com.heathealth.app.network.dto.AlertEvaluationResponseDto = throw UnsupportedOperationException()
            override suspend fun getUpcomingForecast(latitude: Double, longitude: Double, label: String?): com.heathealth.app.network.dto.UpcomingForecastResponseDto = throw UnsupportedOperationException()
            override suspend fun registerDeviceToken(request: com.heathealth.app.network.dto.DeviceTokenRequestDto): com.heathealth.app.network.dto.DeviceTokenResponseDto = throw UnsupportedOperationException()
        }

        val repository = HeatHealthRepository(
            apiService = mockApi,
            heatRiskLogDao = mockDao,
            enableCandidateFailover = false
        )

        // Call fetchHeatRiskData twice in rapid succession
        repository.fetchHeatRiskData(28.6139, 77.2090, "IISc Campus")
        repository.fetchHeatRiskData(28.6139, 77.2090, "IISc Campus")

        val storedLogs = mockDao.getAllLogs().first()
        assertEquals("Duplicate log within 60 seconds MUST be prevented", 1, storedLogs.size)
    }
}
