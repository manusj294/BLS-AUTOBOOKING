package com.bls.autobooking.repository

import android.util.Log
import android.webkit.WebView
import com.bls.autobooking.data.model.Applicant
import com.bls.autobooking.data.model.Preferences
import com.bls.autobooking.network.RetrofitClient
import com.bls.autobooking.network.model.CaptchaRequest
import com.bls.autobooking.network.model.CaptchaResponse
import com.bls.autobooking.network.model.EmailRequest
import kotlinx.coroutines.delay
import java.util.*

class BookingRepository {
    private val blsApiService = RetrofitClient.blsApiService
    private val emailJsService = RetrofitClient.emailJsService
    
    // EmailJS configuration from specs
    private val EMAILJS_SERVICE_ID = "service_pr301aj"
    private val EMAILJS_TEMPLATE_ID = "template_k91tvvh"
    private val EMAILJS_PUBLIC_KEY = "wtW2gzZbGcbGG8ati"
    private val NOTIFICATION_EMAIL = "nomadsam6@gmail.com"
    
    suspend fun solveCaptcha(base64Image: String): Result<String> {
        return try {
            val request = CaptchaRequest(image = base64Image)
            val response = blsApiService.solveCaptcha(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.text ?: "")
            } else {
                Result.failure(Exception("CAPTCHA solving failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkAppointmentSlots(
        webView: WebView,
        preferences: Preferences
    ): Result<List<AppointmentSlot>> {
        return try {
            // Simulate human behavior with random delays
            delay((1000..3000).random().toLong())
            
            // This would be implemented with actual web scraping logic
            // based on the source code provided
            val slots = scrapeAppointmentSlots(webView, preferences)
            Result.success(slots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun scrapeAppointmentSlots(
        webView: WebView,
        preferences: Preferences
    ): List<AppointmentSlot> {
        // This would contain the actual scraping logic
        // based on the website structure from the source code
        delay((500..1500).random().toLong())
        return emptyList() // Placeholder
    }
    
    suspend fun bookAppointment(
        applicant: Applicant,
        slot: AppointmentSlot,
        captchaText: String
    ): Result<Boolean> {
        return try {
            // Simulate human behavior
            delay((2000..5000).random().toLong())
            
            // This would contain the actual booking logic
            // based on the website forms from the source code
            val success = performBooking(applicant, slot, captchaText)
            
            if (success) {
                sendBookingConfirmationEmail(applicant, slot)
            }
            
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun performBooking(
        applicant: Applicant,
        slot: AppointmentSlot,
        captchaText: String
    ): Boolean {
        // This would contain the actual booking implementation
        // based on the website forms from the source code
        delay((3000..7000).random().toLong())
        return true // Placeholder
    }
    
    suspend fun sendBookingConfirmationEmail(
        applicant: Applicant,
        slot: AppointmentSlot
    ) {
        try {
            val templateParams = mapOf(
                "applicant_name" to applicant.fullName,
                "visa_type" to applicant.visaType,
                "appointment_date" to slot.date,
                "appointment_time" to slot.time,
                "location" to applicant.location,
                "to_email" to NOTIFICATION_EMAIL
            )
            
            val emailRequest = EmailRequest(
                service_id = EMAILJS_SERVICE_ID,
                template_id = EMAILJS_TEMPLATE_ID,
                user_id = EMAILJS_PUBLIC_KEY,
                template_params = templateParams
            )
            
            emailJsService.sendEmail(emailRequest)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to send email notification", e)
        }
    }
    
    suspend fun sendSlotAvailableEmail(
        applicant: Applicant,
        slot: AppointmentSlot
    ) {
        try {
            val templateParams = mapOf(
                "applicant_name" to applicant.fullName,
                "visa_type" to applicant.visaType,
                "available_date" to slot.date,
                "available_time" to slot.time,
                "location" to applicant.location,
                "to_email" to NOTIFICATION_EMAIL
            )
            
            val emailRequest = EmailRequest(
                service_id = EMAILJS_SERVICE_ID,
                template_id = EMAILJS_TEMPLATE_ID,
                user_id = EMAILJS_PUBLIC_KEY,
                template_params = templateParams
            )
            
            emailJsService.sendEmail(emailRequest)
        } catch (e: Exception) {
            Log.e("BookingRepository", "Failed to send slot available email", e)
        }
    }
}

data class AppointmentSlot(
    val date: String,
    val time: String,
    val location: String
)