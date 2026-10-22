package com.heathealth.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heathealth.app.data.local.dao.AlertLogDao
import com.heathealth.app.data.local.dao.HeatRiskLogDao
import com.heathealth.app.data.local.dao.StudentProfileDao
import com.heathealth.app.data.local.dao.WaterIntakeDao
import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import com.heathealth.app.data.local.entity.StudentProfileEntity
import com.heathealth.app.data.local.entity.WaterIntakeEntity

@Database(
    entities = [
        StudentProfileEntity::class,
        WaterIntakeEntity::class,
        HeatRiskLogEntity::class,
        AlertLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class HeatHealthDatabase : RoomDatabase() {

    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun heatRiskLogDao(): HeatRiskLogDao
    abstract fun alertLogDao(): AlertLogDao

    companion object {
        @Volatile
        private var INSTANCE: HeatHealthDatabase? = null

        fun getDatabase(context: Context): HeatHealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeatHealthDatabase::class.java,
                    "heat_health_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
