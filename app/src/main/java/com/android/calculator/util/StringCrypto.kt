package com.android.calculator.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * String encryption utility for runtime obfuscation
 * This class provides string encryption/decryption to hide sensitive strings at runtime
 * 
 * Usage:
 * - Use encryptString() during build time to encrypt sensitive strings
 * - Use decrypt() at runtime to get the original strings
 * - All strings remain encrypted in the APK until actually needed
 */
object StringCrypto {
    
    // Obfuscated key components (split to avoid detection)
    private const val KEY_PART_1 = "Q2FsY3VsYXRvcl9TZWN1cmVfa2V5XzIwMjU="
    private const val KEY_PART_2 = "X0FudGlfRGVidWdfUHJvdGVjdGlvbl9LZXk="
    
    // XOR key for additional obfuscation
    private const val XOR_KEY = 0x5A
    
    /**
     * Generates a secure key from obfuscated components
     */
    private fun generateKey(): SecretKey {
        val keyData = (String(Base64.decode(KEY_PART_1, Base64.DEFAULT)) + 
                      String(Base64.decode(KEY_PART_2, Base64.DEFAULT))).toByteArray()
        
        // Apply XOR obfuscation
        for (i in keyData.indices) {
            keyData[i] = (keyData[i].toInt() xor XOR_KEY).toByte()
        }
        
        // Use first 16 bytes for AES-128
        val key = keyData.copyOf(16)
        return SecretKeySpec(key, "AES")
    }
    
    /**
     * Encrypts a string for build-time obfuscation
     * This would typically be called by a build script
     */
    fun encryptString(plaintext: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = generateKey()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray())
        
        // Combine IV + encrypted data
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }
    
    /**
     * Decrypts a string at runtime
     * This is called by the application to get original strings
     */
    fun decrypt(encryptedData: String): String {
        try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            
            // Extract IV (first 16 bytes) and encrypted data
            val iv = combined.copyOf(16)
            val encrypted = combined.copyOfRange(16, combined.size)
            
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val key = generateKey()
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            
            val decrypted = cipher.doFinal(encrypted)
            return String(decrypted)
        } catch (e: Exception) {
            // Return obfuscated string if decryption fails
            return "ERROR_DECRYPT_FAILED"
        }
    }
    
    /**
     * Simple XOR obfuscation for less sensitive strings
     */
    fun xorObfuscate(data: String, key: Int = 0x42): String {
        return data.map { (it.code xor key).toChar() }.joinToString("")
    }
    
    /**
     * Deobfuscate XOR encoded strings
     */
    fun xorDeobfuscate(obfuscated: String, key: Int = 0x42): String {
        return obfuscated.map { (it.code xor key).toChar() }.joinToString("")
    }
    
    /**
     * Base64 + XOR combo obfuscation
     */
    fun comboObfuscate(data: String): String {
        val xored = xorObfuscate(data)
        return Base64.encodeToString(xored.toByteArray(), Base64.NO_WRAP)
    }
    
    /**
     * Deobfuscate Base64 + XOR combo
     */
    fun comboDeobfuscate(obfuscated: String): String {
        val decoded = String(Base64.decode(obfuscated, Base64.DEFAULT))
        return xorDeobfuscate(decoded)
    }
}

/**
 * Pre-encrypted strings for critical security messages
 * These are encrypted at build time and decrypted only when needed
 */
object ObfuscatedStrings {
    
    // Example: "Security threat detected! Terminating application."
    const val SECURITY_THREAT_MESSAGE = "VGhpc19pc19hbl9vYmZ1c2NhdGVkX3N0cmluZw=="
    
    // Example: "Anti-debug initialization failed"
    const val INIT_FAILED_MESSAGE = "QW50aURlYnVnX2luaXRfZmFpbGVk"
    
    // Example: "Debugger detected"
    const val DEBUGGER_DETECTED = "RGVidWdnZXJfZGV0ZWN0ZWQ="
    
    // Example: "Emulator detected"
    const val EMULATOR_DETECTED = "RW11bGF0b3JfZGV0ZWN0ZWQ="
    
    // Example: "Root detected"
    const val ROOT_DETECTED = "Um9vdF9kZXRlY3RlZA=="
    
    // Example: "Tampering detected"
    const val TAMPER_DETECTED = "VGFtcGVyaW5nX2RldGVjdGVk"
    
    /**
     * Get decrypted security message
     */
    fun getSecurityThreatMessage(): String {
        return StringCrypto.comboDeobfuscate(SECURITY_THREAT_MESSAGE)
    }
    
    /**
     * Get decrypted init failed message
     */
    fun getInitFailedMessage(): String {
        return StringCrypto.comboDeobfuscate(INIT_FAILED_MESSAGE)
    }
    
    /**
     * Get decrypted debugger detection message
     */
    fun getDebuggerDetectedMessage(): String {
        return StringCrypto.comboDeobfuscate(DEBUGGER_DETECTED)
    }
    
    /**
     * Get decrypted emulator detection message
     */
    fun getEmulatorDetectedMessage(): String {
        return StringCrypto.comboDeobfuscate(EMULATOR_DETECTED)
    }
    
    /**
     * Get decrypted root detection message
     */
    fun getRootDetectedMessage(): String {
        return StringCrypto.comboDeobfuscate(ROOT_DETECTED)
    }
    
    /**
     * Get decrypted tamper detection message
     */
    fun getTamperDetectedMessage(): String {
        return StringCrypto.comboDeobfuscate(TAMPER_DETECTED)
    }
}
