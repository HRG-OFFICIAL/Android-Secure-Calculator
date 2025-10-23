package com.android.calculator

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate.*
import com.android.calculator.obfuscation.ObfuscationManager
import com.android.calculator.obfuscation.static.ResourceObfuscator
import com.android.calculator.obfuscation.demo.EncryptedClassLoader
import java.io.IOException

class AndroidCalculatorApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize comprehensive obfuscation framework
        if (!BuildConfig.DEBUG) {
            try {
                Log.d("Obfuscation", "Initializing obfuscation framework...")
                ObfuscationManager.initialize(this)
                Log.d("Obfuscation", "Obfuscation framework initialized successfully")
            } catch (e: Exception) {
                Log.e("Obfuscation", "Failed to initialize obfuscation framework", e)
            }
        } else {
            Log.d("Obfuscation", "Debug build detected - Skipping obfuscation initialization")
        }

        // Demonstrate asset decryption at runtime
        if (!BuildConfig.DEBUG) {
            try {
                Log.d("AssetEncryption", "Demonstrating asset decryption...")
                demonstrateAssetDecryption()
                Log.d("AssetEncryption", "Asset decryption demonstration completed")
            } catch (e: Exception) {
                Log.e("AssetEncryption", "Asset decryption demonstration failed", e)
            }
        }

        // Demonstrate runtime class decryption and loading
        if (!BuildConfig.DEBUG) {
            try {
                Log.d("RuntimeClassLoading", "Demonstrating runtime class loading...")
                EncryptedClassLoader.demonstrateClassLoading(this)
                Log.d("RuntimeClassLoading", "Runtime class loading demonstration completed")
            } catch (e: Exception) {
                Log.e("RuntimeClassLoading", "Runtime class loading demonstration failed", e)
            }
        }

        // Demonstrate dynamic code generation for anti-tampering
        if (!BuildConfig.DEBUG) {
            try {
                Log.d("DynamicCodeGeneration", "Demonstrating dynamic code generation...")
                demonstrateDynamicCodeGeneration()
                Log.d("DynamicCodeGeneration", "Dynamic code generation demonstration completed")
            } catch (e: Exception) {
                Log.e("DynamicCodeGeneration", "Dynamic code generation demonstration failed", e)
            }
        }

        // if the theme is overriding the system, the first creation doesn't work properly
        val forceDayNight = MyPreferences(this).forceDayNight
        if (forceDayNight != MODE_NIGHT_UNSPECIFIED && forceDayNight != MODE_NIGHT_FOLLOW_SYSTEM)
            setDefaultNightMode(forceDayNight)
    }

    /**
     * Demonstrate asset decryption at runtime
     * This proves that the asset encryption/decryption pipeline works
     */
    private fun demonstrateAssetDecryption() {
        try {
            // Try to decrypt the encrypted asset
            val encryptedAsset = "secure_config.json.enc"
            val decryptedContent = ResourceObfuscator.AssetEncryption.decryptAsset(this, encryptedAsset)
            
            if (decryptedContent != null) {
                val decryptedString = String(decryptedContent)
                Log.d("AssetEncryption", "Successfully decrypted asset: $decryptedString")
                
                // Simple string verification instead of JSON parsing
                if (decryptedString.contains("api_endpoint") && decryptedString.contains("encryption_key")) {
                    Log.d("AssetEncryption", "Decrypted asset contains expected configuration keys")
                } else {
                    Log.w("AssetEncryption", "Decrypted asset does not contain expected keys")
                }
            } else {
                Log.w("AssetEncryption", "Failed to decrypt asset or asset not found")
            }
        } catch (e: Exception) {
            Log.e("AssetEncryption", "Asset decryption failed", e)
        }
    }

    /**
     * Demonstrate dynamic code generation for anti-tampering checks
     * This proves that the dynamic code generation pipeline works
     */
    private fun demonstrateDynamicCodeGeneration() {
        try {
            val seed = System.currentTimeMillis()
            
            // Generate and execute anti-tampering check
            val antiTamperingCheck = ObfuscationManager.RuntimeObfuscation.generateAntiTamperingCheck(seed)
            val isTampered = antiTamperingCheck()
            Log.d("DynamicCodeGeneration", "Anti-tampering check result: $isTampered")
            
            // Generate and execute integrity verification
            val integrityVerification = ObfuscationManager.RuntimeObfuscation.generateIntegrityVerification(seed + 1)
            val isIntact = integrityVerification()
            Log.d("DynamicCodeGeneration", "Integrity verification result: $isIntact")
            
            // Generate and execute security monitor
            val securityMonitor = ObfuscationManager.RuntimeObfuscation.generateSecurityMonitor(seed + 2)
            val securityStatus = securityMonitor()
            Log.d("DynamicCodeGeneration", "Security monitor status: $securityStatus")
            
            // Demonstrate periodic execution (simulate background monitoring)
            val backgroundMonitor = ObfuscationManager.RuntimeObfuscation.generateSecurityMonitor(seed + 3)
            val backgroundStatus = backgroundMonitor()
            Log.d("DynamicCodeGeneration", "Background security status: $backgroundStatus")
            
        } catch (e: Exception) {
            Log.e("DynamicCodeGeneration", "Dynamic code generation demonstration failed", e)
        }
    }
}
