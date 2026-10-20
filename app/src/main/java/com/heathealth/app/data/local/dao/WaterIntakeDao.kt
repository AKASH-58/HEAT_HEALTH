package com.heathealth.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathealth.app.data.local.entity.WaterIntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {

    @Query("SELECT * FROM water_intake ORDER BY timestamp DESC")
    fun getAllWaterIntake(): Flow<List<WaterIntakeEntity>>

    @Query("SELECT * FROM water_intake WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    fun getWaterIntakeForDateRange(startTimestamp: Long, endTimestamp: Long): Flow<List<WaterIntakeEntity>>

    @Query("SELECT * FROM water_intake WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    suspend fun getWaterIntakeForDateRangeSync(startTimestamp: Long, endTimestamp: Long): List<WaterIntakeEntity>

    @Query("SELECT SUM(amountMl) FROM water_intake WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    fun getDailyTotalMl(startTimestamp: Long, endTimestamp: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterIntake(entry: WaterIntakeEntity): Long

    @Delete
    suspend fun deleteWaterIntake(entry: WaterIntakeEntity): Int

    @Query("DELETE FROM water_intake WHERE id = :id")
    suspend fun deleteWaterIntakeById(id: Long): Int

    @Query("DELETE FROM water_intake")
    suspend fun deleteAllWaterIntake(): Int
}
