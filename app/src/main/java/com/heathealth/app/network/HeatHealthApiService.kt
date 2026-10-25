package com.heathealth.app.network

import com.heathealth.app.network.dto.AddFacilityRequestDto
import com.heathealth.app.network.dto.CampusFacilitiesResponseDto
import com.heathealth.app.network.dto.CampusFacilityDto
import com.heathealth.app.network.dto.CollegesResponseDto
import com.heathealth.app.network.dto.HeatRiskResponseDto
import com.heathealth.app.network.dto.PredictRequestDto
import com.heathealth.app.network.dto.PredictResponseDto
import com.heathealth.app.network.dto.ReportStatusRequestDto
import com.heathealth.app.network.dto.WeatherResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HeatHealthApiService {

    @GET("health")
    suspend fun getHealth(): Map<String, Any>

    @POST("predict")
    suspend fun predictHeatRisk(
        @Body request: PredictRequestDto
    ): PredictResponseDto

    @GET("api/v1/weather")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("label") label: String? = null
    ): WeatherResponseDto

    @GET("api/v1/heat-risk")
    suspend fun getHeatRisk(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("label") label: String? = null
    ): HeatRiskResponseDto

    @GET("api/v1/campus-cool-map/colleges")
    suspend fun getColleges(
        @Query("query") query: String? = null,
        @Query("city") city: String? = null
    ): CollegesResponseDto

    @GET("api/v1/campus-cool-map/facilities")
    suspend fun getCampusFacilities(
        @Query("college_id") collegeId: String? = null,
        @Query("category") category: String? = null
    ): CampusFacilitiesResponseDto

    @POST("api/v1/campus-cool-map/facilities")
    suspend fun addCampusFacility(
        @Body request: AddFacilityRequestDto
    ): CampusFacilityDto

    @PUT("api/v1/campus-cool-map/facilities/{facility_id}/status")
    suspend fun updateFacilityStatus(
        @Path("facility_id") facilityId: String,
        @Body request: ReportStatusRequestDto
    ): CampusFacilityDto

    // Phase 8 Smart Heat Alerts Endpoints
    @GET("api/v1/alerts/history")
    suspend fun getAlertHistory(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): com.heathealth.app.network.dto.AlertHistoryResponseDto

    @POST("api/v1/alerts/history")
    suspend fun createAlertHistory(
        @Body request: com.heathealth.app.network.dto.CreateAlertRequestDto
    ): com.heathealth.app.network.dto.AlertLogDto

    @retrofit2.http.DELETE("api/v1/alerts/history")
    suspend fun clearAlertHistory(): Map<String, Any>

    @GET("api/v1/alerts/preferences")
    suspend fun getAlertPreferences(): com.heathealth.app.network.dto.AlertPreferencesDto

    @PUT("api/v1/alerts/preferences")
    suspend fun updateAlertPreferences(
        @Body preferences: com.heathealth.app.network.dto.AlertPreferencesDto
    ): com.heathealth.app.network.dto.AlertPreferencesDto

    @POST("api/v1/alerts/evaluate")
    suspend fun evaluateAlert(
        @Body request: com.heathealth.app.network.dto.AlertEvaluationRequestDto
    ): com.heathealth.app.network.dto.AlertEvaluationResponseDto

    @GET("api/v1/alerts/forecast")
    suspend fun getUpcomingForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("label") label: String? = null
    ): com.heathealth.app.network.dto.UpcomingForecastResponseDto

    @POST("api/v1/alerts/device-token")
    suspend fun registerDeviceToken(
        @Body request: com.heathealth.app.network.dto.DeviceTokenRequestDto
    ): com.heathealth.app.network.dto.DeviceTokenResponseDto
}
