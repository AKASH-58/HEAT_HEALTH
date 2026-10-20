package com.heathealth.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.model.ProfileUiState
import com.heathealth.app.data.repository.StudentProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StudentProfileViewModel(
    application: Application,
    private val repository: StudentProfileRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        StudentProfileRepository(
            HeatHealthDatabase.getDatabase(application).studentProfileDao(),
            UserPreferencesRepository(application)
        )
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.profileFlow,
                repository.userPreferencesFlow
            ) { profile, userPrefs ->
                ProfileUiState.fromEntity(
                    entity = profile,
                    remindersEnabled = userPrefs.hydrationRemindersEnabled,
                    reminderFrequencyHours = userPrefs.reminderFrequencyHours
                )
            }.collect { state ->
                val current = _uiState.value
                if (!current.isLoaded) {
                    _uiState.value = state
                } else {
                    _uiState.value = current.copy(
                        remindersEnabled = state.remindersEnabled,
                        reminderFrequencyHours = state.reminderFrequencyHours
                    )
                }
            }
        }
    }

    fun onNicknameChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            nickname = value,
            successMessage = null
        )
    }

    fun onActivityTypeChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            activityType = value,
            successMessage = null
        )
    }

    fun onPreferredLanguageChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            preferredLanguage = value,
            successMessage = null
        )
    }

    fun onHydrationTargetChanged(value: String) {
        val target = value.filter { it.isDigit() }
        val error = validateTarget(target)
        _uiState.value = _uiState.value.copy(
            hydrationTargetMlText = target,
            targetValidationError = error,
            successMessage = null
        )
    }

    fun onRemindersToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(remindersEnabled = enabled)
        viewModelScope.launch {
            repository.updateHydrationRemindersEnabled(enabled)
        }
    }

    fun onReminderFrequencyChanged(hours: Int) {
        _uiState.value = _uiState.value.copy(reminderFrequencyHours = hours)
        viewModelScope.launch {
            repository.updateReminderFrequencyHours(hours)
        }
    }

    private fun validateTarget(targetText: String): String? {
        if (targetText.isBlank()) return "Hydration target cannot be empty."
        val target = targetText.toIntOrNull() ?: return "Enter a valid numeric amount."
        if (target < 500) return "Target should be at least 500 mL."
        if (target > 5000) return "Target cannot exceed 5000 mL."
        return null
    }

    fun saveProfile() {
        val state = _uiState.value
        val validationError = validateTarget(state.hydrationTargetMlText)
        if (validationError != null) {
            _uiState.value = state.copy(targetValidationError = validationError)
            return
        }

        val targetMl = state.hydrationTargetMlText.toInt()
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            repository.saveProfile(
                nickname = state.nickname,
                activityType = state.activityType,
                preferredLanguage = state.preferredLanguage,
                hydrationTargetMl = targetMl
            )
            repository.updateHydrationRemindersEnabled(state.remindersEnabled)
            repository.updateReminderFrequencyHours(state.reminderFrequencyHours)
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                targetValidationError = null,
                successMessage = "Profile preferences saved successfully!"
            )
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetProfileToDefaults()
            _uiState.value = ProfileUiState(
                nickname = "",
                activityType = "Regular commuting",
                preferredLanguage = "English",
                hydrationTargetMlText = "2500",
                remindersEnabled = true,
                reminderFrequencyHours = 2,
                successMessage = "Preferences reset to default values.",
                isLoaded = true
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
