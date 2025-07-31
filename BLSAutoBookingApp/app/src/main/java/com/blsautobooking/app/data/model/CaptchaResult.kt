package com.blsautobooking.app.data.model

data class CaptchaResult(
    val success: Boolean,
    val solution: List<Int>? = null, // Array of tile indices to click
    val error: String? = null
)

