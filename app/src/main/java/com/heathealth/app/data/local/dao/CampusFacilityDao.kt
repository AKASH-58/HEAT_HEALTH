package com.heathealth.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathealth.app.data.local.entity.CampusFacilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusFacilityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacilities(facilities: List<CampusFacilityEntity>)

    @Query("SELECT * FROM campus_facilities ORDER BY isVerified DESC, name ASC")
    fun getAllFacilities(): Flow<List<CampusFacilityEntity>>

    @Query("SELECT * FROM campus_facilities WHERE collegeId = :collegeId ORDER BY isVerified DESC, name ASC")
    fun getFacilitiesByCollege(collegeId: String): Flow<List<CampusFacilityEntity>>

    @Query("SELECT * FROM campus_facilities WHERE collegeId = :collegeId AND category = :category ORDER BY isVerified DESC, name ASC")
    fun getFacilitiesByCollegeAndCategory(collegeId: String, category: String): Flow<List<CampusFacilityEntity>>

    @Query("SELECT * FROM campus_facilities WHERE id = :id LIMIT 1")
    suspend fun getFacilityById(id: String): CampusFacilityEntity?

    @Query("DELETE FROM campus_facilities")
    suspend fun clearAllFacilities()
}
