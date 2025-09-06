package com.example.antidebug

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * RootDetection - Comprehensive root detection for Android devices
 * 
 * This class implements multiple techniques to detect rooted devices:
 * - SU binary detection in common paths
 * - Root management app detection
 * - System property checks
 * - Writable system partition checks
 * - Build tag analysis
 * - Native root detection methods
 */
class RootDetection(private val context: Context) {
    
    companion object {
        private const val TAG = "RootDetection"
        
        // Common paths where SU binaries are found
        private val SU_BINARY_PATHS = arrayOf(
            "/system/bin/su",
            "/system/xbin/su", 
            "/system/sbin/su",
            "/vendor/bin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        // Common root management applications
        private val ROOT_PACKAGES = arrayOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root.global",
            "com.alephzain.framaroot"
        )
        
        // Dangerous root-related properties
        private val DANGEROUS_PROPS = arrayOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0",
            "ro.build.type" to "eng",
            "ro.build.tags" to "test-keys",
            "service.adb.root" to "1"
        )
        
        // JNI native methods
        external fun nativeRootCheck(): Boolean
        external fun nativePropertyCheck(): Boolean
    }
    
    /**
     * Main method to check if device is rooted
     * Combines multiple detection techniques
     */
    fun isDeviceRooted(): Boolean {
        return try {
            val checks = listOf(
                ::checkSuBinary,
                ::checkRootPackages,
                ::checkSystemProperties,
                ::checkWritableSystem,
                ::checkBuildTags,
                ::checkRootMethod1,
                ::checkRootMethod2,
                ::checkRootMethod3,
                ::checkNativeRoot,
                ::checkSuCommand,
                ::checkRootFiles,
                ::checkMountCommands
            )
            
            // Return true if any check detects root
            checks.any { check ->
                try {
                    check.invoke()
                } catch (e: Exception) {
                    Log.w(TAG, "Root check failed: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in root detection", e)
            false
        }
    }
    
    /**
     * Check for SU binary in common locations
     */
    private fun checkSuBinary(): Boolean {
        for (path in SU_BINARY_PATHS) {
            try {
                val file = File(path)
                if (file.exists() && file.canExecute()) {
                    Log.d(TAG, "SU binary found at: $path")
                    return true
                }
            } catch (e: Exception) {
                // Continue checking other paths
            }
        }
        return false
    }
    
    /**
     * Check for installed root management packages
     */
    private fun checkRootPackages(): Boolean {
        val packageManager = context.packageManager
        
        for (packageName in ROOT_PACKAGES) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                Log.d(TAG, "Root package found: $packageName")
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, continue
            } catch (e: Exception) {
                Log.w(TAG, "Error checking package: $packageName", e)
            }
        }
        return false
    }
    
    /**
     * Check system properties for dangerous values
     */
    private fun checkSystemProperties(): Boolean {
        for ((property, dangerousValue) in DANGEROUS_PROPS) {
            try {
                val value = getSystemProperty(property)
                if (value == dangerousValue) {
                    Log.d(TAG, "Dangerous property: $property = $value")
                    return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to check property: $property", e)
            }
        }
        return false
    }
    
    /**
     * Check if system partition is writable (indicating root)
     */
    private fun checkWritableSystem(): Boolean {
        return try {
            // Try to create a file in system directory
            val systemDir = File("/system")
            val testFile = File("/system/test_write")
            
            if (systemDir.canWrite()) {
                Log.d(TAG, "System partition is writable")
                return true
            }
            
            // Alternative check using mount command
            val mountResult = executeCommand("mount")
            if (mountResult.contains("/system") && mountResult.contains("rw")) {
                Log.d(TAG, "System mounted as read-write")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check writable system: ${e.message}")
            false
        }
    }
    
    /**
     * Check build tags for test-keys (unsigned builds)
     */
    private fun checkBuildTags(): Boolean {
        return try {
            val buildTags = Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                Log.d(TAG, "Test-keys found in build tags: $buildTags")
                return true
            }
            false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check build tags: ${e.message}")
            false
        }
    }
    
    /**
     * Root detection method 1: Check for Superuser.apk
     */
    private fun checkRootMethod1(): Boolean {
        return try {
            val superuserApk = File("/system/app/Superuser.apk")
            if (superuserApk.exists()) {
                Log.d(TAG, "Superuser.apk found")
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Root detection method 2: Check for su command execution
     */
    private fun checkRootMethod2(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            
            if (result != null && result.isNotEmpty()) {
                Log.d(TAG, "SU command found via which: $result")
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Root detection method 3: Check for Busybox
     */
    private fun checkRootMethod3(): Boolean {
        return try {
            val busyboxPaths = arrayOf(
                "/system/bin/busybox",
                "/system/xbin/busybox",
                "/data/local/bin/busybox",
                "/data/local/xbin/busybox"
            )
            
            for (path in busyboxPaths) {
                if (File(path).exists()) {
                    Log.d(TAG, "Busybox found at: $path")
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Native root check via JNI
     */
    private fun checkNativeRoot(): Boolean {
        return try {
            nativeRootCheck()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native root check unavailable")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Native root check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check if su command can be executed
     */
    private fun checkSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val exitValue = process.waitFor()
            
            // If su command doesn't crash immediately, might have root
            if (exitValue == 0) {
                Log.d(TAG, "SU command executed successfully")
                return true
            }
            false
        } catch (e: Exception) {
            // Most likely su doesn't exist or permission denied
            false
        }
    }
    
    /**
     * Check for root-related files
     */
    private fun checkRootFiles(): Boolean {
        val rootFiles = arrayOf(
            "/system/app/SuperSU.apk",
            "/system/app/Kinguser.apk", 
            "/system/app/RootAppDelete.apk",
            "/data/data/com.noshufou.android.su",
            "/data/data/eu.chainfire.supersu",
            "/data/data/com.koushikdutta.superuser",
            "/system/xbin/daemonsu",
            "/system/etc/init.d/99SuperSUDaemon",
            "/dev/com.koushikdutta.superuser.daemon/"
        )
        
        for (file in rootFiles) {
            try {
                if (File(file).exists()) {
                    Log.d(TAG, "Root file found: $file")
                    return true
                }
            } catch (e: Exception) {
                // Continue checking
            }
        }
        return false
    }
    
    /**
     * Check mount commands for suspicious mounts
     */
    private fun checkMountCommands(): Boolean {
        return try {
            val mountOutput = executeCommand("mount")
            
            // Check for suspicious mount points
            val suspiciousMounts = arrayOf(
                "magisk", "xposed", "substrate", "supersu"
            )
            
            for (mount in suspiciousMounts) {
                if (mountOutput.lowercase().contains(mount)) {
                    Log.d(TAG, "Suspicious mount found: $mount")
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check mount commands: ${e.message}")
            false
        }
    }
    
    /**
     * Get system property value
     */
    private fun getSystemProperty(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()?.trim()
            reader.close()
            result
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Execute shell command and return output
     */
    private fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            reader.close()
            output.toString()
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Check for specific root indicators in environment
     */
    fun checkRootEnvironment(): Boolean {
        return try {
            // Check environment variables
            val rootEnvVars = arrayOf(
                "PATH", "LD_LIBRARY_PATH", "LD_PRELOAD"
            )
            
            for (envVar in rootEnvVars) {
                val value = System.getenv(envVar)
                if (value != null && (value.contains("su") || value.contains("magisk"))) {
                    Log.d(TAG, "Root indicator in environment: $envVar = $value")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check root environment: ${e.message}")
            false
        }
    }
    
    /**
     * Advanced root detection using multiple heuristics
     */
    fun performAdvancedRootCheck(): Boolean {
        return try {
            var rootScore = 0
            
            // Score-based detection
            if (checkSuBinary()) rootScore += 3
            if (checkRootPackages()) rootScore += 3
            if (checkSystemProperties()) rootScore += 2
            if (checkWritableSystem()) rootScore += 3
            if (checkBuildTags()) rootScore += 1
            if (checkRootFiles()) rootScore += 2
            
            // Check for additional indicators
            try {
                val buildUser = Build.USER
                if (buildUser == "root" || buildUser.contains("test")) {
                    rootScore += 1
                    Log.d(TAG, "Suspicious build user: $buildUser")
                }
            } catch (e: Exception) {
                // Ignore
            }
            
            val isRooted = rootScore >= 3
            Log.d(TAG, "Advanced root check score: $rootScore, rooted: $isRooted")
            return isRooted
            
        } catch (e: Exception) {
            Log.w(TAG, "Advanced root check failed: ${e.message}")
            false
        }
    }
}
