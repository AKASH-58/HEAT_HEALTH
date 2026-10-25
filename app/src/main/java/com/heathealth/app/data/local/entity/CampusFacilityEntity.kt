package com.heathealth.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campus_facilities")
data class CampusFacilityEntity(
    @PrimaryKey
    val id: String,
    val collegeId: String = "col_iisc",
    val name: String,
    val category: String,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val buildingLocation: String,
    val openingHours: String?,
    val accessibilityInfo: String?,
    val status: String = "Operational",
    val isVerified: Boolean,
    val lastVerifiedTimestamp: String,
    val createdBy: String = "Campus Member",
    val updatedBy: String = "Campus Member",
    val updatedTimestamp: String = "",
    val contactInfo: String?
)
