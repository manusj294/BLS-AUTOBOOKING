package com.bls.autobooking.network.model

data class CaptchaResponse(
    val success: Boolean,
    val text: String?
)