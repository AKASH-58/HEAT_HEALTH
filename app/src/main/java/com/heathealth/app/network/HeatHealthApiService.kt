package com.heathealth.app.network

import com.heathealth.app.network.dto.HeatRiskResponseDto
import com.heathealth.app.network.dto.PredictRequestDto
import com.heathealth.app.network.dto.PredictResponseDto
import com.heathealth.app.network.dto.WeatherResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
}
