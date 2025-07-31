package com.bls.autobooking.network.api

import com.bls.autobooking.network.model.EmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailJsService {
    @POST("https://api.emailjs.com/api/v1.0/email/send")
    suspend fun sendEmail(@Body request: EmailRequest): Response<Unit>
}