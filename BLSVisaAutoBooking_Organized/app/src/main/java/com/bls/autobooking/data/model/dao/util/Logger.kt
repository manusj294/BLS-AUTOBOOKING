package com.bls.autobooking.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class Logger(private val context: Context) {
    private val TAG = "BLSAutoBooking"
    private val logFile = File(context.getExternalFilesDir(null), "app_logs.txt")
    
    fun logEvent(eventType: EventType, message: String, status: String = "info") {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "$timestamp | $eventType | $status | $message\n"
        
        // Log to Android logcat
        when (status.lowercase()) {
            "error" -> Log.e(TAG, logEntry)
            "warning" -> Log.w(TAG, logEntry)
            else -> Log.i(TAG, logEntry)
        }
        
        // Save to file
        try {
            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }
    
    fun getLogs(): String {
        return try {
            logFile.readText()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read log file", e)
            "Error reading logs: ${e.message}"
        }
    }
    
    fun clearLogs() {
        try {
            FileWriter(logFile, false).use { writer ->
                writer.write("")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear log file", e)
        }
    }
    
    enum class EventType {
        CAPTCHA_FAILURE,
        SLOT_CHECKING_FAILURE,
        BOOKING_ERROR,
        BOOKING_SUCCESS,
        SYSTEM_ERROR,
        NETWORK_ERROR,
        USER_ACTION
    }
}