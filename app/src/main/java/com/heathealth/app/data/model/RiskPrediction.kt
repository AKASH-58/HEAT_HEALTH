package com.heathealth.app.data.model

enum class RiskLevel(val displayName: String) {
    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High"),
    EXTREME("Extreme")
}

data class RiskPrediction(
    val level: RiskLevel = RiskLevel.MODERATE,
    val title: String = "Sample Risk Assessment",
    val subtitle: String = "Demo Prediction",
    val description: String = "Heat strain risk is elevated under current demonstration weather conditions.",
    val isDemoData: Boolean = true
)
