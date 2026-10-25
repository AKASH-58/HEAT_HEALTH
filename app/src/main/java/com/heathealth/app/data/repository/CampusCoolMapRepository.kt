package com.heathealth.app.data.repository

import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.heathealth.app.data.local.dao.CampusFacilityDao
import com.heathealth.app.data.local.dao.CollegeDao
import com.heathealth.app.data.model.CampusFacility
import com.heathealth.app.data.model.College
import com.heathealth.app.data.model.FacilityCategory
import com.heathealth.app.network.BackendApiConfig
import com.heathealth.app.network.HeatHealthApiService
import com.heathealth.app.network.dto.AddFacilityRequestDto
import com.heathealth.app.network.dto.CampusFacilitiesResponseDto
import com.heathealth.app.network.dto.CampusFacilityDto
import com.heathealth.app.network.dto.CollegeDto
import com.heathealth.app.network.dto.ReportStatusRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class CampusCoolMapRepository(
    private var apiService: HeatHealthApiService = BackendApiConfig.createApiService(),
    private val campusFacilityDao: CampusFacilityDao? = null,
    private val collegeDao: CollegeDao? = null,
    private val enableCandidateFailover: Boolean = true
) {

    suspend fun fetchColleges(query: String? = null, city: String? = null): Result<List<College>> {
        try {
            val response = apiService.getColleges(query, city)
            val colleges = response.colleges.map { mapCollegeDtoToModel(it) }
            persistColleges(colleges)
            return Result.success(colleges)
        } catch (e: HttpException) {
            val code = e.code()
            return Result.failure(IOException("Server error ($code) fetching colleges: ${e.message()}", e))
        } catch (initialErr: Exception) {
            if (enableCandidateFailover) {
                for (candidateUrl in BackendApiConfig.getCandidateBaseUrls()) {
                    try {
                        val service = BackendApiConfig.createApiService(candidateUrl)
                        val response = service.getColleges(query, city)
                        BackendApiConfig.baseUrl = candidateUrl
                        this.apiService = service
                        val colleges = response.colleges.map { mapCollegeDtoToModel(it) }
                        persistColleges(colleges)
                        return Result.success(colleges)
                    } catch (_: Exception) {
                    }
                }
            }

            val cached = getCachedColleges().firstOrNull() ?: emptyList()
            if (cached.isNotEmpty()) {
                val q = query?.trim() ?: ""
                val filtered = if (q.isNotEmpty()) {
                    cached.filter {
                        it.name.contains(q, ignoreCase = true) ||
                                it.city.contains(q, ignoreCase = true) ||
                                it.address.contains(q, ignoreCase = true)
                    }
                } else cached
                return Result.success(filtered)
            }

            val details = initialErr.localizedMessage ?: "Unable to connect to backend server"
            return Result.failure(IOException("Unable to connect to FastAPI college directory server at ${BackendApiConfig.baseUrl}. ($details)", initialErr))
        }
    }

    suspend fun fetchCampusFacilities(collegeId: String = "col_iisc", categoryFilter: String? = null): Result<Pair<List<CampusFacility>, String>> {
        try {
            val response = apiService.getCampusFacilities(collegeId, categoryFilter)
            val facilities = response.facilities.map { mapDtoToModel(it) }
            persistFacilities(facilities)
            return Result.success(Pair(facilities, response.lastUpdated))
        } catch (e: HttpException) {
            val code = e.code()
            return Result.failure(IOException("Server error ($code) fetching campus facilities: ${e.message()}", e))
        } catch (e: JsonSyntaxException) {
            return Result.failure(IllegalArgumentException("Received malformed JSON data from campus facilities API.", e))
        } catch (e: JsonParseException) {
            return Result.failure(IllegalArgumentException("Received invalid JSON payload structure from campus facilities API.", e))
        } catch (initialErr: Exception) {
            if (!enableCandidateFailover) {
                val details = initialErr.localizedMessage ?: "Unable to connect to backend server"
                return Result.failure(IOException("Unable to connect to FastAPI backend. ($details)", initialErr))
            }

            var lastException: Exception = initialErr
            for (candidateUrl in BackendApiConfig.getCandidateBaseUrls()) {
                try {
                    val service = BackendApiConfig.createApiService(candidateUrl)
                    val response = service.getCampusFacilities(collegeId, categoryFilter)
                    BackendApiConfig.baseUrl = candidateUrl
                    this.apiService = service
                    val facilities = response.facilities.map { mapDtoToModel(it) }
                    persistFacilities(facilities)
                    return Result.success(Pair(facilities, response.lastUpdated))
                } catch (e: Exception) {
                    lastException = e
                }
            }

            val cached = getCachedFacilitiesByCollege(collegeId).firstOrNull() ?: emptyList()
            if (cached.isNotEmpty()) {
                val cat = categoryFilter?.trim() ?: ""
                val filtered = if (cat.isNotEmpty()) {
                    cached.filter { it.category.displayName.equals(cat, ignoreCase = true) }
                } else cached
                return Result.success(Pair(filtered, "Cached Offline"))
            }

            val details = lastException.localizedMessage ?: "Unable to connect to backend server"
            return Result.failure(IOException("Unable to connect to FastAPI backend at ${BackendApiConfig.baseUrl}. Ensure FastAPI is running on host 0.0.0.0:8000. ($details)", lastException))
        }
    }

    suspend fun addFacility(
        collegeId: String,
        name: String,
        category: String,
        description: String,
        latitude: Double?,
        longitude: Double?,
        buildingLocation: String,
        openingHours: String?,
        accessibilityInfo: String?,
        contactInfo: String?
    ): Result<CampusFacility> {
        try {
            val request = AddFacilityRequestDto(
                collegeId = collegeId,
                name = name,
                category = category,
                description = description,
                latitude = latitude,
                longitude = longitude,
                buildingLocation = buildingLocation,
                openingHours = openingHours,
                accessibilityInfo = accessibilityInfo,
                contactInfo = contactInfo
            )
            val dto = apiService.addCampusFacility(request)
            val facility = mapDtoToModel(dto)
            persistFacilities(listOf(facility))
            return Result.success(facility)
        } catch (e: Exception) {
            val details = e.localizedMessage ?: "Server submission failed"
            return Result.failure(IOException("Failed to submit facility to campus server: $details", e))
        }
    }

    suspend fun updateFacilityStatus(facilityId: String, newStatus: String, statusNote: String?): Result<CampusFacility> {
        try {
            val request = ReportStatusRequestDto(
                facilityId = facilityId,
                status = newStatus,
                statusNote = statusNote,
                reportedBy = "Campus Member"
            )
            val dto = apiService.updateFacilityStatus(facilityId, request)
            val facility = mapDtoToModel(dto)
            persistFacilities(listOf(facility))
            return Result.success(facility)
        } catch (e: Exception) {
            val details = e.localizedMessage ?: "Status update failed"
            return Result.failure(IOException("Failed to update facility status on server: $details", e))
        }
    }

    private suspend fun persistFacilities(facilities: List<CampusFacility>) {
        campusFacilityDao?.let { dao ->
            try {
                dao.insertFacilities(facilities.map { it.toEntity() })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun persistColleges(colleges: List<College>) {
        collegeDao?.let { dao ->
            try {
                dao.insertColleges(colleges.map { it.toEntity() })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCachedFacilitiesByCollege(collegeId: String): Flow<List<CampusFacility>> {
        return campusFacilityDao?.getFacilitiesByCollege(collegeId)?.map { entities ->
            entities.map { CampusFacility.fromEntity(it) }
        } ?: flowOf(emptyList())
    }

    fun getCachedColleges(): Flow<List<College>> {
        return collegeDao?.getAllColleges()?.map { entities ->
            entities.map { College.fromEntity(it) }
        } ?: flowOf(emptyList())
    }

    private fun mapDtoToModel(dto: CampusFacilityDto): CampusFacility {
        return CampusFacility(
            id = dto.id,
            collegeId = dto.collegeId,
            name = dto.name,
            category = FacilityCategory.fromString(dto.category),
            description = dto.description,
            latitude = dto.latitude,
            longitude = dto.longitude,
            buildingLocation = dto.buildingLocation,
            openingHours = dto.openingHours,
            accessibilityInfo = dto.accessibilityInfo,
            status = dto.status,
            isVerified = dto.isVerified,
            lastVerifiedTimestamp = dto.lastVerifiedTimestamp,
            createdBy = dto.createdBy,
            updatedBy = dto.updatedBy,
            updatedTimestamp = dto.updatedTimestamp,
            contactInfo = dto.contactInfo
        )
    }

    private fun mapCollegeDtoToModel(dto: CollegeDto): College {
        return College(
            id = dto.id,
            name = dto.name,
            city = dto.city,
            address = dto.address,
            latitude = dto.latitude,
            longitude = dto.longitude,
            isVerified = dto.isVerified,
            dataSource = dto.dataSource,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }

    fun getDemoColleges(): List<College> {
        return listOf(
            College(
                id = "col_iisc",
                name = "Indian Institute of Science (IISc)",
                city = "Bengaluru",
                address = "CV Raman Rd, Mathikere, Bengaluru, Karnataka 560012",
                latitude = 13.0197,
                longitude = 77.5662,
                isVerified = true,
                dataSource = "Verified Educational Directory (Demo Mode)"
            ),
            College(
                id = "col_rvce",
                name = "RV College of Engineering (RVCE)",
                city = "Bengaluru",
                address = "RV Vidyanikethan Post, Mysore Rd, Bengaluru 560059",
                latitude = 12.9237,
                longitude = 77.4987,
                isVerified = true,
                dataSource = "Verified Educational Directory (Demo Mode)"
            ),
            College(
                id = "col_rnsit",
                name = "RNS Institute of Technology (RNSIT)",
                city = "Bengaluru",
                address = "Uttarahalli-Kengeri Main Road, Channasandra, RR Nagar, Bengaluru 560098",
                latitude = 12.9007,
                longitude = 77.5186,
                isVerified = true,
                dataSource = "Verified Educational Directory (Demo Mode)"
            )
        )
    }

    fun getDemoSampleFacilities(): List<CampusFacility> {
        return listOf(
            CampusFacility(
                id = "fac_demo_iisc_w1",
                collegeId = "col_iisc",
                name = "[Demo] IISc Library Water Fountain",
                category = FacilityCategory.DRINKING_WATER,
                description = "Demo dataset purified water dispenser.",
                latitude = 13.0199,
                longitude = 77.5670,
                buildingLocation = "Central Library Entrance, IISc Campus",
                openingHours = "08:00 AM - 10:00 PM",
                accessibilityInfo = "Wheelchair accessible",
                status = "Operational",
                isVerified = false,
                lastVerifiedTimestamp = "Demo Record",
                createdBy = "Demo Dataset",
                updatedBy = "Demo Dataset",
                updatedTimestamp = "2025-05-15T12:00:00Z",
                contactInfo = null
            )
        )
    }
}
