package com.bls.autobooking.error

import android.util.Log
import com.bls.autobooking.util.Logger

class ErrorHandler(private val logger: Logger) {
    private val TAG = "ErrorHandler"
    
    enum class ErrorType {
        CAPTCHA_FAILURE,
        SLOT_CHECKING_FAILURE,
        BOOKING_SUBMISSION_FAILURE,
        NETWORK_ERROR,
        SESSION_EXPIRED,
        RATE_LIMITED,
        INVALID_CREDENTIALS,
        UNKNOWN_ERROR
    }
    
    data class ErrorInfo(
        val type: ErrorType,
        val message: String,
        val retryCount: Int,
        val shouldRetry: Boolean,
        val delayBeforeRetry: Long
    )
    
    fun analyzeError(error: Exception, currentRetryCount: Int): ErrorInfo {
        val errorMessage = error.message ?: "Unknown error"
        Log.e(TAG, "Analyzing error: $errorMessage", error)
        
        return when {
            errorMessage.contains("captcha", ignoreCase = true) -> {
                ErrorInfo(
                    type = ErrorType.CAPTCHA_FAILURE,
                    message = errorMessage,
                    retryCount = currentRetryCount,
                    shouldRetry = currentRetryCount < 10,
                    delayBeforeRetry = calculateExponentialDelay(currentRetryCount, 2000L)
                )
            }
            
            errorMessage.contains("slot", ignoreCase = true) || 
            errorMessage.contains("appointment", ignoreCase = true) -> {
                ErrorInfo(
                    type = ErrorType.SLOT_CHECKING_FAILURE,
                    message = errorMessage,
                    retryCount = currentRetryCount,
                    shouldRetry = currentRetryCount < 5,
                    delayBeforeRetry = calculateExponentialDelay(currentRetryCount, 5000L)
                )
            }
            
            errorMessage.contains("booking", ignoreCase = true) || 
            errorMessage.contains("submit", ignoreCase = true) -> {
                ErrorInfo(
                    type = ErrorType.BOOKING_SUBMISSION_FAILURE,
                    message = errorMessage,
                    retryCount = currentRetryCount,
                    shouldRetry = currentRetryCount < 3,
                    delayBeforeRetry = calculateExponentialDelay(currentRetryCount, 10000L)
                )
            }
            
            errorMessage.contains("network", ignoreCase = true) || 
            errorMessage.contains("timeout", ignoreCase = true) || 
            errorMessage.contains("connection", ignoreCase = true) -> {
                ErrorInfo(
                    type = ErrorType.NETWORK_ERROR,
                    message = errorMessage,
                    retryCount = currentRetryCount,
                    shouldRetry = currentRetryCount < 5,
                    delayBeforeRetry = calculateExponentialDelay(currentRetryCount, 3000L)
                )
            }
            
            errorMessage.contains("session", ignoreCase = true) || 
            errorMessage.contains("login", ignoreCase = true) || 
            errorMessage.contains("authentication", ignoreCase = true) -> {
                ErrorInfo(
                    type = ErrorType.SESSION_EXPIRED,
                    message = errorMessage,
                    retryCount = currentRetryCount,
                    shouldRetry = true,
                    delayBeforeRetry = 1000L // Quick retry for session issues
                )
            }
            
            errorMessage.contains("rate", ignoreCase = true) || 
            errorMessage.contains("limit", ignoreCase = true) -> {
                ErrorInfo(
                    type = ErrorType.RATE_LIMITED,
                    message = errorMessage,
                    retryCount = currentRetryCount,
                    shouldRetry = true,
                    delayBeforeRetry = 60000L // 1 minute delay for rate limiting
                )
            }
            
            else -> {
                ErrorInfo(
                    type = ErrorType.UNKNOWN_ERROR,
                    message = errorMessage,
                    retryCount = currentRetryCount,
                    shouldRetry = currentRetryCount < 3,
                    delayBeforeRetry = calculateExponentialDelay(currentRetryCount, 5000L)
                )
            }
        }.also { errorInfo ->
            logger.logEvent(
                when (errorInfo.type) {
                    ErrorType.CAPTCHA_FAILURE -> Logger.EventType.CAPTCHA_FAILURE
                    ErrorType.SLOT_CHECKING_FAILURE -> Logger.EventType.SLOT_CHECKING_FAILURE
                    ErrorType.BOOKING_SUBMISSION_FAILURE -> Logger.EventType.BOOKING_ERROR
                    else -> Logger.EventType.SYSTEM_ERROR
                },
                errorInfo.message,
                if (errorInfo.shouldRetry) "warning" else "error"
            )
        }
    }
    
    private fun calculateExponentialDelay(retryCount: Int, baseDelay: Long): Long {
        return (baseDelay * (2.0.pow(retryCount.toDouble())).toLong()).coerceAtMost(300000L) // Max 5 minutes
    }
    
    fun shouldAlertUser(errorInfo: ErrorInfo): Boolean {
        return when (errorInfo.type) {
            ErrorType.CAPTCHA_FAILURE -> errorInfo.retryCount >= 5
            ErrorType.SLOT_CHECKING_FAILURE -> errorInfo.retryCount >= 3
            ErrorType.BOOKING_SUBMISSION_FAILURE -> errorInfo.retryCount >= 2
            ErrorType.RATE_LIMITED -> true
            ErrorType.SESSION_EXPIRED -> errorInfo.retryCount >= 2
            else -> errorInfo.retryCount >= 3
        }
    }
    
    fun getUserAlertMessage(errorInfo: ErrorInfo): String {
        return when (errorInfo.type) {
            ErrorType.CAPTCHA_FAILURE -> "Excessive CAPTCHA failures detected. Please check your internet connection."
            ErrorType.SLOT_CHECKING_FAILURE -> "Unable to check appointment slots. The website may be down."
            ErrorType.BOOKING_SUBMISSION_FAILURE -> "Booking submission failed. Please verify your information."
            ErrorType.NETWORK_ERROR -> "Network connection issues detected. Please check your internet."
            ErrorType.SESSION_EXPIRED -> "Session expired. You may need to log in again."
            ErrorType.RATE_LIMITED -> "Rate limit exceeded. Service will pause temporarily."
            ErrorType.INVALID_CREDENTIALS -> "Invalid login credentials. Please check your login information."
            ErrorType.UNKNOWN_ERROR -> "Unexpected error occurred. Please try again later."
        }
    }
}