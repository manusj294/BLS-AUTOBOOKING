package com.bls.autobooking.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.bls.autobooking.data.database.AppDatabase
import com.bls.autobooking.data.model.Applicant
import com.bls.autobooking.data.model.Preferences
import com.bls.autobooking.repository.ApplicantRepository
import com.bls.autobooking.repository.BookingRepository
import com.bls.autobooking.repository.PreferencesRepository
import com.bls.autobooking.worker.SlotMonitoringWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val applicantRepository = ApplicantRepository(database.applicantDao())
    private val preferencesRepository = PreferencesRepository(database.preferencesDao())
    private val bookingRepository = BookingRepository()
    
    private val _applicants = applicantRepository.getAllApplicants().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val applicants: StateFlow<List<Applicant>> = _applicants
    
    private val _preferences = preferencesRepository.getPreferences().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )
    val preferences: StateFlow<Preferences?> = _preferences
    
    private val _isMonitoring = MutableLiveData<Boolean>()
    val isMonitoring: LiveData<Boolean> = _isMonitoring
    
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage
    
    init {
        startSlotMonitoring()
    }
    
    fun addApplicant(applicant: Applicant) {
        viewModelScope.launch {
            try {
                applicantRepository.insertApplicant(applicant)
                _statusMessage.value = "Applicant added successfully"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to add applicant: ${e.message}"
                Log.e("MainViewModel", "Error adding applicant", e)
            }
        }
    }
    
    fun updateApplicant(applicant: Applicant) {
        viewModelScope.launch {
            try {
                applicantRepository.updateApplicant(applicant)
                _statusMessage.value = "Applicant updated successfully"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update applicant: ${e.message}"
                Log.e("MainViewModel", "Error updating applicant", e)
            }
        }
    }
    
    fun deleteApplicant(applicant: Applicant) {
        viewModelScope.launch {
            try {
                applicantRepository.deleteApplicant(applicant)
                _statusMessage.value = "Applicant deleted successfully"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to delete applicant: ${e.message}"
                Log.e("MainViewModel", "Error deleting applicant", e)
            }
        }
    }
    
    fun savePreferences(preferences: Preferences) {
        viewModelScope.launch {
            try {
                preferencesRepository.savePreferences(preferences)
                _statusMessage.value = "Preferences saved successfully"
                
                // Restart monitoring with new preferences
                startSlotMonitoring()
            } catch (e: Exception) {
                _statusMessage.value = "Failed to save preferences: ${e.message}"
                Log.e("MainViewModel", "Error saving preferences", e)
            }
        }
    }
    
    private fun startSlotMonitoring() {
        viewModelScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val monitoringRequest = PeriodicWorkRequestBuilder<SlotMonitoringWorker>(
                60, TimeUnit.SECONDS // Check every 60 seconds as per specs
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(getApplication())
                .enqueueUniquePeriodicWork(
                    "slot_monitoring",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    monitoringRequest
                )
            
            _isMonitoring.value = true
        }
    }
    
    fun stopSlotMonitoring() {
        WorkManager.getInstance(getApplication())
            .cancelUniqueWork("slot_monitoring")
        _isMonitoring.value = false
    }
    
    fun solveCaptcha(base64Image: String) {
        viewModelScope.launch {
            _statusMessage.value = "Solving CAPTCHA..."
            val result = bookingRepository.solveCaptcha(base64Image)
            
            if (result.isSuccess) {
                _statusMessage.value = "CAPTCHA solved successfully: ${result.getOrNull()}"
            } else {
                _statusMessage.value = "Failed to solve CAPTCHA: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}