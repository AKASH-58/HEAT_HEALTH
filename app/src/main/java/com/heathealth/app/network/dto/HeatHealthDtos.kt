package com.heathealth.app.network.dto

import com.google.gson.annotations.SerializedName

data class LocationDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("location_label") val locationLabel: String? = null
)

data class WeatherDto(
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("relative_humidity") val relativeHumidity: Double,
    @SerializedName("wind_speed") val windSpeed: Double? = null,
    @SerializedName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("source") val source: String,
    @SerializedName("is_forecast") val isForecast: Boolean = false
)

data class HourlyWeatherDto(
    @SerializedName("time") val time: String,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("relative_humidity") val relativeHumidity: Double,
    @SerializedName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerializedName("wind_speed") val windSpeed: Double? = null,
    @SerializedName("heat_risk_level") val heatRiskLevel: String = "Low",
    @SerializedName("heat_index_score") val heatIndexScore: Double? = null
)

data class RiskDto(
    @SerializedName("level") val level: String,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("score_interpretation") val scoreInterpretation: String,
    @SerializedName("method") val method: String
)

data class RecommendationDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String = "Safety Action"
)

data class MetadataDto(
    @SerializedName("rules_version") val rulesVersion: String,
    @SerializedName("model_version") val modelVersion: String? = null,
    @SerializedName("prediction_timestamp") val predictionTimestamp: String,
    @SerializedName("data_quality") val dataQuality: String
)

data class HeatRiskResponseDto(
    @SerializedName("location") val location: LocationDto,
    @SerializedName("weather") val weather: WeatherDto,
    @SerializedName("hourly_forecast") val hourlyForecast: List<HourlyWeatherDto> = emptyList(),
    @SerializedName("risk") val risk: RiskDto,
    @SerializedName("factors") val factors: List<String> = emptyList(),
    @SerializedName("recommendations") val recommendations: List<RecommendationDto> = emptyList(),
    @SerializedName("metadata") val metadata: MetadataDto
)

data class WeatherResponseDto(
    @SerializedName("location") val location: LocationDto,
    @SerializedName("weather") val weather: WeatherDto,
    @SerializedName("hourly_forecast") val hourlyForecast: List<HourlyWeatherDto> = emptyList(),
    @SerializedName("units") val units: Map<String, String> = emptyMap()
)

data class PredictRequestDto(
    @SerializedName("temperature_c") val temperatureC: Double,
    @SerializedName("humidity_percent") val humidityPercent: Double,
    @SerializedName("wind_speed_kmh") val windSpeedKmh: Double? = null,
    @SerializedName("apparent_temperature_c") val apparentTemperatureC: Double? = null
)

data class PredictResponseDto(
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("risk_score") val riskScore: Double? = null,
    @SerializedName("prediction_message") val predictionMessage: String,
    @SerializedName("factors") val factors: List<String> = emptyList(),
    @SerializedName("safety_recommendations") val safetyRecommendations: List<RecommendationDto> = emptyList(),
    @SerializedName("model_version") val modelVersion: String = "1.0.0-rf-prototype",
    @SerializedName("is_demo_model") val isDemoModel: Boolean = true
)
