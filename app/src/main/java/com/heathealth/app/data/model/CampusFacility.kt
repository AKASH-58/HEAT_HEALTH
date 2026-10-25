package com.heathealth.app.data.model

import com.heathealth.app.data.local.entity.CampusFacilityEntity

enum class FacilityCategory(val displayName: String, val headerTitle: String) {
    DRINKING_WATER("Drinking Water", "Drinking-Water Points"),
    SHADED_AREA("Shaded Area", "Shaded Outdoor Areas"),
    COOL_INDOOR_SPACE("Cool Indoor Space", "Cool Indoor Study Spaces"),
    FIRST_AID("First Aid", "First-Aid & Medical Facilities");

    companion object {
        fun fromString(value: String): FacilityCategory {
            return entries.firstOrNull {
                it.displayName.equals(value, ignoreCase = true) ||
                        it.name.equals(value, ignoreCase = true) ||
                        it.displayName.replace(" ", "").equals(value.replace(" ", ""), ignoreCase = true)
            } ?: COOL_INDOOR_SPACE
        }
    }
}

data class CampusFacility(
    val id: String,
    val collegeId: String = "col_iisc",
    val name: String,
    val category: FacilityCategory,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val buildingLocation: String,
    val openingHours: String? = null,
    val accessibilityInfo: String? = null,
    val status: String = "Operational",
    val isVerified: Boolean = true,
    val lastVerifiedTimestamp: String = "",
    val createdBy: String = "Campus Member",
    val updatedBy: String = "Campus Member",
    val updatedTimestamp: String = "",
    val contactInfo: String? = null
) {
    fun toEntity(): CampusFacilityEntity {
        return CampusFacilityEntity(
            id = id,
            collegeId = collegeId,
            name = name,
            category = category.displayName,
            description = description,
            latitude = latitude,
            longitude = longitude,
            buildingLocation = buildingLocation,
            openingHours = openingHours,
            accessibilityInfo = accessibilityInfo,
            status = status,
            isVerified = isVerified,
            lastVerifiedTimestamp = lastVerifiedTimestamp,
            createdBy = createdBy,
            updatedBy = updatedBy,
            updatedTimestamp = updatedTimestamp,
            contactInfo = contactInfo
        )
    }

    companion object {
        fun fromEntity(entity: CampusFacilityEntity): CampusFacility {
            return CampusFacility(
                id = entity.id,
                collegeId = entity.collegeId,
                name = entity.name,
                category = FacilityCategory.fromString(entity.category),
                description = entity.description,
                latitude = entity.latitude,
                longitude = entity.longitude,
                buildingLocation = entity.buildingLocation,
                openingHours = entity.openingHours,
                accessibilityInfo = entity.accessibilityInfo,
                status = entity.status,
                isVerified = entity.isVerified,
                lastVerifiedTimestamp = entity.lastVerifiedTimestamp,
                createdBy = entity.createdBy,
                updatedBy = entity.updatedBy,
                updatedTimestamp = entity.updatedTimestamp,
                contactInfo = entity.contactInfo
            )
        }
    }
}

data class CampusCoolMapUiState(
    val selectedCollege: College = College(
        id = "col_iisc",
        name = "Indian Institute of Science (IISc)",
        city = "Bengaluru",
        address = "CV Raman Rd, Bengaluru, Karnataka 560012",
        latitude = 12.9716,
        longitude = 77.5946,
        isVerified = true
    ),
    val collegesList: List<College> = emptyList(),
    val isCollegeSearchOpen: Boolean = false,
    val collegeSearchQuery: String = "",
    val facilities: List<CampusFacility> = emptyList(),
    val selectedCategory: FacilityCategory? = null,
    val searchQuery: String = "",
    val selectedFacility: CampusFacility? = null,
    val isMapView: Boolean = true,
    val isAddFacilityDialogOpen: Boolean = false,
    val isReportStatusDialogOpen: Boolean = false,
    val dataSource: DataSource = DataSource.UNAVAILABLE,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: String = ""
)
