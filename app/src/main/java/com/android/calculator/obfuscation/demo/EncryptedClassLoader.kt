package com.android.calculator.obfuscation.demo

import android.content.Context
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

/**
 * EncryptedClassLoader - Demonstrates runtime class decryption and loading
 * This class handles the decryption and dynamic loading of encrypted classes
 */
object EncryptedClassLoader {
    
    private const val TAG = "EncryptedClassLoader"
    private const val ENCRYPTION_KEY = "SecureClassKey2025!"
    private const val ENCRYPTED_CLASS_DIR = "encrypted_classes"
    
    /**
     * Load and decrypt a class at runtime
     */
    fun loadEncryptedClass(context: Context, className: String): Class<*>? {
        return try {
            Log.d(TAG, "Loading encrypted class: $className")
            
            // Try to load from assets first
            val encryptedData = loadEncryptedClassFromAssets(context, className)
            if (encryptedData != null) {
                val decryptedBytes = decryptClassBytes(encryptedData)
                val loadedClass = defineClass(decryptedBytes, className)
                Log.d(TAG, "Successfully loaded class from assets: $className")
                return loadedClass
            }
            
            // Try to load from internal storage
            val encryptedFile = File(context.filesDir, "$ENCRYPTED_CLASS_DIR/$className.enc")
            if (encryptedFile.exists()) {
                val encryptedBytes = encryptedFile.readBytes()
                val decryptedBytes = decryptClassBytes(encryptedBytes)
                val loadedClass = defineClass(decryptedBytes, className)
                Log.d(TAG, "Successfully loaded class from storage: $className")
                return loadedClass
            }
            
            Log.w(TAG, "Encrypted class not found: $className")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load encrypted class: $className", e)
            null
        }
    }
    
    /**
     * Load encrypted class data from assets
     */
    private fun loadEncryptedClassFromAssets(context: Context, className: String): ByteArray? {
        return try {
            val assetName = "$ENCRYPTED_CLASS_DIR/$className.enc"
            context.assets.open(assetName).use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Class not found in assets: $className")
            null
        }
    }
    
    /**
     * Decrypt class bytes using AES
     */
    private fun decryptClassBytes(encryptedBytes: ByteArray): ByteArray {
        return try {
            val key = SecretKeySpec(ENCRYPTION_KEY.toByteArray().sliceArray(0..15), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key)
            cipher.doFinal(encryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt class bytes", e)
            encryptedBytes // Return original if decryption fails
        }
    }
    
    /**
     * Define a class from bytecode
     */
    private fun defineClass(classBytes: ByteArray, className: String): Class<*>? {
        return try {
            // Create a custom class loader
            val classLoader = object : ClassLoader() {
                override fun findClass(name: String): Class<*> {
                    if (name == className) {
                        return defineClass(name, classBytes, 0, classBytes.size)
                    }
                    return super.findClass(name)
                }
            }
            
            classLoader.loadClass(className)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to define class: $className", e)
            null
        }
    }
    
    /**
     * Encrypt and store a class for demonstration
     */
    fun encryptAndStoreClass(context: Context, className: String, classBytes: ByteArray) {
        try {
            val key = SecretKeySpec(ENCRYPTION_KEY.toByteArray().sliceArray(0..15), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(classBytes)
            
            // Store in internal storage
            val encryptedDir = File(context.filesDir, ENCRYPTED_CLASS_DIR)
            if (!encryptedDir.exists()) {
                encryptedDir.mkdirs()
            }
            
            val encryptedFile = File(encryptedDir, "$className.enc")
            encryptedFile.writeBytes(encryptedBytes)
            
            Log.d(TAG, "Encrypted and stored class: $className")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt and store class: $className", e)
        }
    }
    
    /**
     * Create a demo instance of SecureUtility
     */
    fun createSecureUtilityInstance(context: Context): Any? {
        return try {
            val secureUtilityClass = loadEncryptedClass(context, "com.android.calculator.obfuscation.demo.SecureUtility")
            if (secureUtilityClass != null) {
                secureUtilityClass.getDeclaredConstructor().newInstance()
            } else {
                // Fallback: create instance directly if encrypted version not available
                Log.d(TAG, "Using fallback SecureUtility instance")
                SecureUtility()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create SecureUtility instance", e)
            null
        }
    }
    
    /**
     * Demonstrate the encrypted class loading
     */
    fun demonstrateClassLoading(context: Context) {
        try {
            Log.d(TAG, "Starting class loading demonstration...")
            
            val secureUtility = createSecureUtilityInstance(context)
            if (secureUtility != null) {
                // Test the loaded class
                val method = secureUtility.javaClass.getMethod("performSecureCalculation", Int::class.java)
                val result = method.invoke(secureUtility, 10) as Int
                Log.d(TAG, "SecureUtility calculation result: $result")
                
                val getSecureStringMethod = secureUtility.javaClass.getMethod("getSecureString")
                val secureString = getSecureStringMethod.invoke(secureUtility) as String
                Log.d(TAG, "SecureUtility secure string: $secureString")
                
                val verifyMethod = secureUtility.javaClass.getMethod("verifySecurityStatus")
                val isSecure = verifyMethod.invoke(secureUtility) as Boolean
                Log.d(TAG, "SecureUtility security status: $isSecure")
                
                val classInfoMethod = secureUtility.javaClass.getMethod("getClassInfo")
                val classInfo = classInfoMethod.invoke(secureUtility) as String
                Log.d(TAG, "SecureUtility class info: $classInfo")
                
                Log.d(TAG, "Class loading demonstration completed successfully")
            } else {
                Log.w(TAG, "Failed to create SecureUtility instance for demonstration")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Class loading demonstration failed", e)
        }
    }
}
