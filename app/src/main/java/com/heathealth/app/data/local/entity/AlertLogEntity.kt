package com.heathealth.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_logs")
data class AlertLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isoTimestamp: String,
    val title: String,
    val description: String,
    val riskLevel: String,
    val triggerSource: String = "Local Risk Monitor"
)
