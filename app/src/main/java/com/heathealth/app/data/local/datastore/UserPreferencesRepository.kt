package com.heathealth.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.datastore.preferences.core.longPreferencesKey

import androidx.datastore.preferences.core.doublePreferencesKey

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val hydrationRemindersEnabled: Boolean = true,
    val reminderFrequencyHours: Int = 2,
    val notificationsEnabled: Boolean = true,
    val heatAlertsEnabled: Boolean = true,
    val alertThreshold: String = "High",
    val lastAlertedRiskLevel: String = "",
    val lastAlertedTimestamp: Long = 0L,
    val selectedCollegeId: String = "col_iisc",
    val selectedLanguage: String = "en",
    val lastLatitude: Double = 0.0,
    val lastLongitude: Double = 0.0,
    val lastLocationLabel: String = ""
)

open class UserPreferencesRepository(private val context: Context?) {

    private object PreferencesKeys {
        val HYDRATION_REMINDERS_ENABLED = booleanPreferencesKey("hydration_reminders_enabled")
        val REMINDER_FREQUENCY_HOURS = intPreferencesKey("reminder_frequency_hours")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val HEAT_ALERTS_ENABLED = booleanPreferencesKey("heat_alerts_enabled")
        val ALERT_THRESHOLD = stringPreferencesKey("alert_threshold")
        val LAST_ALERTED_RISK_LEVEL = stringPreferencesKey("last_alerted_risk_level")
        val LAST_ALERTED_TIMESTAMP = longPreferencesKey("last_alerted_timestamp")
        val SELECTED_COLLEGE_ID = stringPreferencesKey("selected_college_id")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val LAST_LATITUDE = doublePreferencesKey("last_latitude")
        val LAST_LONGITUDE = doublePreferencesKey("last_longitude")
        val LAST_LOCATION_LABEL = stringPreferencesKey("last_location_label")
    }

    open val userPreferencesFlow: Flow<UserPreferences> = context?.dataStore?.data?.map { preferences ->
        val remindersEnabled = preferences[PreferencesKeys.HYDRATION_REMINDERS_ENABLED] ?: true
        val frequencyHours = preferences[PreferencesKeys.REMINDER_FREQUENCY_HOURS] ?: 2
        val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        val heatAlerts = preferences[PreferencesKeys.HEAT_ALERTS_ENABLED] ?: true
        val threshold = preferences[PreferencesKeys.ALERT_THRESHOLD] ?: "High"
        val lastRisk = preferences[PreferencesKeys.LAST_ALERTED_RISK_LEVEL] ?: ""
        val lastTimestamp = preferences[PreferencesKeys.LAST_ALERTED_TIMESTAMP] ?: 0L
        val collegeId = preferences[PreferencesKeys.SELECTED_COLLEGE_ID] ?: "col_iisc"
        val language = preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "en"
        val lat = preferences[PreferencesKeys.LAST_LATITUDE] ?: 0.0
        val lng = preferences[PreferencesKeys.LAST_LONGITUDE] ?: 0.0
        val label = preferences[PreferencesKeys.LAST_LOCATION_LABEL] ?: ""

        UserPreferences(
            hydrationRemindersEnabled = remindersEnabled,
            reminderFrequencyHours = frequencyHours,
            notificationsEnabled = notificationsEnabled,
            heatAlertsEnabled = heatAlerts,
            alertThreshold = threshold,
            lastAlertedRiskLevel = lastRisk,
            lastAlertedTimestamp = lastTimestamp,
            selectedCollegeId = collegeId,
            selectedLanguage = language,
            lastLatitude = lat,
            lastLongitude = lng,
            lastLocationLabel = label
        )
    } ?: kotlinx.coroutines.flow.flowOf(UserPreferences())

    open suspend fun updateLocation(latitude: Double, longitude: Double, label: String) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.LAST_LATITUDE] = latitude
            preferences[PreferencesKeys.LAST_LONGITUDE] = longitude
            preferences[PreferencesKeys.LAST_LOCATION_LABEL] = label
        }
    }

    open suspend fun updateSelectedLanguage(languageCode: String) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = languageCode
        }
    }

    open suspend fun updateSelectedCollegeId(collegeId: String) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_COLLEGE_ID] = collegeId
        }
    }

    open suspend fun updateHydrationRemindersEnabled(enabled: Boolean) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.HYDRATION_REMINDERS_ENABLED] = enabled
        }
    }

    open suspend fun updateReminderFrequencyHours(hours: Int) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_FREQUENCY_HOURS] = hours
        }
    }

    open suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    open suspend fun updateHeatAlertsEnabled(enabled: Boolean) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.HEAT_ALERTS_ENABLED] = enabled
        }
    }

    open suspend fun updateAlertThreshold(threshold: String) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.ALERT_THRESHOLD] = threshold
        }
    }

    open suspend fun updateLastAlertedRiskLevel(riskLevel: String) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.LAST_ALERTED_RISK_LEVEL] = riskLevel
        }
    }

    open suspend fun updateLastAlertedTimestamp(timestamp: Long) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.LAST_ALERTED_TIMESTAMP] = timestamp
        }
    }
}
