package com.bls.autobooking.repository

import android.graphics.Bitmap
import android.util.Base64
import com.bls.autobooking.network.RetrofitClient
import com.bls.autobooking.network.model.CaptchaRequest
import com.bls.autobooking.network.model.CaptchaResponse
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream

class CaptchaRepository {
    private val blsApiService = RetrofitClient.blsApiService
    
    suspend fun solveCaptchaWithRetry(
        captchaImage: Bitmap,
        maxRetries: Int = 10
    ): Result<String> {
        val base64Image = bitmapToBase64(captchaImage)
        
        for (attempt in 1..maxRetries) {
            try {
                val request = CaptchaRequest(image = base64Image)
                val response = blsApiService.solveCaptcha(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    return Result.success(response.body()?.text ?: "")
                }
                
                // Exponential backoff
                delay((1000 * attempt).toLong())
            } catch (e: Exception) {
                if (attempt == maxRetries) {
                    return Result.failure(e)
                }
                delay((1000 * attempt).toLong())
            }
        }
        
        return Result.failure(Exception("Failed to solve CAPTCHA after $maxRetries attempts"))
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    fun simulateHumanClick(tileIndexes: List<Int>): List<Int> {
        // Simulate human-like clicking with random delays
        // In a real implementation, this would interact with the WebView
        return tileIndexes.shuffled() // Simulate random clicking order
    }
}