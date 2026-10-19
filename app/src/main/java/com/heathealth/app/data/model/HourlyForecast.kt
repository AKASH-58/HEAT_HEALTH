package com.heathealth.app.data.model

data class HourlyForecast(
    val time: String,
    val temperatureCelsius: Int,
    val humidityPercent: Int,
    val feelsLikeCelsius: Int,
    val windSpeedKmh: Int? = null,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val heatIndexScore: Double? = null
)
