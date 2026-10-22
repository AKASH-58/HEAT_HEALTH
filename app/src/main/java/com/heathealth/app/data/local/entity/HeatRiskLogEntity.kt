package com.heathealth.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heat_risk_logs")
data class HeatRiskLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isoTimestamp: String,
    val riskLevel: String,
    val riskScore: Double?,
    val temperatureCelsius: Int,
    val humidityPercent: Int,
    val feelsLikeCelsius: Int,
    val locationLabel: String
)
