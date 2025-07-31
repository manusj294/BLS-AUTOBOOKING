package com.bls.autobooking.data.dao

import androidx.room.*
import com.bls.autobooking.data.model.Applicant
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicantDao {
    @Query("SELECT * FROM applicants")
    fun getAllApplicants(): Flow<List<Applicant>>
    
    @Query("SELECT * FROM applicants WHERE id = :id")
    suspend fun getApplicantById(id: Long): Applicant?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplicant(applicant: Applicant): Long
    
    @Update
    suspend fun updateApplicant(applicant: Applicant)
    
    @Delete
    suspend fun deleteApplicant(applicant: Applicant)
}