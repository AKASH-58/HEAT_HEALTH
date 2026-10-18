package com.heathealth.app.data.model

data class DashboardUiState(
    val weatherData: WeatherData = WeatherData(),
    val riskPrediction: RiskPrediction = RiskPrediction(),
    val recommendations: List<Recommendation> = emptyList(),
    val disclaimer: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
