package com.heathealth.app

import com.google.gson.JsonSyntaxException
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.network.HeatHealthApiService
import com.heathealth.app.network.dto.AddFacilityRequestDto
import com.heathealth.app.network.dto.CampusFacilitiesResponseDto
import com.heathealth.app.network.dto.CampusFacilityDto
import com.heathealth.app.network.dto.CollegesResponseDto
import com.heathealth.app.network.dto.HeatRiskResponseDto
import com.heathealth.app.network.dto.PredictRequestDto
import com.heathealth.app.network.dto.PredictResponseDto
import com.heathealth.app.network.dto.ReportStatusRequestDto
import com.heathealth.app.network.dto.WeatherResponseDto
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkReliabilityAndErrorHandlingTest {

    @Test
    fun testHttp422CoordinateValidationErrorHandling() = runBlocking {
        val mockApi = object : HeatHealthApiService {
            override suspend fun getHealth(): Map<String, Any> = emptyMap()
            override suspend fun predictHeatRisk(request: PredictRequestDto): PredictResponseDto = throw UnsupportedOperationException()
            override suspend fun getWeather(latitude: Double, longitude: Double, label: String?): WeatherResponseDto = throw UnsupportedOperationException()
            override suspend fun getHeatRisk(latitude: Double, longitude: Double, label: String?): HeatRiskResponseDto {
                val errorBody = ResponseBody.create(null, "{\"detail\":\"Latitude 95.0 out of valid range [-90, 90].\"}")
                throw HttpException(Response.error<HeatRiskResponseDto>(422, errorBody))
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

        val repository = HeatHealthRepository(apiService = mockApi)
        val result = repository.fetchHeatRiskData(95.0, 77.0, "Invalid Lat")

        assertTrue("Result should be failure on 422 HTTP validation error", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be IllegalArgumentException", exception is IllegalArgumentException)
        assertTrue("Error message must mention invalid coordinates", exception?.message?.contains("Invalid input coordinates") == true)
    }

    @Test
    fun testHttp502ServerErrorHandling() = runBlocking {
        val mockApi = object : HeatHealthApiService {
            override suspend fun getHealth(): Map<String, Any> = emptyMap()
            override suspend fun predictHeatRisk(request: PredictRequestDto): PredictResponseDto = throw UnsupportedOperationException()
            override suspend fun getWeather(latitude: Double, longitude: Double, label: String?): WeatherResponseDto = throw UnsupportedOperationException()
            override suspend fun getHeatRisk(latitude: Double, longitude: Double, label: String?): HeatRiskResponseDto {
                val errorBody = ResponseBody.create(null, "{\"detail\":\"Weather provider gateway error\"}")
                throw HttpException(Response.error<HeatRiskResponseDto>(502, errorBody))
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

        val repository = HeatHealthRepository(apiService = mockApi)
        val result = repository.fetchHeatRiskData(28.6, 77.2, "Delhi")

        assertTrue("Result should be failure on 502 server error", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be IOException", exception is IOException)
        assertTrue("Error message should mention server error and code 502", exception?.message?.contains("502") == true)
    }

    @Test
    fun testMalformedJsonDataHandling() = runBlocking {
        val mockApi = object : HeatHealthApiService {
            override suspend fun getHealth(): Map<String, Any> = emptyMap()
            override suspend fun predictHeatRisk(request: PredictRequestDto): PredictResponseDto = throw UnsupportedOperationException()
            override suspend fun getWeather(latitude: Double, longitude: Double, label: String?): WeatherResponseDto = throw UnsupportedOperationException()
            override suspend fun getHeatRisk(latitude: Double, longitude: Double, label: String?): HeatRiskResponseDto {
                throw JsonSyntaxException("Expected BEGIN_OBJECT but was STRING")
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

        val repository = HeatHealthRepository(apiService = mockApi)
        val result = repository.fetchHeatRiskData(28.6, 77.2, "Delhi")

        assertTrue("Result should be failure on malformed JSON data", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be IllegalArgumentException", exception is IllegalArgumentException)
        assertTrue("Message must indicate malformed JSON", exception?.message?.contains("malformed JSON") == true)
    }

    @Test
    fun testNetworkTimeoutExceptionHandling() = runBlocking {
        val mockApi = object : HeatHealthApiService {
            override suspend fun getHealth(): Map<String, Any> = emptyMap()
            override suspend fun predictHeatRisk(request: PredictRequestDto): PredictResponseDto = throw UnsupportedOperationException()
            override suspend fun getWeather(latitude: Double, longitude: Double, label: String?): WeatherResponseDto = throw UnsupportedOperationException()
            override suspend fun getHeatRisk(latitude: Double, longitude: Double, label: String?): HeatRiskResponseDto {
                throw IOException("Connection timed out connecting to http://127.0.0.1:8000")
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

        val repository = HeatHealthRepository(apiService = mockApi, enableCandidateFailover = false)
        val result = repository.fetchHeatRiskData(28.6, 77.2, "Delhi")

        assertTrue("Result should be failure on network timeout", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue("Exception should be IOException", exception is IOException)
        assertTrue("Error message should explain connection issue", exception?.message?.contains("Unable to connect to FastAPI backend") == true)
    }
}
