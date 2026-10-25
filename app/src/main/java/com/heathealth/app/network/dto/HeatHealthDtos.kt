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

data class CollegeDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("city") val city: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("is_verified") val isVerified: Boolean = true,
    @SerializedName("data_source") val dataSource: String = "Verified Directory",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
)

data class CollegesResponseDto(
    @SerializedName("colleges") val colleges: List<CollegeDto> = emptyList(),
    @SerializedName("total_count") val totalCount: Int = 0
)

data class CampusFacilityDto(
    @SerializedName("id") val id: String,
    @SerializedName("college_id") val collegeId: String = "col_iisc",
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("building_location") val buildingLocation: String,
    @SerializedName("opening_hours") val openingHours: String? = null,
    @SerializedName("accessibility_info") val accessibilityInfo: String? = null,
    @SerializedName("status") val status: String = "Operational",
    @SerializedName("is_verified") val isVerified: Boolean = true,
    @SerializedName("last_verified_timestamp") val lastVerifiedTimestamp: String,
    @SerializedName("created_by") val createdBy: String = "Campus Member",
    @SerializedName("updated_by") val updatedBy: String = "Campus Member",
    @SerializedName("updated_timestamp") val updatedTimestamp: String = "",
    @SerializedName("contact_info") val contactInfo: String? = null
)

data class CampusFacilitiesResponseDto(
    @SerializedName("facilities") val facilities: List<CampusFacilityDto> = emptyList(),
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("last_updated") val lastUpdated: String = ""
)

data class AddFacilityRequestDto(
    @SerializedName("college_id") val collegeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("building_location") val buildingLocation: String,
    @SerializedName("opening_hours") val openingHours: String? = null,
    @SerializedName("accessibility_info") val accessibilityInfo: String? = null,
    @SerializedName("contact_info") val contactInfo: String? = null
)

data class ReportStatusRequestDto(
    @SerializedName("facility_id") val facilityId: String,
    @SerializedName("status") val status: String,
    @SerializedName("status_note") val statusNote: String? = null,
    @SerializedName("reported_by") val reportedBy: String? = "Campus Member"
)

// Phase 8 Smart Heat Alerts DTOs
data class AlertLogDto(
    @SerializedName("id") val id: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("iso_timestamp") val isoTimestamp: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("trigger_source") val triggerSource: String = "Local Risk Monitor",
    @SerializedName("recommendations") val recommendations: List<String> = emptyList()
)

data class AlertHistoryResponseDto(
    @SerializedName("alerts") val alerts: List<AlertLogDto> = emptyList(),
    @SerializedName("total_count") val totalCount: Int = 0
)

data class CreateAlertRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("trigger_source") val triggerSource: String = "Local Risk Monitor",
    @SerializedName("recommendations") val recommendations: List<String> = emptyList()
)

data class AlertPreferencesDto(
    @SerializedName("heat_alerts_enabled") val heatAlertsEnabled: Boolean = true,
    @SerializedName("notifications_enabled") val notificationsEnabled: Boolean = true,
    @SerializedName("alert_threshold") val alertThreshold: String = "High",
    @SerializedName("reminder_frequency_hours") val reminderFrequencyHours: Int = 2,
    @SerializedName("cooldown_minutes") val cooldownMinutes: Int = 60,
    @SerializedName("last_alerted_risk_level") val lastAlertedRiskLevel: String = ""
)

data class AlertEvaluationRequestDto(
    @SerializedName("temperature_c") val temperatureC: Double,
    @SerializedName("humidity_percent") val humidityPercent: Double,
    @SerializedName("user_threshold") val userThreshold: String = "High",
    @SerializedName("last_alerted_risk_level") val lastAlertedRiskLevel: String = ""
)

data class AlertEvaluationResponseDto(
    @SerializedName("should_alert") val shouldAlert: Boolean,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("recommendations") val recommendations: List<RecommendationDto> = emptyList(),
    @SerializedName("reason") val reason: String
)

data class UpcomingForecastItemDto(
    @SerializedName("time_period") val timePeriod: String,
    @SerializedName("temperature_c") val temperatureC: Double,
    @SerializedName("humidity_percent") val humidityPercent: Double,
    @SerializedName("apparent_temperature_c") val apparentTemperatureC: Double? = null,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("recommendations") val recommendations: List<String> = emptyList()
)

data class UpcomingForecastResponseDto(
    @SerializedName("forecast_available") val forecastAvailable: Boolean,
    @SerializedName("location_label") val locationLabel: String,
    @SerializedName("items") val items: List<UpcomingForecastItemDto> = emptyList(),
    @SerializedName("generated_at") val generatedAt: String
)

data class DeviceTokenRequestDto(
    @SerializedName("device_token") val deviceToken: String,
    @SerializedName("device_type") val deviceType: String = "android"
)

data class DeviceTokenResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

