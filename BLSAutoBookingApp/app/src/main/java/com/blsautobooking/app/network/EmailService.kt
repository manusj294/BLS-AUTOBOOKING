package com.blsautobooking.app.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class EmailService {

    // EmailJS configuration from the PRD
    private val serviceId = "service_pr301aj"
    private val templateId = "template_k91tvvh"
    private val publicKey = "wtW2gzZbGcbGG8ati"
    private val recipientEmail = "nomadsam6@gmail.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendNotificationEmail(subject: String, message: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val emailData = JSONObject().apply {
                    put("service_id", serviceId)
                    put("template_id", templateId)
                    put("user_id", publicKey)
                    put("template_params", JSONObject().apply {
                        put("to_email", recipientEmail)
                        put("subject", subject)
                        put("message", message)
                        put("from_name", "BLS AutoBooking App")
                    })
                }

                val requestBody = emailData.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("https://api.emailjs.com/api/v1.0/email/send")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val success = response.isSuccessful

                if (success) {
                    Log.d(TAG, "Email sent successfully: $subject")
                } else {
                    Log.e(TAG, "Failed to send email: ${response.code} - ${response.message}")
                }

                response.close()
                success
            } catch (e: Exception) {
                Log.e(TAG, "Error sending email", e)
                false
            }
        }
    }

    suspend fun sendAppointmentFoundEmail(slotCount: Int): Boolean {
        val subject = "BLS Visa Appointment Slots Available!"
        val message = """
            Great news! We found $slotCount available appointment slot(s) for your BLS visa application.
            
            The BLS AutoBooking App is now attempting to book these slots for your registered applicants.
            
            You will receive another notification once the booking process is complete.
            
            Best regards,
            BLS AutoBooking App
        """.trimIndent()

        return sendNotificationEmail(subject, message)
    }

    suspend fun sendBookingSuccessEmail(applicantName: String, appointmentDetails: String): Boolean {
        val subject = "BLS Visa Appointment Booked Successfully!"
        val message = """
            Excellent news! We have successfully booked a BLS visa appointment for $applicantName.
            
            Appointment Details:
            $appointmentDetails
            
            Please make sure to attend the appointment on time with all required documents.
            
            Best regards,
            BLS AutoBooking App
        """.trimIndent()

        return sendNotificationEmail(subject, message)
    }

    suspend fun sendBookingFailedEmail(applicantName: String, error: String): Boolean {
        val subject = "BLS Visa Appointment Booking Failed"
        val message = """
            Unfortunately, we were unable to book a BLS visa appointment for $applicantName.
            
            Error: $error
            
            The app will continue monitoring for available slots and will try again when new appointments become available.
            
            Best regards,
            BLS AutoBooking App
        """.trimIndent()

        return sendNotificationEmail(subject, message)
    }

    companion object {
        private const val TAG = "EmailService"
    }
}

