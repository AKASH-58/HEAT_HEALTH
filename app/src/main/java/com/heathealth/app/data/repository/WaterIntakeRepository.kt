package com.heathealth.app.data.repository

import com.heathealth.app.data.local.dao.WaterIntakeDao
import com.heathealth.app.data.local.entity.WaterIntakeEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class WaterIntakeRepository(
    private val waterIntakeDao: WaterIntakeDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getTodayStartAndEndTimestamps(date: LocalDate = LocalDate.now()): Pair<Long, Long> {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
        return Pair(startOfDay, endOfDay)
    }

    fun getTodayWaterEntries(date: LocalDate = LocalDate.now()): Flow<List<WaterIntakeEntity>> {
        val (start, end) = getTodayStartAndEndTimestamps(date)
        return waterIntakeDao.getWaterIntakeForDateRange(start, end)
    }

    fun getTodayTotalMl(date: LocalDate = LocalDate.now()): Flow<Int> {
        val (start, end) = getTodayStartAndEndTimestamps(date)
        return waterIntakeDao.getDailyTotalMl(start, end).map { total -> total ?: 0 }
    }

    fun getAllWaterIntakeHistory(): Flow<List<WaterIntakeEntity>> {
        return waterIntakeDao.getAllWaterIntake()
    }

    suspend fun addWaterIntake(amountMl: Int, note: String? = null, timestamp: Long = System.currentTimeMillis()): Long = withContext(ioDispatcher) {
        require(amountMl > 0) { "Water intake amount must be greater than zero." }
        val entry = WaterIntakeEntity(
            amountMl = amountMl,
            timestamp = timestamp,
            note = note
        )
        waterIntakeDao.insertWaterIntake(entry)
    }

    suspend fun deleteWaterIntake(entry: WaterIntakeEntity) = withContext(ioDispatcher) {
        waterIntakeDao.deleteWaterIntake(entry)
    }

    suspend fun deleteWaterIntakeById(id: Long) = withContext(ioDispatcher) {
        waterIntakeDao.deleteWaterIntakeById(id)
    }
}
