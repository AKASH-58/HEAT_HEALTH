package com.heathealth.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: Int = 1,
    val nickname: String = "",
    val activityType: String = "Regular commuting",
    val preferredLanguage: String = "English",
    val hydrationTargetMl: Int = 2500,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
