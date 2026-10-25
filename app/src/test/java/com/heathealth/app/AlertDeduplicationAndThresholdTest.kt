package com.heathealth.app

import com.heathealth.app.data.local.datastore.UserPreferences
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.worker.HeatAlertEvaluator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertDeduplicationAndThresholdTest {

    @Test
    fun testRiskThresholdFilteringWithProductionEvaluator() {
        val prefs = UserPreferences(
            heatAlertsEnabled = true,
            notificationsEnabled = true,
            alertThreshold = "High"
        )

        assertTrue(HeatAlertEvaluator.shouldTriggerAlert(prefs, RiskLevel.HIGH))
        assertTrue(HeatAlertEvaluator.shouldTriggerAlert(prefs, RiskLevel.EXTREME))
        assertFalse(HeatAlertEvaluator.shouldTriggerAlert(prefs, RiskLevel.MODERATE))
        assertFalse(HeatAlertEvaluator.shouldTriggerAlert(prefs, RiskLevel.LOW))
    }

    @Test
    fun testDisabledAlertsSuppressNotificationWithProductionEvaluator() {
        val disabledPrefs1 = UserPreferences(heatAlertsEnabled = false, notificationsEnabled = true, alertThreshold = "Moderate")
        val disabledPrefs2 = UserPreferences(heatAlertsEnabled = true, notificationsEnabled = false, alertThreshold = "Moderate")

        assertFalse(HeatAlertEvaluator.shouldTriggerAlert(disabledPrefs1, RiskLevel.EXTREME))
        assertFalse(HeatAlertEvaluator.shouldTriggerAlert(disabledPrefs2, RiskLevel.EXTREME))
    }

    @Test
    fun testDuplicateAlertSuppressionWhenRiskUnchangedAndCooldownActive() {
        val basePrefs = UserPreferences(
            heatAlertsEnabled = true,
            notificationsEnabled = true,
            alertThreshold = "High",
            reminderFrequencyHours = 2,
            lastAlertedRiskLevel = "HIGH",
            lastAlertedTimestamp = 1000000000000L
        )

        val currentTime = 1000000000000L + (1 * 3600 * 1000L) // 1 hour elapsed (< 2 hours)
        val shouldAlert = HeatAlertEvaluator.shouldTriggerAlert(basePrefs, RiskLevel.HIGH, currentTime)

        assertFalse("Alert must be suppressed when risk state is unchanged AND within 2-hour cooldown window", shouldAlert)
    }

    @Test
    fun testAlertTriggersWhenCooldownPeriodExpires() {
        val basePrefs = UserPreferences(
            heatAlertsEnabled = true,
            notificationsEnabled = true,
            alertThreshold = "High",
            reminderFrequencyHours = 2,
            lastAlertedRiskLevel = "HIGH",
            lastAlertedTimestamp = 1000000000000L
        )

        val currentTime = 1000000000000L + (2 * 3600 * 1000L + 1000L) // 2 hours and 1 sec elapsed
        val shouldAlert = HeatAlertEvaluator.shouldTriggerAlert(basePrefs, RiskLevel.HIGH, currentTime)

        assertTrue("Alert should trigger when the 2-hour cooldown period has expired", shouldAlert)
    }

    @Test
    fun testAlertFiresOnRiskEscalation() {
        val basePrefs = UserPreferences(
            heatAlertsEnabled = true,
            notificationsEnabled = true,
            alertThreshold = "High",
            reminderFrequencyHours = 2,
            lastAlertedRiskLevel = "HIGH",
            lastAlertedTimestamp = 1000000000000L
        )

        val currentTime = 1000000000000L + (10 * 60 * 1000L) // only 10 minutes elapsed
        val shouldAlert = HeatAlertEvaluator.shouldTriggerAlert(basePrefs, RiskLevel.EXTREME, currentTime)

        assertTrue("Alert must trigger immediately when risk level escalates from HIGH to EXTREME even within cooldown window", shouldAlert)
    }
}
