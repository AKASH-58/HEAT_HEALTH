package com.heathealth.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heathealth.app.data.local.dao.AlertLogDao
import com.heathealth.app.data.local.dao.CampusFacilityDao
import com.heathealth.app.data.local.dao.CollegeDao
import com.heathealth.app.data.local.dao.HeatRiskLogDao
import com.heathealth.app.data.local.dao.StudentProfileDao
import com.heathealth.app.data.local.dao.WaterIntakeDao
import com.heathealth.app.data.local.entity.AlertLogEntity
import com.heathealth.app.data.local.entity.CampusFacilityEntity
import com.heathealth.app.data.local.entity.CollegeEntity
import com.heathealth.app.data.local.entity.HeatRiskLogEntity
import com.heathealth.app.data.local.entity.StudentProfileEntity
import com.heathealth.app.data.local.entity.WaterIntakeEntity

@Database(
    entities = [
        StudentProfileEntity::class,
        WaterIntakeEntity::class,
        HeatRiskLogEntity::class,
        AlertLogEntity::class,
        CampusFacilityEntity::class,
        CollegeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class HeatHealthDatabase : RoomDatabase() {

    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun heatRiskLogDao(): HeatRiskLogDao
    abstract fun alertLogDao(): AlertLogDao
    abstract fun campusFacilityDao(): CampusFacilityDao
    abstract fun collegeDao(): CollegeDao

    companion object {
        @Volatile
        private var INSTANCE: HeatHealthDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS campus_facilities (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL,
                        latitude REAL,
                        longitude REAL,
                        buildingLocation TEXT NOT NULL,
                        openingHours TEXT,
                        accessibilityInfo TEXT,
                        isVerified INTEGER NOT NULL,
                        lastVerifiedTimestamp TEXT NOT NULL,
                        contactInfo TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS colleges (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        city TEXT NOT NULL,
                        address TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        isVerified INTEGER NOT NULL,
                        dataSource TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE campus_facilities ADD COLUMN collegeId TEXT NOT NULL DEFAULT 'col_iisc'")
                db.execSQL("ALTER TABLE campus_facilities ADD COLUMN status TEXT NOT NULL DEFAULT 'Operational'")
                db.execSQL("ALTER TABLE campus_facilities ADD COLUMN createdBy TEXT NOT NULL DEFAULT 'Campus Member'")
                db.execSQL("ALTER TABLE campus_facilities ADD COLUMN updatedBy TEXT NOT NULL DEFAULT 'Campus Member'")
                db.execSQL("ALTER TABLE campus_facilities ADD COLUMN updatedTimestamp TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): HeatHealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeatHealthDatabase::class.java,
                    "heat_health_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
