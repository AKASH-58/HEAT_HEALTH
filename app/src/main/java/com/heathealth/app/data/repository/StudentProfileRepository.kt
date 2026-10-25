package com.heathealth.app.data.repository

import com.heathealth.app.data.local.dao.StudentProfileDao
import com.heathealth.app.data.local.datastore.UserPreferences
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.local.entity.StudentProfileEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StudentProfileRepository(
    private val profileDao: StudentProfileDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val profileFlow: Flow<StudentProfileEntity> = profileDao.getProfile().map { entity ->
        entity ?: StudentProfileEntity()
    }

    val userPreferencesFlow: Flow<UserPreferences> = userPreferencesRepository.userPreferencesFlow

    suspend fun saveProfile(
        nickname: String,
        activityType: String,
        preferredLanguage: String,
        hydrationTargetMl: Int
    ) = withContext(ioDispatcher) {
        val current = profileDao.getProfileSync() ?: StudentProfileEntity()
        val updated = current.copy(
            nickname = nickname.trim(),
            activityType = activityType,
            preferredLanguage = preferredLanguage,
            hydrationTargetMl = hydrationTargetMl,
            updatedAt = System.currentTimeMillis()
        )
        profileDao.insertOrUpdateProfile(updated)

        val langCode = when (preferredLanguage.lowercase()) {
            "hindi", "hi" -> "hi"
            "kannada", "kn" -> "kn"
            "telugu", "te" -> "te"
            "tamil", "ta" -> "ta"
            "malayalam", "ml" -> "ml"
            else -> "en"
        }
        userPreferencesRepository.updateSelectedLanguage(langCode)
    }

    suspend fun resetProfileToDefaults() = withContext(ioDispatcher) {
        val defaultProfile = StudentProfileEntity(
            id = 1,
            nickname = "",
            activityType = "Regular commuting",
            preferredLanguage = "English",
            hydrationTargetMl = 2500,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        profileDao.insertOrUpdateProfile(defaultProfile)
        userPreferencesRepository.updateHydrationRemindersEnabled(true)
        userPreferencesRepository.updateReminderFrequencyHours(2)
        userPreferencesRepository.updateNotificationsEnabled(true)
    }

    suspend fun updateHydrationRemindersEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        userPreferencesRepository.updateHydrationRemindersEnabled(enabled)
    }

    suspend fun updateReminderFrequencyHours(hours: Int) = withContext(ioDispatcher) {
        userPreferencesRepository.updateReminderFrequencyHours(hours)
    }
}
