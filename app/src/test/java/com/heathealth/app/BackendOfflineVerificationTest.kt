package com.heathealth.app

import com.heathealth.app.data.model.DataSource
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.network.HeatHealthApiService
import com.heathealth.app.network.dto.AddFacilityRequestDto
import com.heathealth.app.network.dto.CampusFacilitiesResponseDto
import com.heathealth.app.network.dto.CampusFacilityDto
import com.heathealth.app.network.dto.CollegesResponseDto
import com.heathealth.app.network.dto.HeatRiskResponseDto
import com.heathealth.app.network.dto.HourlyWeatherDto
import com.heathealth.app.network.dto.LocationDto
import com.heathealth.app.network.dto.MetadataDto
import com.heathealth.app.network.dto.PredictRequestDto
import com.heathealth.app.network.dto.PredictResponseDto
import com.heathealth.app.network.dto.RecommendationDto
import com.heathealth.app.network.dto.ReportStatusRequestDto
import com.heathealth.app.network.dto.RiskDto
import com.heathealth.app.network.dto.WeatherDto
import com.heathealth.app.network.dto.WeatherResponseDto
import com.heathealth.app.viewmodel.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BackendOfflineVerificationTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class ControllableMockApiService : HeatHealthApiService {
        var isOnline: Boolean = true
        var mockTimestamp: String = "2025-05-15T10:00:00Z"
        var mockTemperature: Double = 35.0

        override suspend fun getHealth(): Map<String, Any> = emptyMap()

        override suspend fun predictHeatRisk(request: PredictRequestDto): PredictResponseDto = throw UnsupportedOperationException()

        override suspend fun getWeather(latitude: Double, longitude: Double, label: String?): WeatherResponseDto = throw UnsupportedOperationException()

        override suspend fun getHeatRisk(latitude: Double, longitude: Double, label: String?): HeatRiskResponseDto {
            if (!isOnline) {
                throw IOException("Unable to connect to FastAPI backend at http://127.0.0.1:8000. Backend offline.")
            }

            return HeatRiskResponseDto(
                location = LocationDto(latitude, longitude, label ?: "New Delhi"),
                weather = WeatherDto(
                    temperature = mockTemperature,
                    relativeHumidity = 60.0,
                    windSpeed = 10.0,
                    apparentTemperature = mockTemperature + 3.0,
                    timestamp = mockTimestamp,
                    source = "Open-Meteo API",
                    isForecast = false
                ),
                hourlyForecast = listOf(
                    HourlyWeatherDto("12:00", mockTemperature, 60.0, mockTemperature + 3.0, 10.0, "High", 38.0)
                ),
                risk = RiskDto("High", 38.0, "High thermal risk", "Baseline Rule Engine"),
                factors = listOf("Elevated ambient temperature"),
                recommendations = listOf(
                    RecommendationDto("1", "Drink water", "Stay hydrated", "Hydration")
                ),
                metadata = MetadataDto("1.0.0", "rf-1.0", mockTimestamp, "Live observation")
            )
        }

        override suspend fun getColleges(query: String?, city: String?): CollegesResponseDto = throw UnsupportedOperationException()

        override suspend fun getCampusFacilities(collegeId: String?, category: String?): CampusFacilitiesResponseDto = throw UnsupportedOperationException()

        override suspend fun addCampusFacility(request: AddFacilityRequestDto): CampusFacilityDto = throw UnsupportedOperationException()

        override suspend fun updateFacilityStatus(facilityId: String, request: ReportStatusRequestDto): CampusFacilityDto = throw UnsupportedOperationException()

        override suspend fun getAlertHistory(limit: Int, offset: Int): com.heathealth.app.network.dto.AlertHistoryResponseDto = com.heathealth.app.network.dto.AlertHistoryResponseDto()

        override suspend fun createAlertHistory(request: com.heathealth.app.network.dto.CreateAlertRequestDto): com.heathealth.app.network.dto.AlertLogDto = throw UnsupportedOperationException()

        override suspend fun clearAlertHistory(): Map<String, Any> = emptyMap()

        override suspend fun getAlertPreferences(): com.heathealth.app.network.dto.AlertPreferencesDto = com.heathealth.app.network.dto.AlertPreferencesDto()

        override suspend fun updateAlertPreferences(preferences: com.heathealth.app.network.dto.AlertPreferencesDto): com.heathealth.app.network.dto.AlertPreferencesDto = preferences

        override suspend fun evaluateAlert(request: com.heathealth.app.network.dto.AlertEvaluationRequestDto): com.heathealth.app.network.dto.AlertEvaluationResponseDto = throw UnsupportedOperationException()

        override suspend fun getUpcomingForecast(latitude: Double, longitude: Double, label: String?): com.heathealth.app.network.dto.UpcomingForecastResponseDto = com.heathealth.app.network.dto.UpcomingForecastResponseDto(true, label ?: "", emptyList(), "")

        override suspend fun registerDeviceToken(request: com.heathealth.app.network.dto.DeviceTokenRequestDto): com.heathealth.app.network.dto.DeviceTokenResponseDto = com.heathealth.app.network.dto.DeviceTokenResponseDto("success", "registered")
    }

    @Test
    fun test9StepBackendOfflineAndRecoverySequence() {
        val mockApi = ControllableMockApiService().apply {
            isOnline = true
            mockTimestamp = "2025-05-15T10:00:00Z"
            mockTemperature = 35.0
        }
        val repository = HeatHealthRepository(apiService = mockApi, enableCandidateFailover = false)
        val viewModel = DashboardViewModel(repository = repository, autoLoad = false)

        viewModel.loadData(isRefresh = false)
        testDispatcher.scheduler.advanceUntilIdle()

        val liveState = viewModel.uiState.value
        assertEquals(DataSource.LIVE, liveState.dataSource)
        assertEquals("2025-05-15T10:00:00Z", liveState.lastUpdated)
        assertEquals(35, liveState.weatherData.temperatureCelsius)

        mockApi.isOnline = false

        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()

        val cachedState = viewModel.uiState.value
        assertEquals(DataSource.CACHED, cachedState.dataSource)
        assertEquals("2025-05-15T10:00:00Z", cachedState.lastUpdated)
        assertNotNull(cachedState.errorMessage)
        assertTrue(cachedState.errorMessage?.contains("Unable to connect") == true)
        assertEquals(35, cachedState.weatherData.temperatureCelsius)

        mockApi.isOnline = true
        mockApi.mockTimestamp = "2025-05-15T11:00:00Z"
        mockApi.mockTemperature = 37.0

        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()

        val restoredState = viewModel.uiState.value
        assertEquals(DataSource.LIVE, restoredState.dataSource)
        assertEquals("2025-05-15T11:00:00Z", restoredState.lastUpdated)
        assertEquals(37, restoredState.weatherData.temperatureCelsius)
        assertNotEquals("2025-05-15T10:00:00Z", restoredState.lastUpdated)
    }

    @Test
    fun testInitialBackendOfflineShowsUnavailableErrorState() {
        val mockApi = ControllableMockApiService().apply { isOnline = false }
        val repository = HeatHealthRepository(apiService = mockApi, enableCandidateFailover = false)
        val viewModel = DashboardViewModel(repository = repository, autoLoad = false)

        viewModel.loadData()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DataSource.UNAVAILABLE, state.dataSource)
        assertNotNull(state.errorMessage)
    }
}
