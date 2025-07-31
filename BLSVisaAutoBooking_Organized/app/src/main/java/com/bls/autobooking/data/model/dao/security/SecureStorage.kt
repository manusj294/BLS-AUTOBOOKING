package com.bls.autobooking.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureStorage(private val context: Context) {
    private val TAG = "SecureStorage"
    private val KEY_ALIAS = "bls_autobooking_key"
    private val PREFS_NAME = "bls_secure_prefs"
    
    private lateinit var encryptedPrefs: SharedPreferences
    private lateinit var masterKey: MasterKey
    
    init {
        initializeEncryptedStorage()
    }
    
    private fun initializeEncryptedStorage() {
        try {
            masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing encrypted storage", e)
            // Fallback to regular preferences (less secure)
            encryptedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    
    fun saveSensitiveData(key: String, value: String) {
        try {
            encryptedPrefs.edit()
                .putString(key, value)
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving sensitive data", e)
        }
    }
    
    fun getSensitiveData(key: String): String? {
        return try {
            encryptedPrefs.getString(key, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving sensitive data", e)
            null
        }
    }
    
    fun saveEncryptedData(key: String, plaintext: String): Boolean {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray())
            val iv = cipher.iv
            
            // Combine IV and encrypted data
            val combined = iv + encryptedBytes
            val encoded = Base64.encodeToString(combined, Base64.DEFAULT)
            
            saveSensitiveData(key, encoded)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting data", e)
            false
        }
    }
    
    fun getDecryptedData(key: String): String? {
        return try {
            val encoded = getSensitiveData(key) ?: return null
            val combined = Base64.decode(encoded, Base64.DEFAULT)
            
            // Extract IV (first 12 bytes for GCM)
            val iv = combined.copyOfRange(0, 12)
            val encryptedBytes = combined.copyOfRange(12, combined.size)
            
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting data", e)
            null
        }
    }
    
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }
    
    fun clearAllData() {
        try {
            encryptedPrefs.edit().clear().apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing secure storage", e)
        }
    }
}