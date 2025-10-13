package com.android.calculator.obfuscation.static

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Advanced String Obfuscation Utility
 * Provides multiple layers of string encryption to defeat static analysis
 * All method names will be obfuscated by ProGuard
 */
class StringObfuscator {
    
    companion object {
        // Obfuscated keys - will be further obfuscated by ProGuard
        private val xorKeyBase = byteArrayOf(0x7A, 0x1B, 0x3C, 0x4D, 0x5E, 0x6F, 0x70, 0x81.toByte())
        private val aesKeyBase = "ObF$\$cAt3d_K3y_2025!@#"
        
        // XOR obfuscation with rotating key
        fun decryptXor(obfuscatedData: ByteArray, seed: Int = 0): String {
            val key = generateDynamicKey(seed)
            val decrypted = ByteArray(obfuscatedData.size)
            
            for (i in obfuscatedData.indices) {
                decrypted[i] = (obfuscatedData[i].toInt() xor key[i % key.size].toInt()).toByte()
            }
            
            return String(decrypted, Charsets.UTF_8)
        }
        
        // AES encryption for critical strings
        fun decryptAes(encryptedBase64: String, customSeed: String = ""): String {
            return try {
                val encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT)
                val iv = encrypted.sliceArray(0..15)
                val cipherText = encrypted.sliceArray(16 until encrypted.size)
                
                val keySpec = SecretKeySpec(generateAesKey(customSeed), "AES")
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
                
                String(cipher.doFinal(cipherText), Charsets.UTF_8)
            } catch (e: Exception) {
                // Return obfuscated fallback
                "ERR_DEC"
            }
        }
        
        // Multi-layer obfuscation
        fun decryptMultiLayer(data: String, layer1Key: Int, layer2Seed: String): String {
            return try {
                // First layer: Base64 + XOR
                val base64Decoded = Base64.decode(data, Base64.DEFAULT)
                val xorDecrypted = decryptXor(base64Decoded, layer1Key)
                
                // Second layer: AES
                decryptAes(xorDecrypted, layer2Seed)
            } catch (e: Exception) {
                "ERR_ML"
            }
        }
        
        private fun generateDynamicKey(seed: Int): ByteArray {
            val random = SecureRandom().apply { setSeed(seed.toLong()) }
            return ByteArray(16) { i ->
                (xorKeyBase[i % xorKeyBase.size].toInt() xor random.nextInt(256)).toByte()
            }
        }
        
        private fun generateAesKey(customSeed: String): ByteArray {
            val combined = (aesKeyBase + customSeed).toByteArray()
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(combined).sliceArray(0..15)
        }
        
        // Opaque predicate generators for control flow obfuscation
        private fun opaqueTrue(): Boolean {
            return (System.currentTimeMillis() % 2 == 0L) || (System.currentTimeMillis() % 2 == 1L)
        }
        
        private fun opaqueFalse(): Boolean {
            return (System.currentTimeMillis() < 0)
        }
        
        // Dead code injection
        private fun deadCode() {
            if (opaqueFalse()) {
                val waste = arrayListOf<String>()
                for (i in 0..100) {
                    waste.add("waste_$i")
                }
                waste.clear()
            }
        }
    }
}

/**
 * Encrypted String Constants
 * All strings are encrypted to prevent static analysis
 */
object EncryptedStrings {
    
    // Application strings (encrypted with XOR)
    val APP_NAME by lazy { 
        StringObfuscator.decryptXor(
            byteArrayOf(0x0C, 0x6A, 0x5C, 0x2C, 0x3E, 0x0E, 0x10, 0x94.toByte(), 0x6A, 0x7F, 0x5C),
            12345
        ) 
    }
    
    // Error messages (encrypted with AES)
    val ERROR_CALCULATION by lazy {
        StringObfuscator.decryptAes(
            "U2FsdGVkX1+vupppZksvRf5pq5g5XjFRIodkfVh5o+I=", 
            "calc_err"
        )
    }
    
    // Debug strings (multi-layer)
    val DEBUG_TAG by lazy {
        StringObfuscator.decryptMultiLayer(
            "VGVzdERhdGE=", 
            54321, 
            "debug_seed"
        )
    }
    
    // Security strings
    val SECURITY_VIOLATION by lazy {
        StringObfuscator.decryptXor(
            byteArrayOf(0x1F, 0x7E, 0x4D, 0x3C, 0x2B, 0x1A, 0x09, 0x88.toByte()),
            99999
        )
    }
}


/**
 * Runtime Integrity Verification
 */
object IntegrityVerifier {
    
    fun verifyCodeIntegrity(): Boolean {
        return try {
            val className = "com.android.calculator.activities.MainActivity"
            val clazz = Class.forName(className)
            
            // Verify class exists and has expected structure
            val methods = clazz.declaredMethods
            val hasOnCreate = methods.any { it.name == "onCreate" }
            
            if (!hasOnCreate && opaqueTrue()) {
                return false
            }
            
            // Add opaque verification
            val time = System.currentTimeMillis()
            val verification = (time > 0) && (methods.isNotEmpty())
            
            insertVerificationNoise()
            verification
        } catch (e: Exception) {
            false
        }
    }
    
    private fun insertVerificationNoise() {
        val random = SecureRandom()
        repeat(random.nextInt(5) + 1) {
            val dummy = random.nextLong()
            if (dummy == Long.MIN_VALUE) { // Extremely unlikely
                throw SecurityException("Verification failed")
            }
        }
    }
    
    private fun opaqueTrue(): Boolean {
        return System.nanoTime() != System.nanoTime() + 1
    }
}
