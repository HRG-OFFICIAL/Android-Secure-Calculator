package com.example.antidebug

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.SharedPreferences
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

/**
 * TamperDetection - Comprehensive application integrity and anti-tampering checks
 * 
 * This class implements multiple techniques to detect application tampering:
 * - APK signature verification and certificate fingerprint checks
 * - DEX file integrity verification using checksums
 * - Loaded library monitoring and verification
 * - Breakpoint instruction scanning in memory
 * - Memory checksum verification for hooks/patches
 * - Installation source verification
 * - File modification time checks
 */
class TamperDetection(private val context: Context) {
    
    companion object {
        private const val TAG = "TamperDetection"
        private const val PREFS_NAME = "tamper_detection_prefs"
        private const val DEX_CHECKSUM_KEY = "dex_checksum"
        private const val APK_CHECKSUM_KEY = "apk_checksum"
        private const val ENCRYPTION_KEY = "TamperDetectionKey2025!"
        
        // Expected certificate fingerprints will be loaded from CertificateInfo
        private var expectedCertFingerprints: Set<String> = emptySet()
        
        fun initializeFingerprints(fingerprints: Set<String>) {
            expectedCertFingerprints = fingerprints
        }
        
        // Breakpoint instruction patterns for different architectures
        private val BREAKPOINT_PATTERNS = arrayOf(
            byteArrayOf(0xCC.toByte()),                    // x86: INT3
            byteArrayOf(0xD4.toByte(), 0x20.toByte(), 0x00.toByte(), 0x20.toByte()), // ARM: BRK
            byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x20.toByte(), 0xD4.toByte()), // ARM64: BRK
            byteArrayOf(0x7F.toByte(), 0xF0.toByte()),     // MIPS: BREAK
        )
        
        // JNI native methods
        external fun nativeMemoryCheck(): Boolean
        external fun nativeIntegrityCheck(): Boolean
        external fun nativeBreakpointScan(): Boolean
    }
    
    private val packageManager = context.packageManager
    private val packageName = context.packageName
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Main method to check if application has been tampered with
     * Combines multiple integrity checks
     */
    fun isAppTampered(): Boolean {
        return try {
            val checks = listOf(
                ::checkSignatureIntegrity,
                ::checkCertificateFingerprint,
                ::checkDexIntegrity,
                ::checkNativeLibraries,
                ::checkInstallationSource,
                ::checkAppDirectory,
                ::checkBreakpointInstructions,
                ::checkNativeMemory,
                ::checkClassLoaderIntegrity,
                ::checkAPKIntegrity,
                ::checkFileModificationTimes
            )
            
            // Return true if any check detects tampering
            checks.any { check ->
                try {
                    check.invoke()
                } catch (e: Exception) {
                    Log.w(TAG, "Tamper check failed: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in tamper detection", e)
            false
        }
    }
    
    /**
     * Check APK signature integrity
     */
    private fun checkSignatureIntegrity(): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(
                packageName, 
                PackageManager.GET_SIGNATURES
            )
            
            val signatures = packageInfo.signatures
            if (signatures == null || signatures.isEmpty()) {
                Log.d(TAG, "No signatures found - potential tampering")
                return true
            }
            
            // Check if signature is debug/test signature
            for (signature in signatures) {
                if (isDebugSignature(signature)) {
                    Log.d(TAG, "Debug signature detected - potential tampering")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Signature integrity check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check certificate fingerprint against expected values
     */
    private fun checkCertificateFingerprint(): Boolean {
        return try {
            if (expectedCertFingerprints.isEmpty()) {
                Log.w(TAG, "No expected fingerprints configured")
                return false
            }
            
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )
            
            val signatures = packageInfo.signatures
            if (signatures == null || signatures.isEmpty()) {
                return true
            }
            
            for (signature in signatures) {
                val cert = CertificateFactory.getInstance("X509")
                    .generateCertificate(signature.toByteArray().inputStream()) as X509Certificate
                
                val fingerprint = getFingerprint(cert)
                if (fingerprint !in expectedCertFingerprints) {
                    Log.d(TAG, "Unexpected certificate fingerprint: $fingerprint")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Certificate fingerprint check failed: ${e.message}")
            true // Assume tampering if check fails
        }
    }
    
    /**
     * Check DEX file integrity using checksums
     */
    private fun checkDexIntegrity(): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val apkPath = applicationInfo.sourceDir
            
            val originalChecksum = getStoredChecksum() // Should be stored securely
            val currentChecksum = calculateDexChecksum(apkPath)
            
            if (originalChecksum != null && originalChecksum != currentChecksum) {
                Log.d(TAG, "DEX checksum mismatch - original: $originalChecksum, current: $currentChecksum")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "DEX integrity check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check loaded native libraries for tampering
     */
    private fun checkNativeLibraries(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists()) return false
            
            val suspiciousLibraries = arrayOf(
                "frida", "xposed", "substrate", "cydia"
            )
            
            mapsFile.readLines().forEach { line ->
                val lowerLine = line.lowercase()
                
                // Check for suspicious library names
                for (suspicious in suspiciousLibraries) {
                    if (lowerLine.contains(suspicious)) {
                        Log.d(TAG, "Suspicious library detected: $line")
                        return true
                    }
                }
                
                // Check for libraries loaded from unusual paths
                if (lowerLine.contains("/data/local/") || 
                    lowerLine.contains("/sdcard/") ||
                    lowerLine.contains("/tmp/")) {
                    Log.d(TAG, "Library from suspicious path: $line")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Native library check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check installation source
     */
    private fun checkInstallationSource(): Boolean {
        return try {
            val installerPackageName = packageManager.getInstallerPackageName(packageName)
            
            // Known legitimate app stores
            val legitimateInstallers = arrayOf(
                "com.android.vending",          // Google Play Store
                "com.amazon.venezia",           // Amazon Appstore
                "com.huawei.appmarket",         // Huawei AppGallery
                "com.samsung.android.samsungpass" // Samsung Galaxy Store
            )
            
            if (installerPackageName == null) {
                Log.d(TAG, "No installer package name - sideloaded app")
                return true
            }
            
            if (installerPackageName !in legitimateInstallers) {
                Log.d(TAG, "Suspicious installer: $installerPackageName")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Installation source check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check application directory for suspicious files
     */
    private fun checkAppDirectory(): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appDir = File(applicationInfo.dataDir)
            
            val suspiciousFiles = arrayOf(
                "frida-server", "xposed", "substrate", ".patch", ".hook"
            )
            
            if (appDir.exists()) {
                appDir.walkTopDown().forEach { file ->
                    val fileName = file.name.lowercase()
                    
                    for (suspicious in suspiciousFiles) {
                        if (fileName.contains(suspicious)) {
                            Log.d(TAG, "Suspicious file in app directory: ${file.absolutePath}")
                            return true
                        }
                    }
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "App directory check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for breakpoint instructions in memory
     */
    private fun checkBreakpointInstructions(): Boolean {
        return try {
            // This would typically be done at native level for better access
            nativeBreakpointScan()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native breakpoint scan unavailable")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Breakpoint instruction check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Native memory integrity check
     */
    private fun checkNativeMemory(): Boolean {
        return try {
            nativeMemoryCheck()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native memory check unavailable")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Native memory check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check class loader integrity
     */
    private fun checkClassLoaderIntegrity(): Boolean {
        return try {
            val classLoader = context.classLoader
            val classLoaderClass = classLoader.javaClass
            
            // Check if class loader has been modified or hooked
            val expectedClassName = "dalvik.system.PathClassLoader"
            if (classLoaderClass.name != expectedClassName) {
                Log.d(TAG, "Unexpected class loader: ${classLoaderClass.name}")
                return true
            }
            
            // Check for suspicious methods or fields
            val methods = classLoaderClass.declaredMethods
            for (method in methods) {
                val methodName = method.name.lowercase()
                if (methodName.contains("hook") || methodName.contains("patch")) {
                    Log.d(TAG, "Suspicious method in class loader: ${method.name}")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Class loader integrity check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check APK file integrity
     */
    private fun checkAPKIntegrity(): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val apkPath = applicationInfo.sourceDir
            val apkFile = File(apkPath)
            
            if (!apkFile.exists()) {
                Log.d(TAG, "APK file not found: $apkPath")
                return true
            }
            
            // Calculate APK checksum
            val currentChecksum = calculateFileChecksum(apkFile)
            val expectedChecksum = getStoredAPKChecksum() // Should be stored securely
            
            if (expectedChecksum != null && expectedChecksum != currentChecksum) {
                Log.d(TAG, "APK checksum mismatch - expected: $expectedChecksum, current: $currentChecksum")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "APK integrity check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check file modification times for suspicious changes
     */
    private fun checkFileModificationTimes(): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val apkFile = File(applicationInfo.sourceDir)
            
            // Get installation time from package manager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val installTime = packageInfo.firstInstallTime
            val apkModTime = apkFile.lastModified()
            
            // APK modification time should not be significantly after installation
            val timeDiff = apkModTime - installTime
            val suspiciousThreshold = 24 * 60 * 60 * 1000L // 24 hours
            
            if (timeDiff > suspiciousThreshold) {
                Log.d(TAG, "Suspicious APK modification time - install: $installTime, modified: $apkModTime")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "File modification time check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check if signature is a debug signature
     */
    private fun isDebugSignature(signature: Signature): Boolean {
        return try {
            val cert = CertificateFactory.getInstance("X509")
                .generateCertificate(signature.toByteArray().inputStream()) as X509Certificate
            
            // Debug certificates typically have CN=Android Debug
            val subject = cert.subjectDN.name
            subject.contains("Android Debug") || subject.contains("CN=Android Debug")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get certificate fingerprint (SHA-256)
     */
    private fun getFingerprint(cert: X509Certificate): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(cert.encoded)
            hash.joinToString(":") { "%02X".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Calculate DEX file checksum from APK
     */
    private fun calculateDexChecksum(apkPath: String): String? {
        return try {
            val zipInputStream = ZipInputStream(FileInputStream(apkPath))
            var entry: ZipEntry?
            
            while (zipInputStream.nextEntry.also { entry = it } != null) {
                if (entry!!.name == "classes.dex") {
                    val digest = MessageDigest.getInstance("SHA-256")
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                    
                    zipInputStream.close()
                    return digest.digest().joinToString("") { "%02x".format(it) }
                }
            }
            zipInputStream.close()
            null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to calculate DEX checksum: ${e.message}")
            null
        }
    }
    
    /**
     * Calculate file checksum (SHA-256)
     */
    private fun calculateFileChecksum(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val inputStream = FileInputStream(file)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            inputStream.close()
            
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to calculate file checksum: ${e.message}")
            null
        }
    }
    
    /**
     * Get stored checksum from encrypted SharedPreferences
     */
    private fun getStoredChecksum(): String? {
        return try {
            val encryptedChecksum = prefs.getString(DEX_CHECKSUM_KEY, null)
            if (encryptedChecksum != null) {
                decryptString(encryptedChecksum)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get stored checksum: ${e.message}")
            null
        }
    }
    
    /**
     * Get stored APK checksum from encrypted SharedPreferences
     */
    private fun getStoredAPKChecksum(): String? {
        return try {
            val encryptedChecksum = prefs.getString(APK_CHECKSUM_KEY, null)
            if (encryptedChecksum != null) {
                decryptString(encryptedChecksum)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get stored APK checksum: ${e.message}")
            null
        }
    }
    
    /**
     * Store checksum securely using encryption
     */
    fun storeChecksum(checksum: String, isApk: Boolean = false) {
        try {
            val encrypted = encryptString(checksum)
            val key = if (isApk) APK_CHECKSUM_KEY else DEX_CHECKSUM_KEY
            prefs.edit().putString(key, encrypted).apply()
            Log.d(TAG, "Stored ${if (isApk) "APK" else "DEX"} checksum securely")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store checksum: ${e.message}")
        }
    }
    
    /**
     * Encrypt string using AES
     */
    private fun encryptString(plaintext: String): String {
        return try {
            val key = SecretKeySpec(ENCRYPTION_KEY.toByteArray().sliceArray(0..15), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encrypted = cipher.doFinal(plaintext.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}")
            plaintext
        }
    }
    
    /**
     * Decrypt string using AES
     */
    private fun decryptString(encryptedText: String): String {
        return try {
            val key = SecretKeySpec(ENCRYPTION_KEY.toByteArray().sliceArray(0..15), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val encrypted = Base64.decode(encryptedText, Base64.DEFAULT)
            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed: ${e.message}")
            ""
        }
    }
    
    /**
     * Advanced tamper detection using multiple heuristics
     */
    fun performAdvancedTamperCheck(): Boolean {
        return try {
            var tamperScore = 0
            
            // Score-based detection
            if (checkSignatureIntegrity()) tamperScore += 3
            if (checkCertificateFingerprint()) tamperScore += 3
            if (checkDexIntegrity()) tamperScore += 2
            if (checkNativeLibraries()) tamperScore += 3
            if (checkInstallationSource()) tamperScore += 1
            if (checkAppDirectory()) tamperScore += 2
            if (checkBreakpointInstructions()) tamperScore += 2
            if (checkClassLoaderIntegrity()) tamperScore += 2
            
            val isTampered = tamperScore >= 3
            Log.d(TAG, "Advanced tamper check score: $tamperScore, tampered: $isTampered")
            return isTampered
            
        } catch (e: Exception) {
            Log.w(TAG, "Advanced tamper check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Get detailed tamper report
     */
    fun getTamperReport(): TamperReport {
        return try {
            TamperReport(
                signatureIntegrity = !checkSignatureIntegrity(),
                certificateValid = !checkCertificateFingerprint(),
                dexIntegrity = !checkDexIntegrity(),
                nativeLibrariesClean = !checkNativeLibraries(),
                legitimateInstaller = !checkInstallationSource(),
                appDirectoryClean = !checkAppDirectory(),
                memoryIntegrity = !checkNativeMemory(),
                classLoaderIntegrity = !checkClassLoaderIntegrity(),
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate tamper report", e)
            TamperReport()
        }
    }
}

/**
 * Detailed tamper detection report
 */
data class TamperReport(
    val signatureIntegrity: Boolean = false,
    val certificateValid: Boolean = false,
    val dexIntegrity: Boolean = false,
    val nativeLibrariesClean: Boolean = false,
    val legitimateInstaller: Boolean = false,
    val appDirectoryClean: Boolean = false,
    val memoryIntegrity: Boolean = false,
    val classLoaderIntegrity: Boolean = false,
    val timestamp: Long = 0L
) {
    fun isAppIntact(): Boolean {
        return signatureIntegrity && certificateValid && dexIntegrity && 
               nativeLibrariesClean && appDirectoryClean && memoryIntegrity && classLoaderIntegrity
    }
    
    fun getTamperCount(): Int {
        return listOf(
            !signatureIntegrity, !certificateValid, !dexIntegrity, !nativeLibrariesClean,
            !legitimateInstaller, !appDirectoryClean, !memoryIntegrity, !classLoaderIntegrity
        ).count { it }
    }
}
