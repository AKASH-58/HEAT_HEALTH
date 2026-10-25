package com.heathealth.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heathealth.app.data.local.entity.CollegeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollegeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColleges(colleges: List<CollegeEntity>)

    @Query("SELECT * FROM colleges ORDER BY isVerified DESC, name ASC")
    fun getAllColleges(): Flow<List<CollegeEntity>>

    @Query("SELECT * FROM colleges WHERE name LIKE '%' || :query || '%' OR city LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY isVerified DESC, name ASC")
    fun searchColleges(query: String): Flow<List<CollegeEntity>>

    @Query("SELECT * FROM colleges WHERE id = :id LIMIT 1")
    suspend fun getCollegeById(id: String): CollegeEntity?

    @Query("DELETE FROM colleges")
    suspend fun clearAllColleges()
}
