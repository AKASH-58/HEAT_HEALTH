package com.heathealth.app.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.heathealth.app.MainActivity
import com.heathealth.app.worker.HydrationActionReceiver

object NotificationHelper {

    const val HYDRATION_CHANNEL_ID = "hydration_reminders_channel"
    const val HEAT_ALERTS_CHANNEL_ID = "heat_alerts_channel"

    const val HYDRATION_NOTIFICATION_ID = 1001
    const val HEAT_ALERT_NOTIFICATION_ID = 2001

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val hydrationChannel = NotificationChannel(
            HYDRATION_CHANNEL_ID,
            "Hydration Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Periodic reminders to stay hydrated during warm student activities."
        }

        val alertChannel = NotificationChannel(
            HEAT_ALERTS_CHANNEL_ID,
            "Heat Risk Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "High or Extreme thermal risk warning alerts."
        }

        manager.createNotificationChannel(hydrationChannel)
        manager.createNotificationChannel(alertChannel)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun sendHydrationReminder(context: Context, title: String, message: String) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markHydratedIntent = Intent(context, HydrationActionReceiver::class.java).apply {
            action = HydrationActionReceiver.ACTION_MARK_HYDRATED
        }
        val markHydratedPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            markHydratedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HYDRATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Mark as Hydrated (+250mL)",
                markHydratedPendingIntent
            )
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(HYDRATION_NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    fun sendHeatAlertNotification(context: Context, title: String, message: String) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "alerts")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HEAT_ALERTS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(HEAT_ALERT_NOTIFICATION_ID, notification)
    }
}
