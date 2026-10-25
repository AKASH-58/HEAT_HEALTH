package com.heathealth.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeatRiskLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HeatRiskLogEntity): Long

    @Query("SELECT * FROM heat_risk_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 10): Flow<List<HeatRiskLogEntity>>

    @Query("SELECT * FROM heat_risk_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<HeatRiskLogEntity>>

    @Query("SELECT * FROM heat_risk_logs WHERE timestamp >= :minTimestamp ORDER BY timestamp DESC")
    fun getLogsSince(minTimestamp: Long): Flow<List<HeatRiskLogEntity>>

    @Query("SELECT * FROM heat_risk_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLog(): HeatRiskLogEntity?

    @Query("DELETE FROM heat_risk_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM heat_risk_logs")
    suspend fun clearAllLogs()
}
