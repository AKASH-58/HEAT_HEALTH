package com.heathealth.app

import com.heathealth.app.data.local.entity.WaterIntakeEntity
import com.heathealth.app.data.repository.WaterIntakeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WaterIntakeRepositoryTest {

    private lateinit var fakeDao: FakeTestWaterIntakeDao
    private lateinit var repository: WaterIntakeRepository

    @Before
    fun setUp() {
        fakeDao = FakeTestWaterIntakeDao()
        repository = WaterIntakeRepository(fakeDao)
    }

    @Test
    fun testDateBoundaryFiltering() = runBlocking {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val zoneId = ZoneId.systemDefault()
        val yesterdayTime = yesterday.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        val todayTime = today.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

        repository.addWaterIntake(amountMl = 500, note = "Yesterday intake", timestamp = yesterdayTime)
        repository.addWaterIntake(amountMl = 250, note = "Today intake 1", timestamp = todayTime)
        repository.addWaterIntake(amountMl = 500, note = "Today intake 2", timestamp = todayTime + 1000)

        // Verify today's entries only contain today's intake
        val todayEntries = repository.getTodayWaterEntries(today).first()
        assertEquals(2, todayEntries.size)

        // Verify today's total sums only today's intake
        val todayTotal = repository.getTodayTotalMl(today).first()
        assertEquals(750, todayTotal)

        // Verify historical entries preserve all 3 records
        val allHistory = repository.getAllWaterIntakeHistory().first()
        assertEquals(3, allHistory.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRejectZeroOrNegativeAmount() {
        runBlocking {
            repository.addWaterIntake(0)
        }
    }

    @Test
    fun testDeleteWaterIntake() = runBlocking {
        repository.addWaterIntake(250)
        val history = repository.getAllWaterIntakeHistory().first()
        assertEquals(1, history.size)

        repository.deleteWaterIntake(history[0])

        val updated = repository.getAllWaterIntakeHistory().first()
        assertTrue(updated.isEmpty())
    }
}
