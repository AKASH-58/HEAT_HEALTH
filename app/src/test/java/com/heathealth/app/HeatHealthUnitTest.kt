package com.heathealth.app

import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.viewmodel.DashboardViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HeatHealthUnitTest {

    private val repository = HeatHealthRepository()

    @Test
    fun testDashboardSampleWeatherValues() {
        val uiState = repository.getDashboardSampleData()
        val weather = uiState.weatherData

        assertEquals(34, weather.temperatureCelsius)
        assertEquals(65, weather.humidityPercent)
        assertEquals(38, weather.feelsLikeCelsius)
        assertTrue("Weather data must be explicitly marked as demo data", weather.isDemoData)
    }

    @Test
    fun testDashboardSampleRiskPrediction() {
        val uiState = repository.getDashboardSampleData()
        val risk = uiState.riskPrediction

        assertEquals(RiskLevel.MODERATE, risk.level)
        assertEquals("Moderate", risk.level.displayName)
        assertEquals("Sample Risk Assessment", risk.title)
        assertEquals("Demo Prediction", risk.subtitle)
        assertTrue("Risk prediction must be explicitly marked as demo data", risk.isDemoData)
    }

    @Test
    fun testRiskLevelMapping() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromDisplayName("Low"))
        assertEquals(RiskLevel.MODERATE, RiskLevel.fromDisplayName("Moderate"))
        assertEquals(RiskLevel.HIGH, RiskLevel.fromDisplayName("High"))
        assertEquals(RiskLevel.VERY_HIGH, RiskLevel.fromDisplayName("Very High"))
        assertEquals(RiskLevel.VERY_HIGH, RiskLevel.fromDisplayName("VERY_HIGH"))
    }

    @Test
    fun testSafetyRecommendations() {
        val uiState = repository.getDashboardSampleData()
        val list = uiState.recommendations

        assertEquals(4, list.size)
        assertTrue(list.any { it.title.contains("Drink water", ignoreCase = true) })
        assertTrue(list.any { it.title.contains("shaded or cool area", ignoreCase = true) })
        assertTrue(list.any { it.title.contains("outdoor activity", ignoreCase = true) })
        assertTrue(list.any { it.title.contains("warning signs", ignoreCase = true) })
    }

    @Test
    fun testPrototypeDisclaimer() {
        val uiState = repository.getDashboardSampleData()

        assertNotNull(uiState.disclaimer)
        assertTrue("Disclaimer must state prototype nature", uiState.disclaimer.contains("prototype", ignoreCase = true))
        assertTrue("Disclaimer must clarify non-medical status", uiState.disclaimer.contains("medical diagnosis", ignoreCase = true))
    }

    @Test
    fun testDashboardViewModelOfflineSampleLoad() {
        val viewModel = DashboardViewModel(repository, autoLoad = false)
        viewModel.loadSampleData()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(34, state.weatherData.temperatureCelsius)
    }
}
