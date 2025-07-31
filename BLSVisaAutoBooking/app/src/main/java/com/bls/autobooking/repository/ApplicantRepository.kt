package com.bls.autobooking.repository

import com.bls.autobooking.data.dao.ApplicantDao
import com.bls.autobooking.data.model.Applicant
import kotlinx.coroutines.flow.Flow

class ApplicantRepository(private val applicantDao: ApplicantDao) {
    fun getAllApplicants(): Flow<List<Applicant>> = applicantDao.getAllApplicants()
    
    suspend fun getApplicantById(id: Long): Applicant? = applicantDao.getApplicantById(id)
    
    suspend fun insertApplicant(applicant: Applicant): Long = applicantDao.insertApplicant(applicant)
    
    suspend fun updateApplicant(applicant: Applicant) = applicantDao.updateApplicant(applicant)
    
    suspend fun deleteApplicant(applicant: Applicant) = applicantDao.deleteApplicant(applicant)
}