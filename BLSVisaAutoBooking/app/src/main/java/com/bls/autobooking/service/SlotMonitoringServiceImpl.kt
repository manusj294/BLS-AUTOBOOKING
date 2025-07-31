package com.bls.autobooking.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.bls.autobooking.R
import com.bls.autobooking.captcha.CaptchaHandler
import com.bls.autobooking.data.database.AppDatabase
import com.bls.autobooking.engine.BookingEngineImpl
import com.bls.autobooking.repository.ApplicantRepository
import com.bls.autobooking.repository.BookingRepository
import com.bls.autobooking.repository.CaptchaRepository
import com.bls.autobooking.repository.PreferencesRepository
import com.bls.autobooking.scraper.BlsWebsiteScraper
import com.bls.autobooking.webview.WebViewManager
import kotlinx.coroutines.*

class SlotMonitoringServiceImpl : LifecycleService() {
    private val TAG = "SlotMonitoringServiceImpl"
    private val CHANNEL_ID = "SlotMonitoringServiceChannel"
    private val NOTIFICATION_ID = 1
    
    private lateinit var database: AppDatabase
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var applicantRepository: ApplicantRepository
    private lateinit var bookingRepository: BookingRepository
    private lateinit var captchaRepository: CaptchaRepository
    private lateinit var scraper: BlsWebsiteScraper
    private lateinit var captchaHandler: CaptchaHandler
    private lateinit var bookingEngine: BookingEngineImpl
    private lateinit var webView: WebView
    private lateinit var webViewManager: WebViewManager
    
    private var isMonitoring = false
    private var monitoringJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SlotMonitoringServiceImpl created")
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Initialize dependencies
        database = AppDatabase.getDatabase(this)
        preferencesRepository = PreferencesRepository(database.preferencesDao())
        applicantRepository = ApplicantRepository(database.applicantDao())
        bookingRepository = BookingRepository()
        captchaRepository = CaptchaRepository()
        scraper = BlsWebsiteScraper()
        captchaHandler = CaptchaHandler(captchaRepository)
        bookingEngine = BookingEngineImpl(
            applicantRepository, 
            bookingRepository, 
            scraper, 
            captchaHandler
        )
        
        // Initialize WebView with proper settings for the BLS website
        initializeWebView()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "SlotMonitoringServiceImpl started")
        
        startMonitoring()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SlotMonitoringServiceImpl destroyed")
        stopMonitoring()
        webViewManager.cleanup()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Slot Monitoring Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
    
    private fun createNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BLS Visa Auto-Booking")
            .setContentText("Monitoring for appointment slots...")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }
    
    private fun initializeWebView() {
        webView = WebView(this)
        webViewManager = WebViewManager(this)
        webViewManager.initializeWebView(webView)
        
        Log.d(TAG, "WebView initialized")
    }
    
    private fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = lifecycleScope.launch {
            while (isMonitoring) {
                try {
                    Log.d(TAG, "Checking for available slots...")
                    
                    // Get current preferences
                    val preferences = withContext(Dispatchers.IO) {
                        preferencesRepository.getPreferences().firstOrNull()
                    }
                    
                    if (preferences != null) {
                        // Check for available slots and attempt booking
                        val result = bookingEngine.autoBookAppointment(webView, preferences)
                        
                        if (result.isSuccess) {
                            val bookingSuccess = result.getOrNull()
                            if (bookingSuccess == true) {
                                Log.d(TAG, "Successfully booked appointment")
                                // Optionally stop monitoring after successful booking
                                // stopMonitoring()
                            } else {
                                Log.d(TAG, "No slots booked in this cycle")
                            }
                        } else {
                            Log.e(TAG, "Error during slot monitoring", result.exceptionOrNull())
                        }
                    } else {
                        Log.d(TAG, "No preferences found, waiting for configuration")
                    }
                    
                    // Wait for 60 seconds before next check (as per specs)
                    delay(60000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error during slot monitoring", e)
                    delay(10000) // Wait 10 seconds before retrying on error
                }
            }
        }
    }
    
    private fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
    }
    
    companion object {
        fun startService(context: Context) {
            val startIntent = Intent(context, SlotMonitoringServiceImpl::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startIntent)
            } else {
                context.startService(startIntent)
            }
        }
        
        fun stopService(context: Context) {
            val stopIntent = Intent(context, SlotMonitoringServiceImpl::class.java)
            context.stopService(stopIntent)
        }
    }
}