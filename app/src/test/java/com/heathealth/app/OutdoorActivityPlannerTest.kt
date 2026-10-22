package com.heathealth.app

import com.heathealth.app.data.model.HourlyForecast
import com.heathealth.app.data.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutdoorActivityPlannerTest {

    private val sampleHourlyForecast = listOf(
        HourlyForecast("10:00", 28, 50, 30, 10, RiskLevel.LOW, 28.0),
        HourlyForecast("12:00", 33, 60, 37, 10, RiskLevel.MODERATE, 37.5),
        HourlyForecast("14:00", 38, 65, 42, 12, RiskLevel.EXTREME, 42.0),
        HourlyForecast("16:00", 36, 55, 40, 15, RiskLevel.HIGH, 40.8),
        HourlyForecast("18:00", 29, 60, 31, 8, RiskLevel.LOW, 29.5)
    )

    @Test
    fun testFindMatchingForecastHour() {
        val selectedTime = "14:00"
        val matched = sampleHourlyForecast.find { it.time == selectedTime }

        assertNotNull(matched)
        assertEquals(RiskLevel.EXTREME, matched?.riskLevel)
        assertEquals(38, matched?.temperatureCelsius)
    }

    @Test
    fun testFilterAlternativeCoolerTimes() {
        val coolerTimes = sampleHourlyForecast.filter {
            it.riskLevel == RiskLevel.LOW || it.riskLevel == RiskLevel.MODERATE
        }

        assertEquals(3, coolerTimes.size)
        assertTrue(coolerTimes.any { it.time == "10:00" })
        assertTrue(coolerTimes.any { it.time == "12:00" })
        assertTrue(coolerTimes.any { it.time == "18:00" })
        assertFalse(coolerTimes.any { it.time == "14:00" })
    }

    @Test
    fun testUnavailableForecastHandling() {
        val selectedTime = "23:00"
        val matched = sampleHourlyForecast.find { it.time == selectedTime }

        assertNull("Selected time outside sample forecast list should return null", matched)
    }
}
