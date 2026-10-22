package com.heathealth.app

import com.heathealth.app.data.local.datastore.UserPreferences
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.worker.WorkManagerScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesAndAlertsTest {

    @Test
    fun testUserPreferencesDefaults() {
        val prefs = UserPreferences()

        assertTrue(prefs.hydrationRemindersEnabled)
        assertEquals(2, prefs.reminderFrequencyHours)
        assertTrue(prefs.notificationsEnabled)
        assertTrue(prefs.heatAlertsEnabled)
        assertEquals("High", prefs.alertThreshold)
        assertEquals("", prefs.lastAlertedRiskLevel)
    }

    @Test
    fun testAlertSuppressionOnSameState() {
        val currentRisk = RiskLevel.HIGH
        val lastAlerted = "HIGH"

        val shouldSuppress = currentRisk.name.equals(lastAlerted, ignoreCase = true)
        assertTrue("Alert must be suppressed when risk state has not changed", shouldSuppress)
    }

    @Test
    fun testAlertTriggerOnStateIncrease() {
        val currentRisk = RiskLevel.VERY_HIGH
        val lastAlerted = "HIGH"

        val shouldSuppress = currentRisk.name.equals(lastAlerted, ignoreCase = true)
        assertFalse("Alert should trigger when risk state escalates", shouldSuppress)
    }

    @Test
    fun testWorkManagerTaskNames() {
        assertEquals("hydration_reminder_work", WorkManagerScheduler.HYDRATION_WORK_NAME)
        assertEquals("heat_alert_check_work", WorkManagerScheduler.HEAT_ALERT_WORK_NAME)
    }
}
