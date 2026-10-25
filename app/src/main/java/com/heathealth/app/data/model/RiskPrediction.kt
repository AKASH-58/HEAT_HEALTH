package com.heathealth.app.data.model

enum class RiskLevel(val displayName: String) {
    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High"),
    VERY_HIGH("Very High"),
    EXTREME("Extreme"),
    UNKNOWN("General Guidance");

    companion object {
        fun fromDisplayName(name: String): RiskLevel {
            return entries.firstOrNull {
                it.displayName.equals(name, ignoreCase = true) ||
                        it.name.equals(name, ignoreCase = true) ||
                        it.name.replace("_", " ").equals(name, ignoreCase = true)
            } ?: MODERATE
        }
    }
}

data class RiskPrediction(
    val level: RiskLevel = RiskLevel.MODERATE,
    val score: Double? = null,
    val scoreInterpretation: String = "",
    val method: String = "Baseline Rule Engine",
    val title: String = "Sample Risk Assessment",
    val subtitle: String = "Demo Prediction",
    val description: String = "Heat strain risk is elevated under current demonstration weather conditions.",
    val isDemoData: Boolean = true
)
