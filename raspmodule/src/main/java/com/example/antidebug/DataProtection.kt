package com.example.raspsdk

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.random.Random

/**
 * DataProtection - Secure data storage and encryption utilities
 * 
 * This class provides secure storage mechanisms:
 * - AES encryption using Android Keystore
 * - Encrypted SharedPreferences
 * - In-memory obfuscation techniques
 * - Secure key management
 * - Data integrity verification
 */
class DataProtection(private val context: Context) {
    
    companion object {
        private const val TAG = "DataProtection"
        private const val KEYSTORE_ALIAS = "RASPSDK_Key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        
        // Obfuscation constants
        private const val XOR_KEY = 0xAB.toByte()
        private val OBFUSCATION_KEYS = byteArrayOf(
            0x12, 0x34, 0x56, 0x78, 0x9A.toByte(), 0xBC.toByte(), 0xDE.toByte(), 0xF0.toByte()
        )
    }
    
    private var masterKey: MasterKey? = null
    private var encryptedPrefs: SharedPreferences? = null
    
    init {
        initializeMasterKey()
        initializeEncryptedPreferences()
    }
    
    /**
     * Initialize master key for encryption
     */
    private fun initializeMasterKey() {
        try {
            masterKey = MasterKey.Builder(context, KEYSTORE_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            Log.d(TAG, "Master key initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize master key", e)
        }
    }
    
    /**
     * Initialize encrypted SharedPreferences
     */
    private fun initializeEncryptedPreferences() {
        try {
            masterKey?.let { key ->
                encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "anti_debug_secure_prefs",
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                Log.d(TAG, "Encrypted SharedPreferences initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize encrypted preferences", e)
        }
    }
    
    /**
     * Encrypt data using Android Keystore
     */
    fun encryptData(plainText: String): EncryptedData? {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.w(TAG, "Android Keystore encryption requires API level 23+")
                return null
            }
            
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            // Generate key if not exists
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                generateSecretKey()
            }
            
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray())
            
            EncryptedData(
                encryptedBytes = encryptedBytes,
                iv = iv
            )
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            null
        }
    }
    
    /**
     * Decrypt data using Android Keystore
     */
    fun decryptData(encryptedData: EncryptedData): String? {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.w(TAG, "Android Keystore decryption requires API level 23+")
                return null
            }
            
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedBytes = cipher.doFinal(encryptedData.encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            null
        }
    }
    
    /**
     * Generate secret key in Android Keystore
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateSecretKey() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.w(TAG, "KeyGenParameterSpec requires API level 23+")
                return
            }
            
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            
            Log.d(TAG, "Secret key generated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate secret key", e)
        }
    }
    
    /**
     * Store encrypted value in SharedPreferences
     */
    fun storeSecureValue(key: String, value: String): Boolean {
        return try {
            encryptedPrefs?.edit()?.putString(key, value)?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store secure value", e)
            false
        }
    }
    
    /**
     * Retrieve encrypted value from SharedPreferences
     */
    fun getSecureValue(key: String, defaultValue: String? = null): String? {
        return try {
            encryptedPrefs?.getString(key, defaultValue)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve secure value", e)
            defaultValue
        }
    }
    
    /**
     * Store secure integer value
     */
    fun storeSecureInt(key: String, value: Int): Boolean {
        return try {
            encryptedPrefs?.edit()?.putInt(key, value)?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store secure int", e)
            false
        }
    }
    
    /**
     * Retrieve secure integer value
     */
    fun getSecureInt(key: String, defaultValue: Int = 0): Int {
        return try {
            encryptedPrefs?.getInt(key, defaultValue) ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve secure int", e)
            defaultValue
        }
    }
    
    /**
     * Store secure boolean value
     */
    fun storeSecureBoolean(key: String, value: Boolean): Boolean {
        return try {
            encryptedPrefs?.edit()?.putBoolean(key, value)?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store secure boolean", e)
            false
        }
    }
    
    /**
     * Retrieve secure boolean value
     */
    fun getSecureBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            encryptedPrefs?.getBoolean(key, defaultValue) ?: defaultValue
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve secure boolean", e)
            defaultValue
        }
    }
    
    /**
     * Clear all secure data
     */
    fun clearSecureData(): Boolean {
        return try {
            encryptedPrefs?.edit()?.clear()?.apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear secure data", e)
            false
        }
    }
    
    /**
     * Simple XOR obfuscation for in-memory strings
     */
    fun obfuscateString(input: String): ByteArray {
        return try {
            val inputBytes = input.toByteArray()
            val obfuscated = ByteArray(inputBytes.size)
            
            for (i in inputBytes.indices) {
                obfuscated[i] = (inputBytes[i].toInt() xor XOR_KEY.toInt()).toByte()
            }
            
            obfuscated
        } catch (e: Exception) {
            Log.e(TAG, "String obfuscation failed", e)
            ByteArray(0)
        }
    }
    
    /**
     * Deobfuscate XOR obfuscated string
     */
    fun deobfuscateString(obfuscated: ByteArray): String {
        return try {
            val deobfuscated = ByteArray(obfuscated.size)
            
            for (i in obfuscated.indices) {
                deobfuscated[i] = (obfuscated[i].toInt() xor XOR_KEY.toInt()).toByte()
            }
            
            String(deobfuscated)
        } catch (e: Exception) {
            Log.e(TAG, "String deobfuscation failed", e)
            ""
        }
    }
    
    /**
     * Advanced obfuscation using rotating key
     */
    fun advancedObfuscate(input: String): ByteArray {
        return try {
            val inputBytes = input.toByteArray()
            val obfuscated = ByteArray(inputBytes.size)
            
            for (i in inputBytes.indices) {
                val keyIndex = i % OBFUSCATION_KEYS.size
                obfuscated[i] = (inputBytes[i].toInt() xor OBFUSCATION_KEYS[keyIndex].toInt()).toByte()
            }
            
            obfuscated
        } catch (e: Exception) {
            Log.e(TAG, "Advanced obfuscation failed", e)
            ByteArray(0)
        }
    }
    
    /**
     * Advanced deobfuscation using rotating key
     */
    fun advancedDeobfuscate(obfuscated: ByteArray): String {
        return try {
            val deobfuscated = ByteArray(obfuscated.size)
            
            for (i in obfuscated.indices) {
                val keyIndex = i % OBFUSCATION_KEYS.size
                deobfuscated[i] = (obfuscated[i].toInt() xor OBFUSCATION_KEYS[keyIndex].toInt()).toByte()
            }
            
            String(deobfuscated)
        } catch (e: Exception) {
            Log.e(TAG, "Advanced deobfuscation failed", e)
            ""
        }
    }
    
    /**
     * Generate secure random salt
     */
    fun generateSalt(length: Int = 16): ByteArray {
        val salt = ByteArray(length)
        for (i in salt.indices) {
            salt[i] = Random.nextInt(256).toByte()
        }
        return salt
    }
    
    /**
     * Create secure checksum of data
     */
    fun createChecksum(data: String): String {
        return try {
            val hash = data.hashCode()
            val salt = generateSalt(4)
            val combined = hash.toString() + salt.joinToString("") { "%02x".format(it) }
            combined
        } catch (e: Exception) {
            Log.e(TAG, "Checksum creation failed", e)
            ""
        }
    }
    
    /**
     * Verify data integrity using checksum
     */
    fun verifyChecksum(data: String, checksum: String): Boolean {
        return try {
            val currentHash = data.hashCode().toString()
            checksum.startsWith(currentHash)
        } catch (e: Exception) {
            Log.e(TAG, "Checksum verification failed", e)
            false
        }
    }
    
    /**
     * Secure memory class for holding sensitive data
     */
    class SecureMemory(initialValue: String = "") {
        private var obfuscatedData: ByteArray = byteArrayOf()
        private var isValid = true
        
        init {
            if (initialValue.isNotEmpty()) {
                setValue(initialValue)
            }
        }
        
        fun setValue(value: String) {
            try {
                // Simple obfuscation - in production, use more sophisticated methods
                obfuscatedData = ByteArray(value.length)
                for (i in value.indices) {
                    obfuscatedData[i] = (value[i].code xor (XOR_KEY.toInt() + i)).toByte()
                }
                isValid = true
            } catch (e: Exception) {
                Log.e(TAG, "SecureMemory setValue failed", e)
                isValid = false
            }
        }
        
        fun getValue(): String {
            return try {
                if (!isValid) return ""
                
                val deobfuscated = StringBuilder()
                for (i in obfuscatedData.indices) {
                    val originalChar = (obfuscatedData[i].toInt() xor (XOR_KEY.toInt() + i)).toChar()
                    deobfuscated.append(originalChar)
                }
                deobfuscated.toString()
            } catch (e: Exception) {
                Log.e(TAG, "SecureMemory getValue failed", e)
                ""
            }
        }
        
        fun clear() {
            // Overwrite memory with random data
            for (i in obfuscatedData.indices) {
                obfuscatedData[i] = Random.nextInt(256).toByte()
            }
            isValid = false
        }
        
        fun isValid(): Boolean = isValid
    }
    
    /**
     * Create secure memory instance
     */
    fun createSecureMemory(initialValue: String = ""): SecureMemory {
        return SecureMemory(initialValue)
    }
    
    /**
     * Secure configuration storage
     */
    fun storeConfiguration(config: Map<String, Any>): Boolean {
        return try {
            for ((key, value) in config) {
                when (value) {
                    is String -> storeSecureValue("config_$key", value)
                    is Int -> storeSecureInt("config_$key", value)
                    is Boolean -> storeSecureBoolean("config_$key", value)
                    else -> storeSecureValue("config_$key", value.toString())
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Configuration storage failed", e)
            false
        }
    }
    
    /**
     * Retrieve secure configuration
     */
    fun getConfiguration(): Map<String, String> {
        return try {
            val config = mutableMapOf<String, String>()
            encryptedPrefs?.all?.forEach { (key, value) ->
                if (key.startsWith("config_")) {
                    val configKey = key.removePrefix("config_")
                    config[configKey] = value.toString()
                }
            }
            config
        } catch (e: Exception) {
            Log.e(TAG, "Configuration retrieval failed", e)
            emptyMap()
        }
    }
    
    /**
     * Check if keystore is compromised
     */
    fun isKeystoreCompromised(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            
            // Basic compromise checks
            val aliases = keyStore.aliases()
            var suspiciousCount = 0
            
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                // Look for suspicious aliases that might indicate compromise
                if (alias.contains("hook") || alias.contains("frida") || 
                    alias.contains("xposed") || alias.contains("debug")) {
                    suspiciousCount++
                }
            }
            
            // If too many suspicious aliases, keystore might be compromised
            suspiciousCount > 5
        } catch (e: Exception) {
            Log.e(TAG, "Keystore compromise check failed", e)
            true // Assume compromised if check fails
        }
    }
}

/**
 * Container for encrypted data
 */
data class EncryptedData(
    val encryptedBytes: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as EncryptedData
        
        if (!encryptedBytes.contentEquals(other.encryptedBytes)) return false
        if (!iv.contentEquals(other.iv)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = encryptedBytes.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}

