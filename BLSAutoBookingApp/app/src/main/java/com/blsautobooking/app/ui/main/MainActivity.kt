package com.blsautobooking.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.blsautobooking.app.databinding.ActivityMainBinding
import com.blsautobooking.app.workers.AppointmentMonitorWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startMonitoringButton.setOnClickListener {
            startMonitoring()
        }

        binding.stopMonitoringButton.setOnClickListener {
            stopMonitoring()
        }
    }

    private fun startMonitoring() {
        val workRequest = PeriodicWorkRequestBuilder<AppointmentMonitorWorker>(15, TimeUnit.MINUTES) // Minimum 15 minutes for periodic work
            .addTag("appointment_monitoring_work")
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AppointmentMonitor",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        binding.monitoringStatusTextView.text = "Monitoring Status: Active"
    }

    private fun stopMonitoring() {
        WorkManager.getInstance(applicationContext).cancelUniqueWork("AppointmentMonitor")
        binding.monitoringStatusTextView.text = "Monitoring Status: Inactive"
    }
}

