package com.heathealth.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    const val HYDRATION_WORK_NAME = "hydration_reminder_work"
    const val HEAT_ALERT_WORK_NAME = "heat_alert_check_work"

    fun scheduleHydrationReminders(context: Context, intervalHours: Int) {
        try {
            val workManager = WorkManager.getInstance(context)
            val hours = intervalHours.coerceAtLeast(1).toLong()

            val request = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                hours, TimeUnit.HOURS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                HYDRATION_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun cancelHydrationReminders(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(HYDRATION_WORK_NAME)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun scheduleHeatAlertChecks(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)

            val request = PeriodicWorkRequestBuilder<HeatAlertCheckWorker>(
                2, TimeUnit.HOURS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                HEAT_ALERT_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun cancelHeatAlertChecks(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(HEAT_ALERT_WORK_NAME)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
