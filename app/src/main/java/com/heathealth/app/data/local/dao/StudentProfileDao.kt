package com.heathealth.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathealth.app.data.local.entity.StudentProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentProfileDao {

    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): StudentProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: StudentProfileEntity): Long

    @Query("DELETE FROM student_profile WHERE id = 1")
    suspend fun deleteProfile(): Int
}
