package com.example.raspsdk

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.NetworkInterface
import android.annotation.SuppressLint

/**
 * EmulatorDetection - Comprehensive emulator detection for Android
 * 
 * This class implements multiple techniques to detect Android emulators:
 * - Build property analysis (manufacturer, model, hardware)
 * - QEMU detection (pipes, files, properties)
 * - Network interface analysis
 * - Telephony service checks
 * - Hardware feature detection
 * - CPU and sensor analysis
 */
class EmulatorDetection(private val context: Context) {
    
    companion object {
        private const val TAG = "EmulatorDetection"
        
        // Known emulator build properties
        private val EMULATOR_PROPS = mapOf(
            Build.MANUFACTURER to arrayOf("Genymotion", "unknown", "Android", "google"),
            Build.MODEL to arrayOf("sdk", "google_sdk", "Android SDK built for x86", "Android SDK built for x86_64", "sdk_gphone64_x86_64", "sdk_gphone_x86"),
            Build.HARDWARE to arrayOf("goldfish", "ranchu", "vbox86", "vbox86p"),
            Build.PRODUCT to arrayOf("sdk", "google_sdk", "sdk_x86", "vbox86p", "sdk_gphone64_x86_64", "sdk_gphone_x86"),
            Build.BOARD to arrayOf("goldfish", "ranchu", "vbox86", "vbox86p"),
            Build.BRAND to arrayOf("generic", "google", "Android"),
            Build.DEVICE to arrayOf("generic", "generic_x86", "generic_x86_64", "vbox86p", "emulator")
        )
        
        // QEMU-related files and properties
        private val QEMU_FILES = arrayOf(
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props",
            "/dev/socket/baseband_genyd",
            "/dev/socket/genyd"
        )
        
        private val QEMU_PROPS = arrayOf(
            "init.svc.qemud",
            "init.svc.qemu-props",
            "qemu.hw.mainkeys",
            "qemu.sf.fake_camera",
            "ro.bootloader",
            "ro.bootmode",
            "ro.hardware",
            "ro.kernel.android.qemud",
            "ro.kernel.qemu.gles",
            "ro.kernel.qemu",
            "ro.product.device",
            "ro.product.model",
            "ro.product.name",
            "ro.serialno"
        )
        
        // Genymotion-specific indicators
        private val GENYMOTION_FILES = arrayOf(
            "/dev/socket/baseband_genyd",
            "/dev/socket/genyd"
        )
        
        // Andy emulator indicators
        private val ANDY_FILES = arrayOf(
            "fstab.andy",
            "ueventd.andy.rc"
        )
        
        // Known emulator IP addresses
        private val EMULATOR_IPS = arrayOf(
            "10.0.2.15",    // Default Android emulator
            "10.0.3.2",     // Genymotion
            "192.168.56.101" // VirtualBox
        )
    }
    
    /**
     * Main method to check if running on an emulator
     * Combines multiple detection techniques
     */
    fun isEmulator(): Boolean {
        return try {
            val checks = listOf(
                ::checkBuildProperties,
                ::checkQemuFiles,
                ::checkQemuProperties,
                ::checkNetworkInterfaces,
                ::checkTelephonyFeatures,
                ::checkGenymotionFeatures,
                ::checkAndyEmulator,
                ::checkBlueStacksEmulator,
                ::checkNoxEmulator,
                ::checkCpuInfo,
                ::checkSensorList,
                ::checkOperatorName
            )
            
            // Return true if any check detects an emulator
            checks.any { check ->
                try {
                    check.invoke()
                } catch (e: Exception) {
                    Log.w(TAG, "Emulator check failed: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in emulator detection", e)
            false
        }
    }
    
    /**
     * Check build properties for emulator indicators
     */
    private fun checkBuildProperties(): Boolean {
        for ((property, suspiciousValues) in EMULATOR_PROPS) {
            val currentValue = property.lowercase()
            
            for (suspiciousValue in suspiciousValues) {
                if (currentValue.contains(suspiciousValue.lowercase())) {
                    Log.d(TAG, "Emulator property detected: $property = $currentValue")
                    return true
                }
            }
        }
        
        // Additional checks
        if (Build.FINGERPRINT.startsWith("generic") || 
            Build.FINGERPRINT.lowercase().contains("vbox") ||
            Build.FINGERPRINT.lowercase().contains("test-keys")) {
            Log.d(TAG, "Emulator fingerprint detected: ${Build.FINGERPRINT}")
            return true
        }
        
        return false
    }
    
    /**
     * Check for QEMU-related files
     */
    private fun checkQemuFiles(): Boolean {
        for (filePath in QEMU_FILES) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    Log.d(TAG, "QEMU file found: $filePath")
                    return true
                }
            } catch (e: Exception) {
                // Continue checking other files
            }
        }
        return false
    }
    
    /**
     * Check QEMU-related system properties
     */
    private fun checkQemuProperties(): Boolean {
        for (property in QEMU_PROPS) {
            try {
                val value = getSystemProperty(property)
                if (value != null && value.isNotEmpty() && value != "null") {
                    // Check for QEMU-specific values
                    if (property.contains("qemu") || 
                        value.lowercase().contains("qemu") ||
                        value.lowercase().contains("goldfish") ||
                        value.lowercase().contains("ranchu")) {
                        Log.d(TAG, "QEMU property detected: $property = $value")
                        return true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to check property: $property", e)
            }
        }
        return false
    }
    
    /**
     * Check network interfaces for emulator-specific configurations
     */
    private fun checkNetworkInterfaces(): Boolean {
        return try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val name = networkInterface.name.lowercase()
                
                // Check for emulator-specific network interfaces
                if (name.contains("eth0") || name.contains("wlan0")) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement().hostAddress
                        
                        // Check against known emulator IP ranges
                        for (emulatorIp in EMULATOR_IPS) {
                            if (address == emulatorIp) {
                                Log.d(TAG, "Emulator IP detected: $address")
                                return true
                            }
                        }
                        
                        // Check for 10.0.2.x range (default Android emulator)
                        if (address?.startsWith("10.0.2.") == true) {
                            Log.d(TAG, "Android emulator IP range detected: $address")
                            return true
                        }
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.w(TAG, "Network interface check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check telephony features for emulator indicators
     */
    @SuppressLint("MissingPermission")
    private fun checkTelephonyFeatures(): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            // Check for fake/empty IMEI (common in emulators)
            val deviceId = telephonyManager.deviceId
            if (deviceId == null || 
                deviceId == "000000000000000" || 
                deviceId == "012345678901234" ||
                deviceId.all { it == '0' }) {
                Log.d(TAG, "Fake/empty device ID detected: $deviceId")
                return true
            }
            
            // Check for fake operator names
            val operatorName = telephonyManager.networkOperatorName
            val fakeOperators = arrayOf("Android", "Fake", "Test", "")
            
            if (operatorName in fakeOperators) {
                Log.d(TAG, "Fake operator detected: $operatorName")
                return true
            }
            
            // Check phone type
            val phoneType = telephonyManager.phoneType
            if (phoneType == TelephonyManager.PHONE_TYPE_NONE) {
                Log.d(TAG, "No phone type detected (emulator indicator)")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Telephony check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for Genymotion-specific features
     */
    private fun checkGenymotionFeatures(): Boolean {
        // Check Genymotion files
        for (filePath in GENYMOTION_FILES) {
            try {
                if (File(filePath).exists()) {
                    Log.d(TAG, "Genymotion file found: $filePath")
                    return true
                }
            } catch (e: Exception) {
                // Continue
            }
        }
        
        // Check for Genymotion properties
        val genymotionProps = arrayOf(
            "init.svc.vbox86-setup",
            "ro.product.manufacturer",
            "ro.product.model"
        )
        
        for (prop in genymotionProps) {
            val value = getSystemProperty(prop)
            if (value != null && value.lowercase().contains("genymotion")) {
                Log.d(TAG, "Genymotion property detected: $prop = $value")
                return true
            }
        }
        
        return false
    }
    
    /**
     * Check for Andy emulator
     */
    private fun checkAndyEmulator(): Boolean {
        for (filePath in ANDY_FILES) {
            try {
                if (File("/system/etc/$filePath").exists()) {
                    Log.d(TAG, "Andy emulator file found: $filePath")
                    return true
                }
            } catch (e: Exception) {
                // Continue
            }
        }
        return false
    }
    
    /**
     * Check for BlueStacks emulator
     */
    private fun checkBlueStacksEmulator(): Boolean {
        return try {
            val bluestacksProps = arrayOf(
                "ro.product.model",
                "ro.product.manufacturer",
                "ro.product.device"
            )
            
            for (prop in bluestacksProps) {
                val value = getSystemProperty(prop)
                if (value != null && value.lowercase().contains("bluestacks")) {
                    Log.d(TAG, "BlueStacks property detected: $prop = $value")
                    return true
                }
            }
            
            // Check for BlueStacks-specific files
            val bluestacksFiles = arrayOf(
                "/data/app/com.bluestacks.home",
                "/data/bluestacks.prop",
                "/data/data/com.bluestacks.home"
            )
            
            for (filePath in bluestacksFiles) {
                if (File(filePath).exists()) {
                    Log.d(TAG, "BlueStacks file found: $filePath")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "BlueStacks check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for Nox emulator
     */
    private fun checkNoxEmulator(): Boolean {
        return try {
            val noxProps = arrayOf(
                "ro.product.model",
                "ro.product.manufacturer"
            )
            
            for (prop in noxProps) {
                val value = getSystemProperty(prop)
                if (value != null && value.lowercase().contains("nox")) {
                    Log.d(TAG, "Nox property detected: $prop = $value")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Nox check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check CPU info for emulator indicators
     */
    private fun checkCpuInfo(): Boolean {
        return try {
            val cpuInfoFile = File("/proc/cpuinfo")
            if (!cpuInfoFile.exists()) return false
            
            BufferedReader(FileReader(cpuInfoFile)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val lowerLine = line!!.lowercase()
                    
                    // Check for emulator-specific CPU features
                    if (lowerLine.contains("goldfish") || 
                        lowerLine.contains("qemu") ||
                        lowerLine.contains("virtual") ||
                        lowerLine.contains("vbox")) {
                        Log.d(TAG, "Emulator CPU detected: $line")
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.w(TAG, "CPU info check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check sensor list for suspicious configurations
     */
    private fun checkSensorList(): Boolean {
        return try {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
            val sensorList = sensorManager.getSensorList(android.hardware.Sensor.TYPE_ALL)
            
            // Emulators typically have fewer sensors or fake sensor names
            if (sensorList.size < 5) {
                Log.d(TAG, "Too few sensors detected: ${sensorList.size}")
                return true
            }
            
            // Check for fake sensor vendors
            for (sensor in sensorList) {
                val vendor = sensor.vendor.lowercase()
                if (vendor.contains("goldfish") || 
                    vendor.contains("the android open source project")) {
                    Log.d(TAG, "Fake sensor vendor detected: ${sensor.vendor}")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Sensor check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check operator name for fake values
     */
    private fun checkOperatorName(): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val operatorName = telephonyManager.networkOperatorName
            
            val fakeOperators = arrayOf(
                "android", "test-keys", "fake", ""
            )
            
            for (fakeOp in fakeOperators) {
                if (operatorName.lowercase().contains(fakeOp)) {
                    Log.d(TAG, "Fake operator name detected: $operatorName")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Operator name check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Get system property value
     */
    private fun getSystemProperty(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            val reader = BufferedReader(process.inputStream.reader())
            val result = reader.readLine()?.trim()
            reader.close()
            result
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Advanced emulator detection using scoring system
     */
    fun performAdvancedEmulatorCheck(): Boolean {
        return try {
            var emulatorScore = 0
            
            // Score-based detection
            if (checkBuildProperties()) emulatorScore += 3
            if (checkQemuFiles()) emulatorScore += 3
            if (checkQemuProperties()) emulatorScore += 2
            if (checkNetworkInterfaces()) emulatorScore += 2
            if (checkTelephonyFeatures()) emulatorScore += 2
            if (checkGenymotionFeatures()) emulatorScore += 3
            if (checkCpuInfo()) emulatorScore += 2
            if (checkSensorList()) emulatorScore += 1
            
            val isEmulator = emulatorScore >= 3
            Log.d(TAG, "Advanced emulator check score: $emulatorScore, emulator: $isEmulator")
            return isEmulator
            
        } catch (e: Exception) {
            Log.w(TAG, "Advanced emulator check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for specific emulator based on multiple indicators
     */
    fun getEmulatorType(): String {
        return try {
            when {
                checkGenymotionFeatures() -> "Genymotion"
                checkAndyEmulator() -> "Andy"
                checkBlueStacksEmulator() -> "BlueStacks"
                checkNoxEmulator() -> "Nox"
                checkQemuFiles() || checkQemuProperties() -> "QEMU/Default Android Emulator"
                checkBuildProperties() -> "Generic Emulator"
                else -> "Physical Device"
            }
        } catch (e: Exception) {
            Log.w(TAG, "Emulator type check failed: ${e.message}")
            "Unknown"
        }
    }
}

