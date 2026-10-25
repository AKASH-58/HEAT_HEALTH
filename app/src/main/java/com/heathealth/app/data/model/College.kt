package com.heathealth.app.data.model

import com.heathealth.app.data.local.entity.CollegeEntity

data class College(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isVerified: Boolean = true,
    val dataSource: String = "Verified Directory",
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun toEntity(): CollegeEntity {
        return CollegeEntity(
            id = id,
            name = name,
            city = city,
            address = address,
            latitude = latitude,
            longitude = longitude,
            isVerified = isVerified,
            dataSource = dataSource,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromEntity(entity: CollegeEntity): College {
            return College(
                id = entity.id,
                name = entity.name,
                city = entity.city,
                address = entity.address,
                latitude = entity.latitude,
                longitude = entity.longitude,
                isVerified = entity.isVerified,
                dataSource = entity.dataSource,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
}
