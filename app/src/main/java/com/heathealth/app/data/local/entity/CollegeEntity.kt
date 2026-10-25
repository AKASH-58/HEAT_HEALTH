package com.heathealth.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "colleges")
data class CollegeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isVerified: Boolean,
    val dataSource: String,
    val createdAt: String,
    val updatedAt: String
)
