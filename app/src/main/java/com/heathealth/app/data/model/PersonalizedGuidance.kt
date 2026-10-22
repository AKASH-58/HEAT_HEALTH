package com.heathealth.app.data.model

data class ActivityGuidance(
    val activityName: String,
    val recommendedAction: String,
    val hydrationTip: String,
    val warningNote: String? = null
)

data class PersonalizedGuidance(
    val riskLevel: RiskLevel,
    val overviewMessage: String,
    val activityGuidances: List<ActivityGuidance>,
    val heatstrokeEmergencyAdvice: String
)

object PersonalizedGuidanceEngine {

    fun generateGuidance(
        riskLevel: RiskLevel,
        temperatureC: Int = 30,
        humidityPercent: Int = 50,
        activityPattern: String = "Regular commuting"
    ): PersonalizedGuidance {
        val overview = when (riskLevel) {
            RiskLevel.LOW -> "Environmental thermal stress is low ($temperatureC°C, $humidityPercent% RH). Safe for routine campus activities."
            RiskLevel.MODERATE -> "Moderate thermal strain ($temperatureC°C, $humidityPercent% RH). Prolonged exposure may cause mild fatigue. Stay hydrated."
            RiskLevel.HIGH -> "High environmental heat risk ($temperatureC°C, $humidityPercent% RH). Increased risk of heat exhaustion during outdoor campus travel or sports."
            RiskLevel.VERY_HIGH, RiskLevel.EXTREME -> "Extreme thermal strain danger ($temperatureC°C, $humidityPercent% RH). Avoid prolonged physical exertion outdoors. High risk of heat illness."
        }

        val isHighOrExtreme = riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.VERY_HIGH || riskLevel == RiskLevel.EXTREME

        val activities = listOf(
            ActivityGuidance(
                activityName = "Walking to College / Campus Commute",
                recommendedAction = when {
                    riskLevel == RiskLevel.LOW -> "Walk normally; wear comfortable footwear."
                    riskLevel == RiskLevel.MODERATE -> "Use shaded walkways or covered paths across campus."
                    isHighOrExtreme -> "Carry an umbrella/cap, walk at a relaxed pace, and take shade breaks."
                    else -> "Use shaded walkways across campus."
                },
                hydrationTip = "Drink 250-300 mL water before leaving your room or hostel."
            ),
            ActivityGuidance(
                activityName = "Outdoor Classes & Fieldwork",
                recommendedAction = when {
                    riskLevel == RiskLevel.LOW -> "Standard outdoor class participation."
                    riskLevel == RiskLevel.MODERATE -> "Sit in shaded seating areas or under trees during lectures."
                    isHighOrExtreme -> "Request instructor to move class to a ventilated indoor venue during peak midday hours."
                    else -> "Sit in shaded seating areas."
                },
                hydrationTip = "Keep a re-fillable water bottle accessible at all times."
            ),
            ActivityGuidance(
                activityName = "Sports & Physical Activities",
                recommendedAction = when {
                    riskLevel == RiskLevel.LOW -> "Normal sports practice and outdoor workouts."
                    riskLevel == RiskLevel.MODERATE -> "Schedule 10-minute rest breaks every 30 minutes in shade."
                    isHighOrExtreme -> "Postpone heavy exertion until after 5:00 PM or move to indoor gym."
                    else -> "Take frequent shaded rest breaks."
                },
                hydrationTip = "Drink fluids with electrolytes every 15-20 minutes during physical activity."
            ),
            ActivityGuidance(
                activityName = "Indoor Study & Classroom Exposure",
                recommendedAction = when {
                    riskLevel == RiskLevel.LOW -> "Standard indoor study environment."
                    riskLevel == RiskLevel.MODERATE -> "Ensure fans or ventilation are active in classrooms and library."
                    isHighOrExtreme -> "Study in air-conditioned or well-ventilated campus reading halls."
                    else -> "Ensure adequate indoor air circulation."
                },
                hydrationTip = "Sip small quantities of water continuously while studying."
            )
        )

        val emergencyAdvice = "Emergency Protocol: If someone shows heatstroke signs (confusion, hot dry skin, fainting, body temp >40°C):\n" +
                "1. Move them to a shaded/cooler area immediately.\n" +
                "2. Cool body with cold water, wet towels, or ice packs.\n" +
                "3. Call emergency medical services (108/112).\n" +
                "4. CRITICAL: Do NOT give fluids to anyone with altered consciousness or who cannot swallow safely."

        return PersonalizedGuidance(
            riskLevel = riskLevel,
            overviewMessage = overview,
            activityGuidances = activities,
            heatstrokeEmergencyAdvice = emergencyAdvice
        )
    }
}
