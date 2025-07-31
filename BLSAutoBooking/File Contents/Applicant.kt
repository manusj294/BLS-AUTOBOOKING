package com.bls.autobooking.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applicants")
data class Applicant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val passportNumber: String,
    val nationality: String,
    val visaType: String,
    val location: String,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val status: String = "pending" // pending, booked, failed
)