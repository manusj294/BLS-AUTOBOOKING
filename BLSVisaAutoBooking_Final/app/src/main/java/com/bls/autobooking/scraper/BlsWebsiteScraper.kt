package com.bls.autobooking.scraper

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bls.autobooking.data.model.Preferences
import com.bls.autobooking.engine.AppointmentSlot
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BlsWebsiteScraper {
    private val TAG = "BlsWebsiteScraper"
    private val BASE_URL = "https://algeria.blsspainglobal.com"
    
    suspend fun scrapeAvailableSlots(
        webView: WebView,
        preferences: Preferences
    ): List<AppointmentSlot> {
        return try {
            Log.d(TAG, "Starting slot scraping process")
            
            // Navigate to the main page
            loadUrl(webView, BASE_URL)
            
            // Handle login if needed
            handleLogin(webView)
            
            // Select location and visa type based on source code patterns
            selectLocationAndVisaType(webView, preferences)
            
            // Scrape available appointment dates
            val slots = extractAvailableSlots(webView)
            Log.d(TAG, "Found ${slots.size} available slots")
            slots
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping slots", e)
            emptyList()
        }
    }
    
    private suspend fun loadUrl(webView: WebView, url: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    if (!continuation.isCancelled) {
                        Log.d(TAG, "Page finished loading: $url")
                        continuation.resume(Unit)
                    }
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    if (!continuation.isCancelled) {
                        Log.e(TAG, "Failed to load URL: $description for $failingUrl")
                        continuation.cancel(Exception("Failed to load URL: $description"))
                    }
                }
            }
            Log.d(TAG, "Loading URL: $url")
            webView.loadUrl(url)
        }
    }
    
    private suspend fun handleLogin(webView: WebView) {
        Log.d(TAG, "Handling login process")
        
        // Wait for page to load
        delay(2000)
        
        // Based on SECOND LOGIN SOURCE CODE.pdf, we need to show login elements
        // The source shows functions like ySdl() that show elements with IDs 'xcnvPF' and 'EGPV'
        webView.evaluateJavascript(
            """
            (function() {
                try {
                    // Show login elements based on source code patterns
                    if (document.getElementById('xcnvPF')) {
                        document.getElementById('xcnvPF').style.display = 'block';
                    }
                    if (document.getElementById('EGPV')) {
                        document.getElementById('EGPV').style.display = 'block';
                    }
                    
                    // Additional elements from source code
                    var loginFunctions = ['ySdl', 'xEQxy', 'eTcUyod', 'HdQo'];
                    for (var i = 0; i < loginFunctions.length; i++) {
                        if (typeof window[loginFunctions[i]] === 'function') {
                            try {
                                window[loginFunctions[i]]();
                            } catch (e) {
                                console.log('Error calling ' + loginFunctions[i] + ': ' + e.message);
                            }
                        }
                    }
                    
                    return 'Login elements shown';
                } catch (e) {
                    return 'Error: ' + e.message;
                }
            })();
            """.trimIndent()
        ) { result ->
            Log.d(TAG, "Login preparation result: $result")
        }
        
        // Add human-like delay
        delay((1000..3000).random().toLong())
    }
    
    private suspend fun selectLocationAndVisaType(
        webView: WebView,
        preferences: Preferences
    ) {
        Log.d(TAG, "Selecting location: ${preferences.targetLocation} and visa type: ${preferences.targetVisaType}")
        
        // Based on SOURCE.pdf, we need to handle Kendo DropDownList selections
        // This involves selecting location first, then visa type
        
        // Select location using Kendo DropDownList
        webView.evaluateJavascript(
            """
            (function() {
                try {
                    var locationDropdown = $("#LocationId").data("kendoDropDownList");
                    if (locationDropdown) {
                        // Find location by text (simplified)
                        var locationData = locationDropdown.dataSource.data();
                        for (var i = 0; i < locationData.length; i++) {
                            if (locationData[i].Name && 
                                locationData[i].Name.toLowerCase().includes("${preferences.targetLocation.lowercase()}")) {
                                locationDropdown.value(locationData[i].Id);
                                locationDropdown.trigger("change");
                                return "Location selected: " + locationData[i].Name;
                            }
                        }
                        return "Location not found: ${preferences.targetLocation}";
                    }
                    return "Location dropdown not found";
                } catch (e) {
                    return "Error selecting location: " + e.message;
                }
            })();
            """.trimIndent()
        ) { result ->
            Log.d(TAG, "Location selection result: $result")
        }
        
        // Wait for visa types to load
        delay(3000)
        
        // Select visa type using Kendo DropDownList
        webView.evaluateJavascript(
            """
            (function() {
                try {
                    var visaTypeDropdown = $("#VisaType").data("kendoDropDownList");
                    if (visaTypeDropdown) {
                        // Find visa type by text
                        var visaTypeData = visaTypeDropdown.dataSource.data();
                        for (var i = 0; i < visaTypeData.length; i++) {
                            if (visaTypeData[i].Name && 
                                visaTypeData[i].Name.toLowerCase().includes("${preferences.targetVisaType.lowercase()}")) {
                                visaTypeDropdown.value(visaTypeData[i].Id);
                                visaTypeDropdown.trigger("change");
                                return "Visa type selected: " + visaTypeData[i].Name;
                            }
                        }
                        return "Visa type not found: ${preferences.targetVisaType}";
                    }
                    return "Visa type dropdown not found";
                } catch (e) {
                    return "Error selecting visa type: " + e.message;
                }
            })();
            """.trimIndent()
        ) { result ->
            Log.d(TAG, "Visa type selection result: $result")
        }
        
        // Wait for appointment dates to load
        delay(3000)
    }
    
    private suspend fun extractAvailableSlots(webView: WebView): List<AppointmentSlot> {
        Log.d(TAG, "Extracting available appointment slots")
        
        // Based on APPOINTMENT SOURCE CODE.pdf and SOURCE.pdf, we need to extract available dates
        return suspendCancellableCoroutine { continuation ->
            webView.evaluateJavascript(
                """
                (function() {
                    try {
                        var slots = [];
                        
                        // Look for appointment date elements in Kendo Calendar
                        // Based on the source code, dates are likely in specific elements
                        var dateElements = document.querySelectorAll('.k-calendar td[data-value]');
                        
                        for (var i = 0; i < dateElements.length; i++) {
                            var element = dateElements[i];
                            // Check if the date is available (not disabled or from other month)
                            if (!element.classList.contains('k-other-month') && 
                                !element.classList.contains('k-state-disabled')) {
                                
                                var dateValue = element.getAttribute('data-value');
                                if (dateValue) {
                                    // Extract date information
                                    var date = new Date(dateValue);
                                    var dateString = date.toISOString().split('T')[0];
                                    
                                    // Look for available time slots
                                    var timeSlots = [];
                                    var timeElements = document.querySelectorAll('.time-slot:not(.disabled), .k-time-list li:not(.k-disabled)');
                                    
                                    for (var j = 0; j < timeElements.length; j++) {
                                        var timeText = timeElements[j].textContent || timeElements[j].innerText;
                                        if (timeText && timeText.trim() !== '') {
                                            timeSlots.push(timeText.trim());
                                        }
                                    }
                                    
                                    // If no specific time slots found, use a default
                                    if (timeSlots.length === 0) {
                                        timeSlots.push("Morning");
                                    }
                                    
                                    // Create slot objects
                                    for (var k = 0; k < Math.min(timeSlots.length, 5); k++) { // Limit to 5 slots per date
                                        slots.push({
                                            date: dateString,
                                            time: timeSlots[k],
                                            location: "Algiers" // This would be dynamic in a real implementation
                                        });
                                    }
                                }
                            }
                        }
                        
                        return JSON.stringify(slots);
                    } catch (e) {
                        return "Error: " + e.message;
                    }
                })();
                """.trimIndent()
            ) { result ->
                try {
                    if (result.startsWith("\"") && result.endsWith("\"")) {
                        val jsonString = result.substring(1, result.length - 1).replace("\\\"", "\"")
                        Log.d(TAG, "Extracted slots JSON: $jsonString")
                        
                        // Parse the JSON result
                        val slots = parseSlotsFromJson(jsonString)
                        continuation.resume(slots)
                    } else {
                        Log.e(TAG, "Unexpected result format: $result")
                        continuation.resume(emptyList())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing slots", e)
                    continuation.resume(emptyList())
                }
            }
        }
    }
    
    private fun parseSlotsFromJson(jsonString: String): List<AppointmentSlot> {
        val slots = mutableListOf<AppointmentSlot>()
        
        try {
            // Simple JSON parsing for demonstration
            // In a real implementation, you'd use a proper JSON library like Gson
            if (jsonString.contains("[") && jsonString.contains("]")) {
                // Extract array content
                val arrayContent = jsonString.substring(jsonString.indexOf("[") + 1, jsonString.lastIndexOf("]"))
                
                // Split by objects (simplified)
                val objects = arrayContent.split("},")
                
                for (obj in objects) {
                    val cleanObj = obj.replace("{", "").replace("}", "").replace("\"", "")
                    val properties = cleanObj.split(",")
                    
                    var date = ""
                    var time = ""
                    var location = "Algiers"
                    
                    for (prop in properties) {
                        val parts = prop.split(":")
                        if (parts.size == 2) {
                            when (parts[0].trim()) {
                                "date" -> date = parts[1].trim()
                                "time" -> time = parts[1].trim()
                                "location" -> location = parts[1].trim()
                            }
                        }
                    }
                    
                    if (date.isNotEmpty() && time.isNotEmpty()) {
                        slots.add(AppointmentSlot(date, time, location))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BlsWebsiteScraper", "Error parsing JSON: ${e.message}")
        }
        
        return slots
    }
}