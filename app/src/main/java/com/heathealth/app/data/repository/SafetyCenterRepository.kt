package com.heathealth.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.safetyDataStore: DataStore<Preferences> by preferencesDataStore(name = "safety_center_preferences")

data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val category: String, // "Campus Health", "Personal Contact", "Public Helpline"
    val isOfficial: Boolean = false
)

data class EmergencyContactsData(
    val campusHealthName: String = "",
    val campusHealthPhone: String = "",
    val personalContactName: String = "",
    val personalContactPhone: String = "",
    val publicHelplineName: String = "National Emergency Helpline (India - Unified Response 112 / 108)",
    val publicHelplinePhone: String = "112"
)

data class SafetyChecklistState(
    val itemsCompletedMap: Map<String, Boolean> = emptyMap(),
    val lastResetDate: String = ""
)

open class SafetyCenterRepository(
    private val context: Context?,
    private val customDataStore: DataStore<Preferences>? = null,
    private val todayDateProvider: () -> String = { try { LocalDate.now().toString() } catch (e: Exception) { "" } }
) {

    private val targetDataStore: DataStore<Preferences>?
        get() = customDataStore ?: context?.safetyDataStore

    private object Keys {
        val CHECKLIST_RESET_DATE = stringPreferencesKey("checklist_reset_date")
        val CHECKLIST_WATER = booleanPreferencesKey("checklist_item_water")
        val CHECKLIST_REST = booleanPreferencesKey("checklist_item_rest")
        val CHECKLIST_SHADE = booleanPreferencesKey("checklist_item_shade")
        val CHECKLIST_ACTIVITY = booleanPreferencesKey("checklist_item_activity")
        val CHECKLIST_RISK = booleanPreferencesKey("checklist_item_risk")
        val CHECKLIST_CLOTHING = booleanPreferencesKey("checklist_item_clothing")

        val CAMPUS_HEALTH_NAME = stringPreferencesKey("campus_health_name")
        val CAMPUS_HEALTH_PHONE = stringPreferencesKey("campus_health_phone")
        val PERSONAL_CONTACT_NAME = stringPreferencesKey("personal_contact_name")
        val PERSONAL_CONTACT_PHONE = stringPreferencesKey("personal_contact_phone")
        val PUBLIC_HELPLINE_PHONE = stringPreferencesKey("public_helpline_phone")
    }

    open val checklistStateFlow: Flow<SafetyChecklistState> = targetDataStore?.data?.map { prefs ->
        val savedDate = prefs[Keys.CHECKLIST_RESET_DATE] ?: ""
        val todayStr = todayDateProvider()

        if (savedDate.isNotEmpty() && savedDate != todayStr && todayStr.isNotEmpty()) {
            // New day detected: return reset state
            SafetyChecklistState(
                itemsCompletedMap = mapOf(
                    "item_water" to false,
                    "item_rest" to false,
                    "item_shade" to false,
                    "item_activity" to false,
                    "item_risk" to false,
                    "item_clothing" to false
                ),
                lastResetDate = todayStr
            )
        } else {
            SafetyChecklistState(
                itemsCompletedMap = mapOf(
                    "item_water" to (prefs[Keys.CHECKLIST_WATER] ?: false),
                    "item_rest" to (prefs[Keys.CHECKLIST_REST] ?: false),
                    "item_shade" to (prefs[Keys.CHECKLIST_SHADE] ?: false),
                    "item_activity" to (prefs[Keys.CHECKLIST_ACTIVITY] ?: false),
                    "item_risk" to (prefs[Keys.CHECKLIST_RISK] ?: false),
                    "item_clothing" to (prefs[Keys.CHECKLIST_CLOTHING] ?: false)
                ),
                lastResetDate = if (savedDate.isEmpty()) todayStr else savedDate
            )
        }
    } ?: kotlinx.coroutines.flow.flowOf(SafetyChecklistState())

    open val emergencyContactsFlow: Flow<EmergencyContactsData> = targetDataStore?.data?.map { prefs ->
        EmergencyContactsData(
            campusHealthName = prefs[Keys.CAMPUS_HEALTH_NAME] ?: "",
            campusHealthPhone = prefs[Keys.CAMPUS_HEALTH_PHONE] ?: "",
            personalContactName = prefs[Keys.PERSONAL_CONTACT_NAME] ?: "",
            personalContactPhone = prefs[Keys.PERSONAL_CONTACT_PHONE] ?: "",
            publicHelplineName = "National Emergency Helpline (India - Unified Response 112 / 108)",
            publicHelplinePhone = prefs[Keys.PUBLIC_HELPLINE_PHONE] ?: "112"
        )
    } ?: kotlinx.coroutines.flow.flowOf(EmergencyContactsData())

    open suspend fun updateChecklistItem(itemId: String, isCompleted: Boolean, currentDate: String) {
        targetDataStore?.edit { prefs ->
            val savedDate = prefs[Keys.CHECKLIST_RESET_DATE] ?: ""
            if (savedDate.isNotEmpty() && savedDate != currentDate && currentDate.isNotEmpty()) {
                // Atomic daily reset transaction before setting updated item
                prefs[Keys.CHECKLIST_RESET_DATE] = currentDate
                prefs[Keys.CHECKLIST_WATER] = false
                prefs[Keys.CHECKLIST_REST] = false
                prefs[Keys.CHECKLIST_SHADE] = false
                prefs[Keys.CHECKLIST_ACTIVITY] = false
                prefs[Keys.CHECKLIST_RISK] = false
                prefs[Keys.CHECKLIST_CLOTHING] = false
            } else if (savedDate.isEmpty() && currentDate.isNotEmpty()) {
                prefs[Keys.CHECKLIST_RESET_DATE] = currentDate
            }

            when (itemId) {
                "item_water" -> prefs[Keys.CHECKLIST_WATER] = isCompleted
                "item_rest" -> prefs[Keys.CHECKLIST_REST] = isCompleted
                "item_shade" -> prefs[Keys.CHECKLIST_SHADE] = isCompleted
                "item_activity" -> prefs[Keys.CHECKLIST_ACTIVITY] = isCompleted
                "item_risk" -> prefs[Keys.CHECKLIST_RISK] = isCompleted
                "item_clothing" -> prefs[Keys.CHECKLIST_CLOTHING] = isCompleted
            }
        }
    }

    open suspend fun resetChecklistForNewDay(currentDate: String) {
        targetDataStore?.edit { prefs ->
            prefs[Keys.CHECKLIST_RESET_DATE] = currentDate
            prefs[Keys.CHECKLIST_WATER] = false
            prefs[Keys.CHECKLIST_REST] = false
            prefs[Keys.CHECKLIST_SHADE] = false
            prefs[Keys.CHECKLIST_ACTIVITY] = false
            prefs[Keys.CHECKLIST_RISK] = false
            prefs[Keys.CHECKLIST_CLOTHING] = false
        }
    }

    open suspend fun saveEmergencyContacts(
        campusName: String,
        campusPhone: String,
        personalName: String,
        personalPhone: String
    ) {
        targetDataStore?.edit { prefs ->
            prefs[Keys.CAMPUS_HEALTH_NAME] = campusName
            prefs[Keys.CAMPUS_HEALTH_PHONE] = campusPhone
            prefs[Keys.PERSONAL_CONTACT_NAME] = personalName
            prefs[Keys.PERSONAL_CONTACT_PHONE] = personalPhone
        }
    }

    companion object {
        fun isValidPhoneNumber(phone: String): Boolean {
            val trimmed = phone.trim()
            if (trimmed.isEmpty()) return false
            val digitCount = trimmed.count { it.isDigit() }
            return digitCount in 3..15 && trimmed.all { it.isDigit() || it in "+-() " }
        }
    }
}
