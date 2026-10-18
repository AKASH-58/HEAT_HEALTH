package com.heathealth.app.data.repository

import com.heathealth.app.data.model.DashboardUiState
import com.heathealth.app.data.model.Recommendation
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.model.RiskPrediction
import com.heathealth.app.data.model.WeatherData

class HeatHealthRepository {
    fun getDashboardSampleData(): DashboardUiState {
        return DashboardUiState(
            weatherData = WeatherData(
                temperatureCelsius = 34,
                humidityPercent = 65,
                feelsLikeCelsius = 38,
                isDemoData = true
            ),
            riskPrediction = RiskPrediction(
                level = RiskLevel.MODERATE,
                title = "Sample Risk",
                subtitle = "Demo Prediction",
                description = "Heat strain risk is elevated under current sample conditions.",
                isDemoData = true
            ),
            recommendations = listOf(
                Recommendation(
                    id = "1",
                    title = "Drink water regularly",
                    description = "Stay hydrated throughout the day even if you do not feel thirsty."
                ),
                Recommendation(
                    id = "2",
                    title = "Take breaks in a shaded or cool area",
                    description = "Rest periodically in shaded, ventilated, or air-conditioned environments."
                ),
                Recommendation(
                    id = "3",
                    title = "Avoid intense outdoor activity",
                    description = "Minimize strenuous outdoor exercises during peak heat afternoon hours."
                ),
                Recommendation(
                    id = "4",
                    title = "Watch for warning signs",
                    description = "Pay attention to dizziness, confusion, headaches, or unusual bodily weakness."
                )
            ),
            disclaimer = "Prototype Notice: HeatHealth is an early-phase research prototype and does not provide a medical diagnosis or official heat warning. Always follow instructions from public health authorities.",
            isLoading = false,
            errorMessage = null
        )
    }
}
