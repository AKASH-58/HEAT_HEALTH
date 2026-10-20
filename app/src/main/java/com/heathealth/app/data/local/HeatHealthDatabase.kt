package com.heathealth.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.heathealth.app.data.local.dao.StudentProfileDao
import com.heathealth.app.data.local.dao.WaterIntakeDao
import com.heathealth.app.data.local.entity.StudentProfileEntity
import com.heathealth.app.data.local.entity.WaterIntakeEntity

@Database(
    entities = [StudentProfileEntity::class, WaterIntakeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HeatHealthDatabase : RoomDatabase() {

    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun waterIntakeDao(): WaterIntakeDao

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
