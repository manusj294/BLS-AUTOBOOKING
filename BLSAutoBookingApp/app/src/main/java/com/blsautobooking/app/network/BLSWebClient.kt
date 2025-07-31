package com.blsautobooking.app.network

import android.content.Context
import android.util.Log
import com.blsautobooking.app.utils.StealthAutomation
import com.blsautobooking.app.utils.WebViewHelper
import kotlinx.coroutines.delay

class BLSWebClient(private val context: Context) {

    private lateinit var webViewHelper: WebViewHelper

    fun initialize() {
        webViewHelper = WebViewHelper(context)
        webViewHelper.initializeWebView()
        StealthAutomation.bypassBotDetection(webViewHelper)
    }

    suspend fun performLogin(username: String, password: String): Boolean {
        try {
            Log.d(TAG, "Attempting to log in with username: $username")
            val loginUrl = "https://algeria.blsspainglobal.com/DZA/account/login"
            
            if (!webViewHelper.loadUrl(loginUrl)) {
                Log.e(TAG, "Failed to load login page")
                return false
            }
            Log.d(TAG, "Login page loaded.")

            StealthAutomation.humanLikeDelay()

            // Fill username
            if (!webViewHelper.fillInputField("input[name=\'Email\']", username)) {
                Log.e(TAG, "Failed to fill username field")
                return false
            }
            Log.d(TAG, "Username filled.")

            StealthAutomation.humanLikeDelay()

            // Fill password
            if (!webViewHelper.fillInputField("input[name=\'Password\']", password)) {
                Log.e(TAG, "Failed to fill password field")
                return false
            }
            Log.d(TAG, "Password filled.")

            StealthAutomation.humanLikeDelay()

            // Click login button
            if (!webViewHelper.clickElement("button[type=\'submit\']")) {
                Log.e(TAG, "Failed to click login button")
                return false
            }
            Log.d(TAG, "Login button clicked.")

            StealthAutomation.humanLikeDelay(3000, 5000) // Longer delay for page navigation

            // Verify login success (e.g., check for URL change or presence of dashboard element)
            val currentUrl = webViewHelper.executeJavaScript("window.location.href")
            if (currentUrl != null && currentUrl.contains("/DZA/home/index")) {
                Log.d(TAG, "Login successful!")
                return true
            } else {
                Log.e(TAG, "Login failed. Current URL: $currentUrl")
                // Optionally, check for error messages on the page
                val errorMessage = webViewHelper.executeJavaScript("document.querySelector(\".validation-summary-errors ul li\").innerText")
                if (!errorMessage.isNullOrEmpty()) {
                    Log.e(TAG, "Login error message: $errorMessage")
                }
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during login: ${e.message}", e)
            return false
        } finally {
            webViewHelper.destroy()
        }
    }

    companion object {
        private const val TAG = "BLSWebClient"
    }
}

