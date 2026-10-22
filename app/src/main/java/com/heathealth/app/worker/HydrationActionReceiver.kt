package com.heathealth.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.repository.WaterIntakeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HydrationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_HYDRATED = "com.heathealth.app.ACTION_MARK_HYDRATED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MARK_HYDRATED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = HeatHealthDatabase.getDatabase(context)
                    val repository = WaterIntakeRepository(db.waterIntakeDao())
                    repository.addWaterIntake(250, "Completed from Hydration Reminder")

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "Hydration logged (+250 mL)!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
