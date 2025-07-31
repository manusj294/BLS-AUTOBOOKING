package com.blsautobooking.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.blsautobooking.app.R
import com.blsautobooking.app.network.EmailService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "bls_autobooking_channel"
    private val NOTIFICATION_ID = 101
    private val emailService = EmailService()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BLS AutoBooking Notifications"
            val descriptionText = "Notifications for BLS Visa AutoBooking App"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showAppointmentFoundNotification(slotCount: Int) {
        val message = context.resources.getQuantityString(R.plurals.appointment_found_message, slotCount, slotCount)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }

        // Also send email notification
        CoroutineScope(Dispatchers.IO).launch {
            emailService.sendNotificationEmail("Appointment Found", message)
        }
    }

    fun showBookingSuccessNotification(applicantName: String) {
        val message = context.getString(R.string.booking_successful_message, applicantName)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID + 1, builder.build())
        }

        // Also send email notification
        CoroutineScope(Dispatchers.IO).launch {
            emailService.sendNotificationEmail("Booking Successful", message)
        }
    }

    fun showBookingFailedNotification(applicantName: String, error: String?) {
        val message = context.getString(R.string.booking_failed_message, applicantName, error)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID + 2, builder.build())
        }

        // Also send email notification
        CoroutineScope(Dispatchers.IO).launch {
            emailService.sendNotificationEmail("Booking Failed", message)
        }
    }

    fun showCaptchaFailedNotification(error: String?) {
        val message = context.getString(R.string.captcha_failed_message, error)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID + 3, builder.build())
        }

        // Also send email notification
        CoroutineScope(Dispatchers.IO).launch {
            emailService.sendNotificationEmail("CAPTCHA Failed", message)
        }
    }
}

