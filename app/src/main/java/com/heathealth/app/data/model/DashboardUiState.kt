package com.heathealth.app.data.model

import com.heathealth.app.network.dto.PredictResponseDto

data class CustomPredictionState(
    val temperatureC: Double = 38.0,
    val humidityPercent: Double = 65.0,
    val windSpeedKmh: Double = 8.0,
    val isPredicting: Boolean = false,
    val predictionResult: PredictResponseDto? = null,
    val errorMessage: String? = null
)

data class DashboardUiState(
    val weatherData: WeatherData = WeatherData(),
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val riskPrediction: RiskPrediction = RiskPrediction(),
    val factors: List<String> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val disclaimer: String = "Prototype Notice: HeatHealth is an early-phase research prototype and does not provide a medical diagnosis or official heat warning. Always follow instructions from public health authorities.",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val locationLabel: String = "New Delhi, India",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val lastUpdated: String = "",
    val customPrediction: CustomPredictionState = CustomPredictionState()
)
