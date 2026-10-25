package com.heathealth.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.heathealth.app.data.local.datastore.UserPreferences
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.util.NotificationHelper
import kotlinx.coroutines.flow.first

object HeatAlertEvaluator {
    fun shouldTriggerAlert(
        userPrefs: UserPreferences,
        currentRisk: RiskLevel,
        nowMs: Long = System.currentTimeMillis()
    ): Boolean {
        if (!userPrefs.heatAlertsEnabled || !userPrefs.notificationsEnabled) {
            return false
        }

        val thresholdLevel = RiskLevel.fromDisplayName(userPrefs.alertThreshold)
        if (currentRisk.ordinal < thresholdLevel.ordinal) {
            return false
        }

        val cooldownMs = userPrefs.reminderFrequencyHours * 3600 * 1000L
        val timeSinceLastAlert = nowMs - userPrefs.lastAlertedTimestamp
        val isRiskChanged = !userPrefs.lastAlertedRiskLevel.equals(currentRisk.name, ignoreCase = true)
        val isCooldownExpired = timeSinceLastAlert >= cooldownMs

        return isRiskChanged || isCooldownExpired
    }
}

class HeatAlertCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefsRepository = UserPreferencesRepository(context)
        val userPrefs = prefsRepository.userPreferencesFlow.first()

        val repository = HeatHealthRepository()
        val result = repository.fetchHeatRiskData(28.6139, 77.2090, "New Delhi, India")

        result.onSuccess { uiState ->
            val currentRisk = uiState.riskPrediction.level
            val nowMs = System.currentTimeMillis()

            if (HeatAlertEvaluator.shouldTriggerAlert(userPrefs, currentRisk, nowMs)) {
                val alertTitle = "${currentRisk.displayName} Heat Risk Advisory"
                val feelsLike = uiState.weatherData.feelsLikeCelsius ?: uiState.weatherData.temperatureCelsius ?: 32
                val alertMessage = "Current heat risk is ${currentRisk.displayName} (${feelsLike}°C feels-like). Take appropriate precautions."

                NotificationHelper.sendHeatAlertNotification(
                    context = context,
                    title = alertTitle,
                    message = alertMessage
                )

                // Log locally and sync with backend
                repository.logAlert(
                    title = alertTitle,
                    description = alertMessage,
                    riskLevel = currentRisk.displayName,
                    triggerSource = "Automated Background Check",
                    recommendations = uiState.recommendations.map { it.title }
                )

                prefsRepository.updateLastAlertedRiskLevel(currentRisk.name)
                prefsRepository.updateLastAlertedTimestamp(nowMs)
            }
        }

        return Result.success()
    }
}
