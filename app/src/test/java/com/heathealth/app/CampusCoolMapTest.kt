package com.heathealth.app

import com.heathealth.app.data.model.CampusFacility
import com.heathealth.app.data.model.College
import com.heathealth.app.data.model.DataSource
import com.heathealth.app.data.model.FacilityCategory
import com.heathealth.app.data.repository.CampusCoolMapRepository
import com.heathealth.app.network.HeatHealthApiService
import com.heathealth.app.network.dto.AddFacilityRequestDto
import com.heathealth.app.network.dto.CampusFacilitiesResponseDto
import com.heathealth.app.network.dto.CampusFacilityDto
import com.heathealth.app.network.dto.CollegeDto
import com.heathealth.app.network.dto.CollegesResponseDto
import com.heathealth.app.network.dto.HeatRiskResponseDto
import com.heathealth.app.network.dto.PredictRequestDto
import com.heathealth.app.network.dto.PredictResponseDto
import com.heathealth.app.network.dto.ReportStatusRequestDto
import com.heathealth.app.network.dto.WeatherResponseDto
import com.heathealth.app.viewmodel.CampusCoolMapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CampusCoolMapTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleColleges = listOf(
        College("col_iisc", "Indian Institute of Science (IISc)", "Bengaluru", "CV Raman Rd", 12.9716, 77.5946, true),
        College("col_du", "Delhi University (DU)", "New Delhi", "University Enclave", 28.6139, 77.2090, true)
    )

    private val sampleFacilities = listOf(
        CampusFacility(
            id = "fac_w1",
            collegeId = "col_iisc",
            name = "Central Library Chilled Water Station",
            category = FacilityCategory.DRINKING_WATER,
            description = "RO purified chilled water fountain.",
            latitude = 12.9720,
            longitude = 77.5950,
            buildingLocation = "Central Library Entrance",
            isVerified = true
        ),
        CampusFacility(
            id = "fac_s1",
            collegeId = "col_iisc",
            name = "Main Quadrangle Shade",
            category = FacilityCategory.SHADED_AREA,
            description = "Canopy shade pergola.",
            latitude = 12.9712,
            longitude = 77.5942,
            buildingLocation = "Academic Quadrangle",
            isVerified = true
        ),
        CampusFacility(
            id = "fac_w2",
            collegeId = "col_du",
            name = "DU Arts Faculty Water Point",
            category = FacilityCategory.DRINKING_WATER,
            description = "Chilled water dispenser.",
            latitude = 28.6142,
            longitude = 77.2095,
            buildingLocation = "Arts Faculty Entrance",
            isVerified = true
        )
    )

    @Test
    fun testCollegeSearchAndFiltering() {
        val query = "Bengaluru"
        val matches = sampleColleges.filter { it.city.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true) }
        assertEquals(1, matches.size)
        assertEquals("Indian Institute of Science (IISc)", matches.first().name)
    }

    @Test
    fun testFacilityIsolationByCollegeId() {
        val iiscFacilities = sampleFacilities.filter { it.collegeId == "col_iisc" }
        assertEquals(2, iiscFacilities.size)

        val duFacilities = sampleFacilities.filter { it.collegeId == "col_du" }
        assertEquals(1, duFacilities.size)
        assertEquals("DU Arts Faculty Water Point", duFacilities.first().name)
    }

    @Test
    fun testFacilityCategoryMapping() {
        assertEquals(FacilityCategory.DRINKING_WATER, FacilityCategory.fromString("Drinking Water"))
        assertEquals(FacilityCategory.SHADED_AREA, FacilityCategory.fromString("Shaded Area"))
        assertEquals(FacilityCategory.COOL_INDOOR_SPACE, FacilityCategory.fromString("Cool Indoor Space"))
        assertEquals(FacilityCategory.FIRST_AID, FacilityCategory.fromString("First Aid"))
    }

    @Test
    fun testSearchByNameAndLocation() {
        val nameQuery = "Library"
        val nameMatches = sampleFacilities.filter {
            it.name.contains(nameQuery, ignoreCase = true) || it.buildingLocation.contains(nameQuery, ignoreCase = true)
        }
        assertEquals(1, nameMatches.size)
    }

    @Test
    fun testEntityConversionAndDuplicatePrevention() {
        val entity1 = sampleFacilities.first().toEntity()
        val converted = CampusFacility.fromEntity(entity1)

        assertEquals(sampleFacilities.first().id, converted.id)
        assertEquals(sampleFacilities.first().name, converted.name)
        assertEquals(sampleFacilities.first().category, converted.category)

        val duplicateList = listOf(entity1, entity1.copy(name = "Updated Station Name"))
        val map = duplicateList.associateBy { it.id }
        assertEquals(1, map.size)
        assertEquals("Updated Station Name", map[entity1.id]?.name)
    }

    @Test
    fun testOfflineFallbackToCachedFacilities() = runBlocking {
        val mockApi = object : HeatHealthApiService {
            override suspend fun getHealth(): Map<String, Any> = emptyMap()
            override suspend fun predictHeatRisk(request: PredictRequestDto): PredictResponseDto = throw UnsupportedOperationException()
            override suspend fun getWeather(latitude: Double, longitude: Double, label: String?): WeatherResponseDto = throw UnsupportedOperationException()
            override suspend fun getHeatRisk(latitude: Double, longitude: Double, label: String?): HeatRiskResponseDto = throw UnsupportedOperationException()
            override suspend fun getColleges(query: String?, city: String?): CollegesResponseDto = CollegesResponseDto()
            override suspend fun getCampusFacilities(collegeId: String?, category: String?): CampusFacilitiesResponseDto {
                throw IOException("Campus server offline")
            }
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

        val repository = CampusCoolMapRepository(apiService = mockApi, enableCandidateFailover = false)
        val viewModel = CampusCoolMapViewModel(repository = repository, autoLoad = false)

        viewModel.loadFacilities()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DataSource.UNAVAILABLE, state.dataSource)
        assertNotNull(state.errorMessage)
    }
}
