package com.heathealth.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val hydrationRemindersEnabled: Boolean = true,
    val reminderFrequencyHours: Int = 2,
    val notificationsEnabled: Boolean = true
)

open class UserPreferencesRepository(private val context: Context?) {

    private object PreferencesKeys {
        val HYDRATION_REMINDERS_ENABLED = booleanPreferencesKey("hydration_reminders_enabled")
        val REMINDER_FREQUENCY_HOURS = intPreferencesKey("reminder_frequency_hours")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    open val userPreferencesFlow: Flow<UserPreferences> = context?.dataStore?.data?.map { preferences ->
        val remindersEnabled = preferences[PreferencesKeys.HYDRATION_REMINDERS_ENABLED] ?: true
        val frequencyHours = preferences[PreferencesKeys.REMINDER_FREQUENCY_HOURS] ?: 2
        val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true

        UserPreferences(
            hydrationRemindersEnabled = remindersEnabled,
            reminderFrequencyHours = frequencyHours,
            notificationsEnabled = notificationsEnabled
        )
    } ?: kotlinx.coroutines.flow.flowOf(UserPreferences())

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
}
