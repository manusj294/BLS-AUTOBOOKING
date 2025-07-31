package com.blsautobooking.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applicants")
data class Applicant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val passportNumber: String,
    val visaType: String,
    val visaTypeId: String, // The ID used in the form
    val preferredCenter: String,
    val preferredDate: String? = null,
    val isActive: Boolean = true
)

