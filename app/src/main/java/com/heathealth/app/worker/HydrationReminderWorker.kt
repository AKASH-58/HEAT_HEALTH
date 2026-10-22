package com.heathealth.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.heathealth.app.data.local.datastore.UserPreferencesRepository
import com.heathealth.app.util.NotificationHelper
import kotlinx.coroutines.flow.first

class HydrationReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefsRepository = UserPreferencesRepository(context)
        val userPrefs = prefsRepository.userPreferencesFlow.first()

        if (userPrefs.hydrationRemindersEnabled && userPrefs.notificationsEnabled) {
            NotificationHelper.sendHydrationReminder(
                context = context,
                title = "Hydration Reminder",
                message = "Time for a water break! Stay hydrated to maintain your energy and stay safe in the heat."
            )
        }

        return Result.success()
    }
}
