package com.blsautobooking.app.data.model

data class BookingResult(
    val success: Boolean,
    val applicantName: String,
    val appointmentId: String? = null,
    val error: String? = null
)

