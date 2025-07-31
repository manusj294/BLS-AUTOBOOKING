package com.bls.autobooking.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class Preferences(
    @PrimaryKey
    val id: Int = 1,
    val targetVisaType: String,
    val targetLocation: String,
    val earliestDate: String? = null,
    val latestDate: String? = null,
    val emailNotifications: Boolean = true,
    val notificationEmail: String,
    val captchaRetryAttempts: Int = 3,
    val bookingRetryAttempts: Int = 3
)