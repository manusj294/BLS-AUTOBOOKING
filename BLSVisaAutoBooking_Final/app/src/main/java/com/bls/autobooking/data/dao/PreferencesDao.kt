package com.bls.autobooking.data.dao

import androidx.room.*
import com.bls.autobooking.data.model.Preferences
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {
    @Query("SELECT * FROM preferences WHERE id = 1")
    fun getPreferences(): Flow<Preferences?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: Preferences)
    
    @Update
    suspend fun updatePreferences(preferences: Preferences)
}