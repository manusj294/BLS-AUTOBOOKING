package com.blsautobooking.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityHelper(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "bls_autobooking_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveCredentials(username: String, password: String) {
        try {
            encryptedSharedPreferences.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .apply()
            Log.d(TAG, "Credentials saved securely")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving credentials", e)
        }
    }

    fun getUsername(): String? {
        return try {
            encryptedSharedPreferences.getString(KEY_USERNAME, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving username", e)
            null
        }
    }

    fun getPassword(): String? {
        return try {
            encryptedSharedPreferences.getString(KEY_PASSWORD, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving password", e)
            null
        }
    }

    fun clearCredentials() {
        try {
            encryptedSharedPreferences.edit()
                .remove(KEY_USERNAME)
                .remove(KEY_PASSWORD)
                .apply()
            Log.d(TAG, "Credentials cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing credentials", e)
        }
    }

    fun saveEmailJSConfig(serviceId: String, templateId: String, publicKey: String) {
        try {
            encryptedSharedPreferences.edit()
                .putString(KEY_EMAILJS_SERVICE_ID, serviceId)
                .putString(KEY_EMAILJS_TEMPLATE_ID, templateId)
                .putString(KEY_EMAILJS_PUBLIC_KEY, publicKey)
                .apply()
            Log.d(TAG, "EmailJS config saved securely")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving EmailJS config", e)
        }
    }

    fun getEmailJSServiceId(): String? {
        return try {
            encryptedSharedPreferences.getString(KEY_EMAILJS_SERVICE_ID, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving EmailJS service ID", e)
            null
        }
    }

    fun getEmailJSTemplateId(): String? {
        return try {
            encryptedSharedPreferences.getString(KEY_EMAILJS_TEMPLATE_ID, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving EmailJS template ID", e)
            null
        }
    }

    fun getEmailJSPublicKey(): String? {
        return try {
            encryptedSharedPreferences.getString(KEY_EMAILJS_PUBLIC_KEY, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving EmailJS public key", e)
            null
        }
    }

    companion object {
        private const val TAG = "SecurityHelper"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_EMAILJS_SERVICE_ID = "emailjs_service_id"
        private const val KEY_EMAILJS_TEMPLATE_ID = "emailjs_template_id"
        private const val KEY_EMAILJS_PUBLIC_KEY = "emailjs_public_key"
    }
}

