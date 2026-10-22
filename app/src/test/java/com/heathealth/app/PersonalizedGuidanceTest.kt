package com.heathealth.app

import com.heathealth.app.data.model.PersonalizedGuidanceEngine
import com.heathealth.app.data.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalizedGuidanceTest {

    @Test
    fun testLowRiskGuidance() {
        val guidance = PersonalizedGuidanceEngine.generateGuidance(
            riskLevel = RiskLevel.LOW,
            temperatureC = 22,
            humidityPercent = 45
        )

        assertEquals(RiskLevel.LOW, guidance.riskLevel)
        assertTrue(guidance.overviewMessage.contains("low", ignoreCase = true))
        assertEquals(4, guidance.activityGuidances.size)
        assertTrue(guidance.activityGuidances.any { it.activityName.contains("Walking", ignoreCase = true) })
    }

    @Test
    fun testExtremeRiskGuidanceAndEmergencyProtocol() {
        val guidance = PersonalizedGuidanceEngine.generateGuidance(
            riskLevel = RiskLevel.EXTREME,
            temperatureC = 42,
            humidityPercent = 70
        )

        assertEquals(RiskLevel.EXTREME, guidance.riskLevel)
        assertTrue(guidance.overviewMessage.contains("Extreme", ignoreCase = true))

        val emergencyText = guidance.heatstrokeEmergencyAdvice
        assertNotNull(emergencyText)
        assertTrue("Emergency protocol must instruct to call 108/112", emergencyText.contains("108/112"))
        assertTrue("Emergency protocol must explicitly forbid giving fluids to unconscious person", emergencyText.contains("Do NOT give fluids", ignoreCase = true))
    }

    @Test
    fun testStudentActivityGuidanceCategories() {
        val guidance = PersonalizedGuidanceEngine.generateGuidance(
            riskLevel = RiskLevel.HIGH,
            temperatureC = 36,
            humidityPercent = 60
        )

        val activities = guidance.activityGuidances.map { it.activityName }
        assertTrue(activities.any { it.contains("Walking to College", ignoreCase = true) })
        assertTrue(activities.any { it.contains("Outdoor Classes", ignoreCase = true) })
        assertTrue(activities.any { it.contains("Sports", ignoreCase = true) })
        assertTrue(activities.any { it.contains("Indoor Study", ignoreCase = true) })
    }
}
