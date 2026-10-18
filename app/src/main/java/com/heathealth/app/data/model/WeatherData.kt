package com.heathealth.app.data.model

data class WeatherData(
    val temperatureCelsius: Int = 34,
    val humidityPercent: Int = 65,
    val feelsLikeCelsius: Int = 38,
    val isDemoData: Boolean = true
)
