package com.heathealth.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.model.RiskLevel
import com.heathealth.app.data.repository.HeatHealthRepository
import com.heathealth.app.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.time.Instant

class HeatAlertCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefsRepository = UserPreferencesRepository(context)
        val userPrefs = prefsRepository.userPreferencesFlow.first()

        if (!userPrefs.heatAlertsEnabled || !userPrefs.notificationsEnabled) {
            return Result.success()
        }

        val repository = HeatHealthRepository()
        val result = repository.fetchHeatRiskData(28.6139, 77.2090, "New Delhi, India")

        result.onSuccess { uiState ->
            val currentRisk = uiState.riskPrediction.level
            val thresholdLevel = RiskLevel.fromDisplayName(userPrefs.alertThreshold)

            if (currentRisk.ordinal >= thresholdLevel.ordinal) {
                if (!userPrefs.lastAlertedRiskLevel.equals(currentRisk.name, ignoreCase = true)) {
                    val alertTitle = "${currentRisk.displayName} Heat Risk Advisory"
                    val alertMessage = "Current heat risk is ${currentRisk.displayName} (${uiState.weatherData.feelsLikeCelsius}°C feels-like). Take appropriate precautions."

                    NotificationHelper.sendHeatAlertNotification(
                        context = context,
                        title = alertTitle,
                        message = alertMessage
                    )

                    val db = HeatHealthDatabase.getDatabase(context)
                    val alertLog = AlertLogEntity(
                        timestamp = System.currentTimeMillis(),
                        isoTimestamp = Instant.now().toString(),
                        title = alertTitle,
                        description = alertMessage,
                        riskLevel = currentRisk.displayName,
                        triggerSource = "Automated Background Check"
                    )
                    db.alertLogDao().insertAlert(alertLog)

                    prefsRepository.updateLastAlertedRiskLevel(currentRisk.name)
                }
            }
        }

        return Result.success()
    }
}
