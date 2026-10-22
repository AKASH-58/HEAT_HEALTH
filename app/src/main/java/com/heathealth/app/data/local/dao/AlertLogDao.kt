package com.heathealth.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathealth.app.data.local.entity.AlertLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertLogEntity): Long

    @Query("SELECT * FROM alert_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAlerts(limit: Int = 10): Flow<List<AlertLogEntity>>

    @Query("SELECT * FROM alert_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAlert(): AlertLogEntity?

    @Query("DELETE FROM alert_logs")
    suspend fun clearAllAlerts()
}
