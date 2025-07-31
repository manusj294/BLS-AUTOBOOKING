package com.bls.autobooking.repository

import com.bls.autobooking.data.dao.PreferencesDao
import com.bls.autobooking.data.model.Preferences
import kotlinx.coroutines.flow.Flow

class PreferencesRepository(private val preferencesDao: PreferencesDao) {
    fun getPreferences(): Flow<Preferences?> = preferencesDao.getPreferences()
    
    suspend fun savePreferences(preferences: Preferences) = preferencesDao.insertPreferences(preferences)
    
    suspend fun updatePreferences(preferences: Preferences) = preferencesDao.updatePreferences(preferences)
}