package com.heathealth.app.data.model

import com.heathealth.app.data.local.entity.StudentProfileEntity

data class ProfileUiState(
    val nickname: String = "",
    val activityType: String = "Regular commuting",
    val preferredLanguage: String = "English",
    val hydrationTargetMlText: String = "2500",
    val remindersEnabled: Boolean = true,
    val reminderFrequencyHours: Int = 2,
    val isSaving: Boolean = false,
    val targetValidationError: String? = null,
    val successMessage: String? = null,
    val isLoaded: Boolean = false
) {
    companion object {
        fun fromEntity(
            entity: StudentProfileEntity,
            remindersEnabled: Boolean = true,
            reminderFrequencyHours: Int = 2
        ): ProfileUiState {
            return ProfileUiState(
                nickname = entity.nickname,
                activityType = entity.activityType,
                preferredLanguage = entity.preferredLanguage,
                hydrationTargetMlText = entity.hydrationTargetMl.toString(),
                remindersEnabled = remindersEnabled,
                reminderFrequencyHours = reminderFrequencyHours,
                isLoaded = true
            )
        }
    }
}
