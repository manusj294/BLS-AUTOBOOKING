package com.blsautobooking.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blsautobooking.app.data.model.Applicant
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicantDao {
    @Query("SELECT * FROM applicants ORDER BY name ASC")
    fun getAllApplicants(): Flow<List<Applicant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(applicant: Applicant)

    @Query("DELETE FROM applicants WHERE id = :applicantId")
    suspend fun deleteById(applicantId: Long)

    @Query("SELECT * FROM applicants WHERE isActive = 1")
    fun getActiveApplicants(): Flow<List<Applicant>>
}

