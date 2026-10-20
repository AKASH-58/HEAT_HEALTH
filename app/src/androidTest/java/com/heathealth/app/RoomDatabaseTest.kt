package com.heathealth.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heathealth.app.data.local.HeatHealthDatabase
import com.heathealth.app.data.local.entity.StudentProfileEntity
import com.heathealth.app.data.local.entity.WaterIntakeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var db: HeatHealthDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, HeatHealthDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndGetStudentProfile() = runBlocking {
        val dao = db.studentProfileDao()
        val profile = StudentProfileEntity(
            id = 1,
            nickname = "Alex",
            activityType = "Frequent outdoor activities",
            preferredLanguage = "English",
            hydrationTargetMl = 3000
        )
        dao.insertOrUpdateProfile(profile)

        val retrieved = dao.getProfile().first()
        assertNotNull(retrieved)
        assertEquals("Alex", retrieved?.nickname)
        assertEquals(3000, retrieved?.hydrationTargetMl)
    }

    @Test
    fun testWaterIntakeInsertAndQuery() = runBlocking {
        val dao = db.waterIntakeDao()
        val entry1 = WaterIntakeEntity(amountMl = 250, timestamp = System.currentTimeMillis())
        val entry2 = WaterIntakeEntity(amountMl = 500, timestamp = System.currentTimeMillis())

        dao.insertWaterIntake(entry1)
        dao.insertWaterIntake(entry2)

        val list = dao.getAllWaterIntake().first()
        assertEquals(2, list.size)
    }
}
