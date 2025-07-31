package com.bls.autobooking.network.api

import com.bls.autobooking.network.model.CaptchaRequest
import com.bls.autobooking.network.model.CaptchaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BlsApiService {
    @POST("/api/ocr-match")
    suspend fun solveCaptcha(@Body request: CaptchaRequest): Response<CaptchaResponse>
}