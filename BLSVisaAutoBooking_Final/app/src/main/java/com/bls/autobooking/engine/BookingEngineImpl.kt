package com.bls.autobooking.engine

import android.util.Log
import android.webkit.WebView
import com.bls.autobooking.captcha.CaptchaHandler
import com.bls.autobooking.data.model.Applicant
import com.bls.autobooking.data.model.Preferences
import com.bls.autobooking.repository.ApplicantRepository
import com.bls.autobooking.repository.BookingRepository
import com.bls.autobooking.scraper.BlsWebsiteScraper
import kotlinx.coroutines.delay

class BookingEngineImpl(
    private val applicantRepository: ApplicantRepository,
    private val bookingRepository: BookingRepository,
    private val scraper: BlsWebsiteScraper,
    private val captchaHandler: CaptchaHandler
) {
    private val TAG = "BookingEngineImpl"
    
    suspend fun autoBookAppointment(
        webView: WebView,
        preferences: Preferences
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "Starting auto-booking process")
            
            // Simulate human behavior with random delays
            simulateHumanBehavior()
            
            // Check for available slots matching preferences
            val availableSlots = scraper.scrapeAvailableSlots(webView, preferences)
            val matchingSlots = filterSlotsByPreferences(availableSlots, preferences)
            
            if (matchingSlots.isNotEmpty()) {
                Log.d(TAG, "Found ${matchingSlots.size} matching slots")
                
                // Send notification about available slots
                if (preferences.emailNotifications && matchingSlots.isNotEmpty()) {
                    bookingRepository.sendSlotAvailableEmail(
                        Applicant(
                            fullName = "User",
                            email = preferences.notificationEmail,
                            passportNumber = "",
                            nationality = "",
                            visaType = preferences.targetVisaType,
                            location = preferences.targetLocation
                        ),
                        matchingSlots.first()
                    )
                }
                
                // Get applicant matching the preferences
                val applicant = getApplicantForBooking(preferences)
                
                if (applicant != null) {
                    Log.d(TAG, "Found matching applicant: ${applicant.fullName}")
                    
                    // Attempt booking with retry logic
                    return attemptBookingWithRetry(
                        webView, 
                        applicant, 
                        matchingSlots.first(), 
                        preferences.bookingRetryAttempts
                    )
                } else {
                    Log.w(TAG, "No matching applicant found")
                }
            } else {
                Log.d(TAG, "No matching slots found")
            }
            
            Result.success(false)
        } catch (e: Exception) {
            Log.e(TAG, "Error in auto-booking process", e)
            Result.failure(e)
        }
    }
    
    private fun filterSlotsByPreferences(
        slots: List<AppointmentSlot>,
        preferences: Preferences
    ): List<AppointmentSlot> {
        return slots.filter { slot ->
            // Filter by date range if specified
            val slotDate = slot.date
            val passesDateFilter = if (preferences.earliestDate != null && preferences.latestDate != null) {
                slotDate >= preferences.earliestDate && slotDate <= preferences.latestDate
            } else if (preferences.earliestDate != null) {
                slotDate >= preferences.earliestDate
            } else if (preferences.latestDate != null) {
                slotDate <= preferences.latestDate
            } else {
                true
            }
            
            // Filter by location
            val passesLocationFilter = slot.location.equals(preferences.targetLocation, ignoreCase = true)
            
            passesDateFilter && passesLocationFilter
        }
    }
    
    private suspend fun getApplicantForBooking(preferences: Preferences): Applicant? {
        // Find an applicant matching the visa type and location preferences
        val applicants = applicantRepository.getAllApplicants()
        return applicants.firstOrNull { applicant ->
            applicant.visaType.equals(preferences.targetVisaType, ignoreCase = true) &&
            applicant.location.equals(preferences.targetLocation, ignoreCase = true)
        }
    }
    
    private suspend fun attemptBookingWithRetry(
        webView: WebView,
        applicant: Applicant,
        slot: AppointmentSlot,
        maxRetries: Int
    ): Result<Boolean> {
        for (attempt in 1..maxRetries) {
            try {
                Log.d(TAG, "Booking attempt $attempt/$maxRetries")
                
                // Simulate human behavior before each attempt
                simulateHumanBehavior()
                
                // Navigate through the booking process
                val navigationSuccess = navigateToBookingPage(webView)
                if (!navigationSuccess) {
                    Log.w(TAG, "Failed to navigate to booking page on attempt $attempt")
                    if (attempt < maxRetries) {
                        delay((5000 * attempt).toLong())
                    }
                    continue
                }
                
                // Fill form fields dynamically using heuristics
                val formFilled = fillBookingForm(webView, applicant, slot)
                if (!formFilled) {
                    Log.w(TAG, "Failed to fill booking form on attempt $attempt")
                    if (attempt < maxRetries) {
                        delay((5000 * attempt).toLong())
                    }
                    continue
                }
                
                // Handle CAPTCHA if present
                val captchaSolved = captchaHandler.handleCaptchaChallenge(webView)
                if (!captchaSolved) {
                    Log.w(TAG, "Failed to solve CAPTCHA on attempt $attempt")
                    if (attempt < maxRetries) {
                        delay((5000 * attempt).toLong())
                    }
                    continue
                }
                
                // Submit the booking
                val bookingSuccess = submitBooking(webView)
                
                if (bookingSuccess) {
                    Log.d(TAG, "Booking successful!")
                    
                    // Update applicant status
                    val updatedApplicant = applicant.copy(
                        status = "booked",
                        appointmentDate = slot.date,
                        appointmentTime = slot.time
                    )
                    applicantRepository.updateApplicant(updatedApplicant)
                    
                    // Send confirmation email
                    bookingRepository.sendBookingConfirmationEmail(applicant, slot)
                    
                    return Result.success(true)
                } else {
                    Log.w(TAG, "Booking submission failed on attempt $attempt")
                }
                
                // If we get here, the booking failed
                if (attempt < maxRetries) {
                    // Wait before retrying
                    delay((5000 * attempt).toLong())
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Booking attempt $attempt failed", e)
                if (attempt == maxRetries) {
                    return Result.failure(e)
                }
                delay((5000 * attempt).toLong())
            }
        }
        
        return Result.success(false)
    }
    
    private suspend fun navigateToBookingPage(webView: WebView): Boolean {
        // Based on the source code files, this would involve:
        // 1. Navigating to the login page
        // 2. Handling login (if needed)
        // 3. Navigating to the appointment booking page
        // 4. Handling any intermediate pages or modals
        
        Log.d(TAG, "Navigating to booking page")
        
        // Simulate human-like navigation delays
        delay((2000..5000).random().toLong())
        
        // Execute JavaScript to navigate through the booking process
        // Based on the source code patterns from SOURCE C.pdf and other files
        webView.evaluateJavascript(
            """
            (function() {
                try {
                    // Show various elements that might be hidden (based on source code patterns)
                    var showFunctions = [
                        'yupjhky', 'xjhhem', 'asfslpz', 'vwhsyk', 'dmtrrma', 'exyctpho',
                        'djwowxvy', 'iavwpx', 'qralttum', 'ntakl', 'neizna', 'ictzgl',
                        'qxferwqom', 'hgoio', 'dgkgtw', 'ogwiyxlw', 'kelqrxopt', 'ozgpa'
                    ];
                    
                    for (var i = 0; i < showFunctions.length; i++) {
                        try {
                            if (typeof window[showFunctions[i]] === 'function') {
                                window[showFunctions[i]]();
                            }
                        } catch (e) {
                            // Continue with next function
                        }
                    }
                    
                    // Additional navigation logic based on SOURCE.pdf
                    // This would involve clicking navigation elements
                    var navElements = document.querySelectorAll('.nav-link, .btn-primary, .btn-success');
                    if (navElements.length > 0) {
                        // Click the first navigation element as an example
                        var event = new MouseEvent('click', {
                            view: window,
                            bubbles: true,
                            cancelable: true
                        });
                        navElements[0].dispatchEvent(event);
                    }
                    
                    return "Navigation elements shown and clicked";
                } catch (e) {
                    return "Error: " + e.message;
                }
            })();
            """.trimIndent()
        ) { result ->
            Log.d(TAG, "Navigation result: $result")
        }
        
        delay((1000..2000).random().toLong())
        return true // Placeholder
    }
    
    private suspend fun fillBookingForm(
        webView: WebView,
        applicant: Applicant,
        slot: AppointmentSlot
    ): Boolean {
        // Based on the source code, this would use heuristics to identify form fields
        // by their labels, classes, IDs, etc.
        
        Log.d(TAG, "Filling booking form for applicant: ${applicant.fullName}")
        
        // Simulate human-like typing delays
        delay((1000..3000).random().toLong())
        
        // Execute JavaScript to fill form fields
        // Based on patterns from SOURCE.pdf and other source files
        webView.evaluateJavascript(
            """
            (function() {
                try {
                    // Fill applicant information using heuristics
                    // Look for common field patterns based on the source code
                    
                    // Name field
                    var nameFields = document.querySelectorAll('input[name*="name"], input[id*="name"], input[placeholder*="name"], .name-input');
                    for (var i = 0; i < nameFields.length; i++) {
                        if (nameFields[i].type !== 'hidden' && !nameFields[i].value) {
                            nameFields[i].value = "${applicant.fullName}";
                            // Trigger change event
                            nameFields[i].dispatchEvent(new Event('change', { bubbles: true }));
                            break;
                        }
                    }
                    
                    // Email field
                    var emailFields = document.querySelectorAll('input[type="email"], input[name*="email"], input[id*="email"], .email-input');
                    for (var i = 0; i < emailFields.length; i++) {
                        if (emailFields[i].type !== 'hidden' && !emailFields[i].value) {
                            emailFields[i].value = "${applicant.email}";
                            // Trigger change event
                            emailFields[i].dispatchEvent(new Event('change', { bubbles: true }));
                            break;
                        }
                        }
                    
                    // Passport number
                    var passportFields = document.querySelectorAll('input[name*="passport"], input[id*="passport"], input[placeholder*="passport"], .passport-input');
                    for (var i = 0; i < passportFields.length; i++) {
                        if (passportFields[i].type !== 'hidden' && !passportFields[i].value) {
                            passportFields[i].value = "${applicant.passportNumber}";
                            // Trigger change event
                            passportFields[i].dispatchEvent(new Event('change', { bubbles: true }));
                            break;
                        }
                    }
                    
                    // Nationality
                    var nationalityFields = document.querySelectorAll('input[name*="nationality"], input[id*="nationality"], select[name*="nationality"], .nationality-input');
                    for (var i = 0; i < nationalityFields.length; i++) {
                        if (nationalityFields[i].type === 'select-one') {
                            // For dropdowns, try to select by value or text
                            var options = nationalityFields[i].options;
                            for (var j = 0; j < options.length; j++) {
                                if (options[j].text.toLowerCase().includes("${applicant.nationality.lowercase()}") ||
                                    options[j].value.toLowerCase().includes("${applicant.nationality.lowercase()}")) {
                                    nationalityFields[i].selectedIndex = j;
                                    // Trigger change event
                                    nationalityFields[i].dispatchEvent(new Event('change', { bubbles: true }));
                                    break;
                                }
                            }
                        } else if (nationalityFields[i].type !== 'hidden' && !nationalityFields[i].value) {
                            nationalityFields[i].value = "${applicant.nationality}";
                            // Trigger change event
                            nationalityFields[i].dispatchEvent(new Event('change', { bubbles: true }));
                        }
                    }
                    
                    // Select appointment date (based on slot)
                    // This would involve interacting with calendar widgets
                    var dateFields = document.querySelectorAll('input[type="date"], input[name*="date"], .k-calendar input, .date-input');
                    for (var i = 0; i < dateFields.length; i++) {
                        if (dateFields[i].type !== 'hidden' && !dateFields[i].value) {
                            dateFields[i].value = "${slot.date}";
                            // Trigger change event
                            dateFields[i].dispatchEvent(new Event('change', { bubbles: true }));
                            break;
                        }
                    }
                    
                    // Select appointment time
                    var timeFields = document.querySelectorAll('select[name*="time"], select[id*="time"], input[name*="time"], .time-input');
                    for (var i = 0; i < timeFields.length; i++) {
                        if (timeFields[i].type === 'select-one') {
                            var options = timeFields[i].options;
                            for (var j = 0; j < options.length; j++) {
                                if (options[j].text.includes("${slot.time}") ||
                                    options[j].value.includes("${slot.time}") ||
                                    options[j].text.toLowerCase().includes("${slot.time.lowercase().substring(0, 3)}")) {
                                    timeFields[i].selectedIndex = j;
                                    // Trigger change event
                                    timeFields[i].dispatchEvent(new Event('change', { bubbles: true }));
                                    break;
                                }
                            }
                        } else if (timeFields[i].type !== 'hidden' && !timeFields[i].value) {
                            timeFields[i].value = "${slot.time}";
                            // Trigger change event
                            timeFields[i].dispatchEvent(new Event('change', { bubbles: true }));
                        }
                    }
                    
                    return "Form filled successfully";
                } catch (e) {
                    return "Error filling form: " + e.message;
                }
            })();
            """.trimIndent()
        ) { result ->
            Log.d(TAG, "Form filling result: $result")
        }
        
        delay((500..1500).random().toLong())
        return true // Placeholder
    }
    
    private suspend fun submitBooking(webView: WebView): Boolean {
        // This would contain the actual booking submission logic
        
        Log.d(TAG, "Submitting booking")
        
        // Simulate human-like delays before submission
        delay((2000..4000).random().toLong())
        
        // Execute JavaScript to submit the form
        webView.evaluateJavascript(
            """
            (function() {
                try {
                    // Look for submit buttons using various heuristics
                    var submitButtons = document.querySelectorAll(
                        'button[type="submit"], input[type="submit"], .submit, .btn-primary, [value="Submit"], .book-button'
                    );
                    
                    if (submitButtons.length > 0) {
                        // Click the first submit button
                        var event = new MouseEvent('click', {
                            view: window,
                            bubbles: true,
                            cancelable: true
                        });
                        submitButtons[0].dispatchEvent(event);
                        return "Booking submitted";
                    }
                    
                    // If no explicit submit button found, try to submit the form
                    var forms = document.querySelectorAll('form');
                    if (forms.length > 0) {
                        forms[0].submit();
                        return "Form submitted";
                    }
                    
                    return "No submit button or form found";
                } catch (e) {
                    return "Error submitting booking: " + e.message;
                }
            })();
            """.trimIndent()
        ) { result ->
            Log.d(TAG, "Booking submission result: $result")
        }
        
        // Wait for submission to process
        delay((3000..7000).random().toLong())
        
        // Check if booking was successful
        return checkBookingSuccess(webView)
    }
    
    private suspend fun checkBookingSuccess(webView: WebView): Boolean {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            webView.evaluateJavascript(
                """
                (function() {
                    try {
                        // Look for success indicators
                        var successIndicators = document.querySelectorAll(
                            '.success, .alert-success, [class*="success"], [id*="success"], .confirmation'
                        );
                        
                        // Look for confirmation messages
                        var confirmationText = document.body.textContent || document.body.innerText;
                        var isConfirmed = confirmationText.toLowerCase().includes('confirm') || 
                                         confirmationText.toLowerCase().includes('success') ||
                                         confirmationText.toLowerCase().includes('booked') ||
                                         confirmationText.toLowerCase().includes('appointment') ||
                                         confirmationText.toLowerCase().includes('confirmation');
                        
                        // Look for booking ID or reference number
                        var bookingIdPattern = /\b[A-Z0-9]{6,12}\b/;
                        var hasBookingId = bookingIdPattern.test(confirmationText);
                        
                        return successIndicators.length > 0 || isConfirmed || hasBookingId;
                    } catch (e) {
                        return false;
                    }
                })();
                """.trimIndent()
            ) { result ->
                try {
                    val isSuccess = result.toBoolean()
                    continuation.resume(isSuccess)
                } catch (e: Exception) {
                    continuation.resume(false)
                }
            }
        }
    }
    
    private suspend fun simulateHumanBehavior() {
        // Simulate human-like behavior with random delays
        val delay = (500..3000).random().toLong()
        Log.d(TAG, "Simulating human behavior with delay: ${delay}ms")
        delay(delay)
    }
}