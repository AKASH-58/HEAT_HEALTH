package com.heathealth.app

import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.model.DataSource
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.network.dto.AlertEvaluationResponseDto
import com.heathealth.app.network.dto.UpcomingForecastItemDto
import com.heathealth.app.network.dto.UpcomingForecastResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRiskLevelDisplayNamesAndOrdinals() {
        assertEquals("Low", RiskLevel.LOW.displayName)
        assertEquals("Moderate", RiskLevel.MODERATE.displayName)
        assertEquals("High", RiskLevel.HIGH.displayName)
        assertEquals("Extreme", RiskLevel.EXTREME.displayName)

        assertTrue(RiskLevel.EXTREME.ordinal > RiskLevel.HIGH.ordinal)
        assertTrue(RiskLevel.HIGH.ordinal > RiskLevel.MODERATE.ordinal)
        assertTrue(RiskLevel.MODERATE.ordinal > RiskLevel.LOW.ordinal)
    }

    @Test
    fun testUpcomingForecastResponseMapping() {
        val item = UpcomingForecastItemDto(
            timePeriod = "14:00",
            temperatureC = 38.0,
            humidityPercent = 60.0,
            apparentTemperatureC = 42.0,
            riskLevel = "Extreme",
            recommendations = listOf("Stay indoors in air conditioning")
        )

        val response = UpcomingForecastResponseDto(
            forecastAvailable = true,
            locationLabel = "Test Campus",
            items = listOf(item),
            generatedAt = "2025-05-15T14:00:00Z"
        )

        assertTrue(response.forecastAvailable)
        assertEquals(1, response.items.size)
        assertEquals("Extreme", response.items[0].riskLevel)
        assertEquals("14:00", response.items[0].timePeriod)
    }

    @Test
    fun testAlertEvaluationLogic() {
        val evaluation = AlertEvaluationResponseDto(
            shouldAlert = true,
            riskLevel = "High",
            title = "High Risk Advisory",
            message = "Thermal risk has reached High level.",
            recommendations = emptyList(),
            reason = "Risk exceeded configured threshold."
        )

        assertTrue(evaluation.shouldAlert)
        assertEquals("High Risk Advisory", evaluation.title)
    }

    @Test
    fun testAlertLogEntityCreation() {
        val log = AlertLogEntity(
            timestamp = 1700000000000L,
            isoTimestamp = "2025-05-15T12:00:00Z",
            title = "Extreme Heat Advisory",
            description = "Temperature exceeded 40°C",
            riskLevel = "Extreme",
            triggerSource = "Automated Test"
        )

        assertEquals("Extreme Heat Advisory", log.title)
        assertEquals("Extreme", log.riskLevel)
        assertEquals("Automated Test", log.triggerSource)
    }
}
