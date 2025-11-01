package com.example.raspsdk

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.Socket

/**
 * HookDetection - Comprehensive hooking framework detection
 * 
 * This class implements multiple techniques to detect hooking frameworks:
 * - Frida detection (process scanning, port checking, library detection)
 * - Xposed detection (runtime classes, environment checks)
 * - Substrate detection (library and process checks)
 * - Inline hook detection via function prologue validation
 * - Generic hooking framework detection
 */
class HookDetection(private val context: Context) {
    
    companion object {
        private const val TAG = "HookDetection"
        
        // Frida-related indicators
        private val FRIDA_LIBRARIES = arrayOf(
            "libfrida-gadget.so",
            "libfrida-agent.so", 
            "libfrida-core.so",
            "frida-gadget",
            "frida-agent"
        )
        
        private val FRIDA_PROCESSES = arrayOf(
            "frida-server",
            "frida",
            "re.frida.server",
            "linjector"
        )
        
        private val FRIDA_PORTS = arrayOf(27042, 27043, 27045)
        
        // Xposed-related indicators
        private val XPOSED_CLASSES = arrayOf(
            "de.robv.android.xposed.XposedHelpers",
            "de.robv.android.xposed.XposedBridge",
            "de.robv.android.xposed.XC_MethodHook"
        )
        
        private val XPOSED_PACKAGES = arrayOf(
            "de.robv.android.xposed.installer",
            "org.meowcat.edxposed.manager",
            "top.canyie.dreamland.manager"
        )
        
        // Substrate-related indicators
        private val SUBSTRATE_LIBRARIES = arrayOf(
            "libsubstrate.so",
            "libsubstratehook.so",
            "substrate"
        )
        
        // Generic hooking libraries
        private val HOOK_LIBRARIES = arrayOf(
            "libdobby.so",
            "libmshook.so", 
            "libinlinehook.so",
            "libhook.so",
            "libadore.so"
        )
        
        // JNI native methods
        external fun nativeHookCheck(): Boolean
        external fun nativeFridaCheck(): Boolean
        external fun nativeInlineHookCheck(): Boolean
    }
    
    /**
     * Main method to check if hooks are detected
     * Combines multiple detection techniques
     */
    fun areHooksDetected(): Boolean {
        return try {
            val checks = listOf(
                ::checkFridaFramework,
                ::checkXposedFramework,
                ::checkSubstrateFramework,
                ::checkGenericHooks,
                ::checkNativeHooks,
                ::checkPortsForHooks,
                ::checkProcessesForHooks,
                ::checkLibrariesForHooks,
                ::checkEnvironmentForHooks,
                ::checkInlineHooks
            )
            
            // Return true if any check detects hooks
            checks.any { check ->
                try {
                    check.invoke()
                } catch (e: Exception) {
                    Log.w(TAG, "Hook check failed: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in hook detection", e)
            false
        }
    }
    
    /**
     * Check for Frida framework presence
     */
    private fun checkFridaFramework(): Boolean {
        return try {
            // Check for Frida libraries in memory maps
            if (checkFridaLibraries()) {
                Log.d(TAG, "Frida libraries detected")
                return true
            }
            
            // Check for Frida processes
            if (checkFridaProcesses()) {
                Log.d(TAG, "Frida processes detected")
                return true
            }
            
            // Check for Frida ports
            if (checkFridaPorts()) {
                Log.d(TAG, "Frida ports detected")
                return true
            }
            
            // Native Frida check
            try {
                if (nativeFridaCheck()) {
                    Log.d(TAG, "Native Frida detection triggered")
                    return true
                }
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native Frida check unavailable")
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Frida framework check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for Xposed framework presence
     */
    private fun checkXposedFramework(): Boolean {
        return try {
            // Check for Xposed classes in runtime
            for (className in XPOSED_CLASSES) {
                try {
                    Class.forName(className)
                    Log.d(TAG, "Xposed class detected: $className")
                    return true
                } catch (e: ClassNotFoundException) {
                    // Class not found, continue
                }
            }
            
            // Check for Xposed packages
            val packageManager = context.packageManager
            for (packageName in XPOSED_PACKAGES) {
                try {
                    packageManager.getPackageInfo(packageName, 0)
                    Log.d(TAG, "Xposed package detected: $packageName")
                    return true
                } catch (e: Exception) {
                    // Package not found, continue
                }
            }
            
            // Check for Xposed environment variables
            val xposedEnv = System.getProperty("xposed.bridge.version")
            if (xposedEnv != null) {
                Log.d(TAG, "Xposed environment detected: $xposedEnv")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Xposed framework check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for Substrate framework presence
     */
    private fun checkSubstrateFramework(): Boolean {
        return try {
            // Check for Substrate libraries
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                mapsFile.readLines().forEach { line ->
                    val lowerLine = line.lowercase()
                    
                    for (library in SUBSTRATE_LIBRARIES) {
                        if (lowerLine.contains(library)) {
                            Log.d(TAG, "Substrate library detected: $line")
                            return true
                        }
                    }
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Substrate framework check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for generic hooking indicators
     */
    private fun checkGenericHooks(): Boolean {
        return try {
            // Check loaded libraries for hook-related names
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                mapsFile.readLines().forEach { line ->
                    val lowerLine = line.lowercase()
                    
                    for (library in HOOK_LIBRARIES) {
                        if (lowerLine.contains(library)) {
                            Log.d(TAG, "Hook library detected: $line")
                            return true
                        }
                    }
                    
                    // Check for suspicious library paths
                    if (lowerLine.contains("hook") || 
                        lowerLine.contains("inject") ||
                        lowerLine.contains("patch")) {
                        Log.d(TAG, "Suspicious library name: $line")
                        return true
                    }
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Generic hook check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Native hook detection
     */
    private fun checkNativeHooks(): Boolean {
        return try {
            nativeHookCheck()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native hook check unavailable")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Native hook check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for Frida libraries in memory maps
     */
    private fun checkFridaLibraries(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists()) return false
            
            mapsFile.readLines().forEach { line ->
                val lowerLine = line.lowercase()
                
                for (library in FRIDA_LIBRARIES) {
                    if (lowerLine.contains(library)) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check for Frida processes
     */
    private fun checkFridaProcesses(): Boolean {
        return try {
            // Check running processes
            val processOutput = executeCommand("ps")
            val lowerOutput = processOutput.lowercase()
            
            for (process in FRIDA_PROCESSES) {
                if (lowerOutput.contains(process)) {
                    return true
                }
            }
            
            // Alternative check via /proc
            val procDir = File("/proc")
            if (procDir.exists()) {
                procDir.listFiles()?.forEach { pidDir ->
                    if (pidDir.isDirectory && pidDir.name.matches(Regex("\\d+"))) {
                        try {
                            val cmdlineFile = File(pidDir, "cmdline")
                            if (cmdlineFile.exists()) {
                                val cmdline = cmdlineFile.readText().lowercase()
                                for (process in FRIDA_PROCESSES) {
                                    if (cmdline.contains(process)) {
                                        return true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Continue checking other processes
                        }
                    }
                }
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check for Frida ports
     */
    private fun checkFridaPorts(): Boolean {
        return try {
            for (port in FRIDA_PORTS) {
                try {
                    val socket = Socket("127.0.0.1", port)
                    socket.close()
                    return true // Port is open
                } catch (e: Exception) {
                    // Port not open, continue
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check ports for various hooking frameworks
     */
    private fun checkPortsForHooks(): Boolean {
        return try {
            // Common ports used by hooking tools
            val suspiciousPorts = arrayOf(
                27042, 27043, 27045, // Frida
                23946, 23947,        // Xposed/EdXposed
                12345, 54321,        // Generic
                8080, 8443,          // HTTP debugging
                5555, 5554           // ADB over network
            )
            
            for (port in suspiciousPorts) {
                try {
                    val socket = Socket("127.0.0.1", port)
                    socket.close()
                    Log.d(TAG, "Suspicious port open: $port")
                    return true
                } catch (e: Exception) {
                    // Port not open, continue
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Port check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check processes for hooking frameworks
     */
    private fun checkProcessesForHooks(): Boolean {
        return try {
            val suspiciousProcesses = arrayOf(
                "frida", "xposed", "substrate", "magisk", "supersu",
                "adbd", "debuggerd", "gdbserver", "strace"
            )
            
            val processOutput = executeCommand("ps")
            val lowerOutput = processOutput.lowercase()
            
            for (process in suspiciousProcesses) {
                if (lowerOutput.contains(process)) {
                    Log.d(TAG, "Suspicious process detected: $process")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Process check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check loaded libraries for hooking frameworks
     */
    private fun checkLibrariesForHooks(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists()) return false
            
            mapsFile.readLines().forEach { line ->
                val lowerLine = line.lowercase()
                
                // Check for various hooking libraries
                val allLibraries = FRIDA_LIBRARIES + SUBSTRATE_LIBRARIES + HOOK_LIBRARIES
                
                for (library in allLibraries) {
                    if (lowerLine.contains(library)) {
                        Log.d(TAG, "Hook-related library detected: $line")
                        return true
                    }
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Library check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check environment for hooking indicators
     */
    private fun checkEnvironmentForHooks(): Boolean {
        return try {
            val suspiciousEnvVars = arrayOf(
                "FRIDA_VERSION", "XPOSED_VERSION", "SUBSTRATE_VERSION",
                "CYDIA_VERSION", "MAGISK_VERSION"
            )
            
            for (envVar in suspiciousEnvVars) {
                if (System.getenv(envVar) != null) {
                    Log.d(TAG, "Suspicious environment variable: $envVar")
                    return true
                }
            }
            
            // Check system properties
            val suspiciousProps = arrayOf(
                "ro.xposed.version", "ro.substrate.version", 
                "persist.sys.frida", "persist.magisk.version"
            )
            
            for (prop in suspiciousProps) {
                val value = getSystemProperty(prop)
                if (value != null && value.isNotEmpty()) {
                    Log.d(TAG, "Suspicious system property: $prop = $value")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Environment check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for inline hooks
     */
    private fun checkInlineHooks(): Boolean {
        return try {
            nativeInlineHookCheck()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native inline hook check unavailable")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Inline hook check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Execute shell command and return output
     */
    private fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(process.inputStream.reader())
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
     * Advanced hook detection using multiple heuristics
     */
    fun performAdvancedHookCheck(): HookReport {
        return try {
            val fridaDetected = checkFridaFramework()
            val xposedDetected = checkXposedFramework()
            val substrateDetected = checkSubstrateFramework()
            val genericHooksDetected = checkGenericHooks()
            val inlineHooksDetected = checkInlineHooks()
            val suspiciousPortsDetected = checkPortsForHooks()
            val suspiciousProcessesDetected = checkProcessesForHooks()
            val suspiciousLibrariesDetected = checkLibrariesForHooks()
            
            HookReport(
                fridaDetected = fridaDetected,
                xposedDetected = xposedDetected,
                substrateDetected = substrateDetected,
                genericHooksDetected = genericHooksDetected,
                inlineHooksDetected = inlineHooksDetected,
                suspiciousPortsDetected = suspiciousPortsDetected,
                suspiciousProcessesDetected = suspiciousProcessesDetected,
                suspiciousLibrariesDetected = suspiciousLibrariesDetected,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Advanced hook check failed", e)
            HookReport()
        }
    }
}

/**
 * Detailed hook detection report
 */
data class HookReport(
    val fridaDetected: Boolean = false,
    val xposedDetected: Boolean = false,
    val substrateDetected: Boolean = false,
    val genericHooksDetected: Boolean = false,
    val inlineHooksDetected: Boolean = false,
    val suspiciousPortsDetected: Boolean = false,
    val suspiciousProcessesDetected: Boolean = false,
    val suspiciousLibrariesDetected: Boolean = false,
    val timestamp: Long = 0L
) {
    fun hasHooks(): Boolean {
        return fridaDetected || xposedDetected || substrateDetected || 
               genericHooksDetected || inlineHooksDetected || suspiciousPortsDetected ||
               suspiciousProcessesDetected || suspiciousLibrariesDetected
    }
    
    fun getHookCount(): Int {
        return listOf(
            fridaDetected, xposedDetected, substrateDetected, genericHooksDetected,
            inlineHooksDetected, suspiciousPortsDetected, suspiciousProcessesDetected,
            suspiciousLibrariesDetected
        ).count { it }
    }
    
    fun getDetectedFrameworks(): List<String> {
        val frameworks = mutableListOf<String>()
        if (fridaDetected) frameworks.add("Frida")
        if (xposedDetected) frameworks.add("Xposed")
        if (substrateDetected) frameworks.add("Substrate")
        if (genericHooksDetected) frameworks.add("Generic Hooks")
        if (inlineHooksDetected) frameworks.add("Inline Hooks")
        return frameworks
    }
}

