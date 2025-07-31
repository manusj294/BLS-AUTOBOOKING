package com.bls.autobooking.booking

import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BookingSuccessVerifier {
    private val TAG = "BookingSuccessVerifier"
    
    data class BookingResult(
        val isSuccess: Boolean,
        val bookingId: String?,
        val confirmationMessage: String?,
        val errorMessage: String?
    )
    
    suspend fun verifyBookingSuccess(webView: WebView): BookingResult {
        return suspendCancellableCoroutine { continuation ->
            webView.evaluateJavascript(
                """
                (function() {
                    try {
                        // Look for success indicators based on typical booking confirmation patterns
                        var successIndicators = document.querySelectorAll(
                            '.success, .alert-success, [class*="success"], [id*="success"], .confirmation, .booking-confirmation'
                        );
                        
                        // Look for booking reference numbers (typically alphanumeric, 6-12 characters)
                        var pageText = document.body.textContent || document.body.innerText;
                        var bookingIdPattern = /\b[A-Z0-9]{6,12}\b/;
                        var bookingIdMatch = pageText.match(bookingIdPattern);
                        var bookingId = bookingIdMatch ? bookingIdMatch[0] : null;
                        
                        // Look for confirmation messages
                        var confirmationKeywords = [
                            'confirm', 'success', 'booked', 'appointment', 'confirmation',
                            'reserved', 'scheduled', 'approved'
                        ];
                        
                        var isConfirmed = false;
                        var confirmationMessage = null;
                        
                        for (var i = 0; i < confirmationKeywords.length; i++) {
                            if (pageText.toLowerCase().includes(confirmationKeywords[i])) {
                                isConfirmed = true;
                                // Try to extract a more specific confirmation message
                                var regex = new RegExp(confirmationKeywords[i] + '[^.!?]*[.!?]', 'i');
                                var match = pageText.match(regex);
                                if (match) {
                                    confirmationMessage = match[0].trim();
                                }
                                break;
                            }
                        }
                        
                        // Look for error messages
                        var errorIndicators = document.querySelectorAll(
                            '.error, .alert-error, .alert-danger, [class*="error"]'
                        );
                        
                        var errorMessage = null;
                        if (errorIndicators.length > 0) {
                            // Try to extract error message
                            var errorElement = errorIndicators[0];
                            errorMessage = errorElement.textContent || errorElement.innerText;
                        } else if (pageText.toLowerCase().includes('error') || 
                                  pageText.toLowerCase().includes('failed') ||
                                  pageText.toLowerCase().includes('invalid')) {
                            // Look for error keywords in page text
                            var errorRegex = /(error|failed|invalid)[^.!?]*[.!?]/i;
                            var errorMatch = pageText.match(errorRegex);
                            if (errorMatch) {
                                errorMessage = errorMatch[0].trim();
                            }
                        }
                        
                        return JSON.stringify({
                            isSuccess: (successIndicators.length > 0 || isConfirmed || bookingId !== null) && errorIndicators.length === 0,
                            bookingId: bookingId,
                            confirmationMessage: confirmationMessage,
                            errorMessage: errorMessage
                        });
                    } catch (e) {
                        return JSON.stringify({
                            isSuccess: false,
                            bookingId: null,
                            confirmationMessage: null,
                            errorMessage: 'Error verifying booking: ' + e.message
                        });
                    }
                })();
                """.trimIndent()
            ) { result ->
                try {
                    if (result != null && result.startsWith("\"") && result.endsWith("\"")) {
                        val jsonString = result.substring(1, result.length - 1).replace("\\\"", "\"")
                        Log.d(TAG, "Booking verification result: $jsonString")
                        
                        // Parse the JSON result
                        val bookingResult = parseBookingResultFromJson(jsonString)
                        continuation.resume(bookingResult)
                    } else {
                        Log.e(TAG, "Unexpected booking verification result: $result")
                        continuation.resume(BookingResult(false, null, null, "Unexpected result format"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing booking verification result", e)
                    continuation.resume(BookingResult(false, null, null, "Error parsing result: ${e.message}"))
                }
            }
        }
    }
    
    private fun parseBookingResultFromJson(jsonString: String): BookingResult {
        try {
            // Simple JSON parsing for demonstration
            val cleanJson = jsonString.replace("\\", "")
            
            var isSuccess = false
            var bookingId: String? = null
            var confirmationMessage: String? = null
            var errorMessage: String? = null
            
            // Extract values (simplified)
            if (cleanJson.contains("\"isSuccess\":true")) {
                isSuccess = true
            }
            
            // Extract bookingId
            val bookingIdRegex = "\"bookingId\":\"([^\"]*)\"".toRegex()
            val bookingIdMatch = bookingIdRegex.find(cleanJson)
            if (bookingIdMatch != null) {
                bookingId = bookingIdMatch.groupValues[1]
                if (bookingId == "null") bookingId = null
            }
            
            // Extract confirmationMessage
            val confirmationRegex = "\"confirmationMessage\":\"([^\"]*)\"".toRegex()
            val confirmationMatch = confirmationRegex.find(cleanJson)
            if (confirmationMatch != null) {
                confirmationMessage = confirmationMatch.groupValues[1]
                if (confirmationMessage == "null") confirmationMessage = null
            }
            
            // Extract errorMessage
            val errorRegex = "\"errorMessage\":\"([^\"]*)\"".toRegex()
            val errorMatch = errorRegex.find(cleanJson)
            if (errorMatch != null) {
                errorMessage = errorMatch.groupValues[1]
                if (errorMessage == "null") errorMessage = null
            }
            
            return BookingResult(isSuccess, bookingId, confirmationMessage, errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing booking result JSON: ${e.message}")
            return BookingResult(false, null, null, "Error parsing result: ${e.message}")
        }
    }
}