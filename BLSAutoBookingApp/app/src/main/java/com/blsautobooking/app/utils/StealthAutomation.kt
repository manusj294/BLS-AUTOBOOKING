package com.blsautobooking.app.utils

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.random.Random

object StealthAutomation {

    private const val TAG = "StealthAutomation"

    /**
     * Introduces a random delay to simulate human-like pauses between actions.
     * @param minMillis Minimum delay in milliseconds.
     * @param maxMillis Maximum delay in milliseconds.
     */
    suspend fun humanLikeDelay(minMillis: Long = 500, maxMillis: Long = 2000) {
        val delayTime = Random.nextLong(minMillis, maxMillis)
        Log.d(TAG, "Introducing human-like delay for $delayTime ms")
        delay(delayTime)
    }

    /**
     * Generates a random coordinate within a given range to simulate mouse movement.
     * This can be used to simulate moving the mouse before clicking an element.
     * @param minX Minimum X coordinate.
     * @param maxX Maximum X coordinate.
     * @param minY Minimum Y coordinate.
     * @param maxY Maximum Y coordinate.
     * @return A Pair of (x, y) coordinates.
     */
    fun getRandomCoordinates(minX: Int, maxX: Int, minY: Int, maxY: Int): Pair<Int, Int> {
        val x = Random.nextInt(minX, maxX + 1)
        val y = Random.nextInt(minY, maxY + 1)
        Log.d(TAG, "Generated random coordinates: ($x, $y)")
        return Pair(x, y)
    }

    /**
     * Simulates human-like typing by introducing delays between characters.
     * @param text The text to type.
     * @param delayPerCharMillis Delay in milliseconds between each character.
     */
    suspend fun humanLikeTyping(text: String, delayPerCharMillis: Long = 100) {
        Log.d(TAG, "Simulating human-like typing for text of length ${text.length}")
        for (char in text) {
            // In a real WebView interaction, you would send each character one by one
            // For this simulated environment, we just delay.
            delay(delayPerCharMillis)
        }
    }

    /**
     * Simulates random scrolling behavior within a WebView.
     * @param webViewHelper The WebViewHelper instance.
     * @param scrollAmountPx The amount to scroll in pixels.
     * @param scrollIterations The number of times to scroll.
     */
    suspend fun humanLikeScroll(webViewHelper: WebViewHelper, scrollAmountPx: Int, scrollIterations: Int) {
        Log.d(TAG, "Simulating human-like scrolling")
        repeat(scrollIterations) {
            val direction = if (Random.nextBoolean()) 1 else -1 // 1 for down, -1 for up
            val actualScrollAmount = scrollAmountPx * direction
            // In a real WebView, you would execute JavaScript to scroll
            // webViewHelper.executeJavaScript("window.scrollBy(0, $actualScrollAmount);")
            Log.d(TAG, "Scrolling by $actualScrollAmount px")
            humanLikeDelay(200, 800)
        }
    }

    /**
     * Attempts to bypass simple bot detection by changing user agent strings or other headers.
     * This would typically be set up when initializing the WebView.
     * @param webViewHelper The WebViewHelper instance.
     */
    fun bypassBotDetection(webViewHelper: WebViewHelper) {
        // The user agent is already set in WebViewHelper.initializeWebView()
        // Further enhancements could include managing cookies, headers, etc.
        Log.d(TAG, "Applying bot detection bypass techniques.")
    }
}

