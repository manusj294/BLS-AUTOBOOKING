package com.blsautobooking.app.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blsautobooking.app.network.AppointmentService
import com.blsautobooking.app.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppointmentMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val appointmentService = AppointmentService()
    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting appointment monitoring...")
                
                // Check for available appointment slots
                val availableSlots = appointmentService.checkAvailableSlots()
                
                if (availableSlots.isNotEmpty()) {
                    Log.d(TAG, "Found ${availableSlots.size} available slots")
                    
                    // Send notification about available slots
                    notificationHelper.showAppointmentFoundNotification(availableSlots.size)
                    
                    // Try to book appointments for eligible applicants
                    val bookingResults = appointmentService.attemptBooking(availableSlots)
                    
                    bookingResults.forEach { result ->
                        if (result.success) {
                            Log.d(TAG, "Successfully booked appointment for ${result.applicantName}")
                            notificationHelper.showBookingSuccessNotification(result.applicantName)
                        } else {
                            Log.w(TAG, "Failed to book appointment for ${result.applicantName}: ${result.error}")
                        }
                    }
                } else {
                    Log.d(TAG, "No available slots found")
                }
                
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Error during appointment monitoring", e)
                Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "AppointmentMonitorWorker"
    }
}

