package com.example.raspsdk

import android.content.Context
import android.os.Debug
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * DebuggerDetection - Comprehensive debugger and tracer detection
 * 
 * This class implements multiple techniques to detect debuggers, tracers, and debugging tools:
 * - Android Debug API checks
 * - TracerPid monitoring from /proc/self/status
 * - Native ptrace self-attachment
 * - Signal handling for SIGTRAP
 * - Runtime debug flags detection
 * - Timing-based detection
 */
class DebuggerDetection(private val context: Context) {
    
    companion object {
        private const val TAG = "DebuggerDetection"
        
        // JNI native methods
        external fun nativePtraceCheck(): Boolean
        external fun nativeSignalCheck(): Boolean
        external fun nativeTimingCheck(): Boolean
        external fun nativeDebuggerCheck(): Boolean
    }
    
    private var lastCheckTime = 0L
    private val randomDelay = (100..500).random()
    
    /**
     * Main method to check if debugger is attached
     * Combines multiple detection techniques for comprehensive coverage
     */
    fun isDebuggerAttached(): Boolean {
        return try {
            // Use multiple detection methods
            val methods = listOf(
                ::checkAndroidDebugApi,
                ::checkTracerPid,
                ::checkDebuggerConnected,
                ::checkWaitingForDebugger,
                ::checkDebugFlags,
                ::checkTimingAttack,
                ::checkNativePtrace,
                ::checkNativeSignal,
                ::checkNativeTiming,
                ::checkJDWPPort
            )
            
            // Return true if any method detects a debugger
            methods.any { method ->
                try {
                    method.invoke()
                } catch (e: Exception) {
                    Log.w(TAG, "Detection method failed: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in debugger detection", e)
            false
        }
    }
    
    /**
     * Check Android Debug API for connected debugger
     */
    private fun checkAndroidDebugApi(): Boolean {
        return Debug.isDebuggerConnected()
    }
    
    /**
     * Check if debugger is waiting for connection
     */
    private fun checkDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected()
    }
    
    /**
     * Check if process is waiting for debugger to attach
     */
    private fun checkWaitingForDebugger(): Boolean {
        return Debug.waitingForDebugger()
    }
    
    /**
     * Check TracerPid from /proc/self/status
     * If TracerPid is not 0, a tracer/debugger is attached
     */
    private fun checkTracerPid(): Boolean {
        return try {
            val statusFile = File("/proc/self/status")
            if (!statusFile.exists()) return false
            
            BufferedReader(FileReader(statusFile)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.startsWith("TracerPid:")) {
                        val tracerPid = line!!.substring(10).trim().toInt()
                        if (tracerPid != 0) {
                            Log.d(TAG, "TracerPid detected: $tracerPid")
                            return true
                        }
                        break
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check TracerPid: ${e.message}")
            false
        }
    }
    
    /**
     * Check system properties for debug flags
     */
    private fun checkDebugFlags(): Boolean {
        return try {
            val debuggableProperty = getSystemProperty("ro.debuggable")
            val secureProperty = getSystemProperty("ro.secure")
            val adbProperty = getSystemProperty("init.svc.adbd")
            
            // Check if device is in debug mode
            val isDebuggable = debuggableProperty == "1"
            val isInsecure = secureProperty == "0"
            val isAdbRunning = adbProperty == "running"
            
            if (isDebuggable || isInsecure || isAdbRunning) {
                Log.d(TAG, "Debug flags detected - debuggable: $isDebuggable, secure: $secureProperty, adb: $isAdbRunning")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check debug flags: ${e.message}")
            false
        }
    }
    
    /**
     * Timing-based debugger detection
     * Debuggers often slow down execution
     */
    private fun checkTimingAttack(): Boolean {
        return try {
            val iterations = 1000
            val startTime = System.nanoTime()
            
            // Simple computation that should be fast
            var sum = 0
            for (i in 0 until iterations) {
                sum += i * i
            }
            
            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1000000 // Convert to milliseconds
            
            // If execution took too long, might be debugged
            val threshold = 10 // 10ms threshold
            val isSlowed = duration > threshold
            
            if (isSlowed) {
                Log.d(TAG, "Timing attack detected - duration: ${duration}ms")
            }
            
            isSlowed
        } catch (e: Exception) {
            Log.w(TAG, "Timing check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for JDWP (Java Debug Wire Protocol) port
     */
    private fun checkJDWPPort(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("netstat -an")
            val reader = BufferedReader(process.inputStream.reader())
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                // Check for common JDWP ports
                if (line!!.contains(":8000") || line!!.contains(":8600") || 
                    line!!.contains(":5005") || line!!.contains(":8453")) {
                    Log.d(TAG, "JDWP port detected: $line")
                    reader.close()
                    return true
                }
            }
            reader.close()
            false
        } catch (e: Exception) {
            Log.w(TAG, "JDWP port check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Native ptrace check via JNI
     */
    private fun checkNativePtrace(): Boolean {
        return try {
            nativePtraceCheck()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native ptrace check unavailable")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Native ptrace check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Native signal check via JNI
     */
    private fun checkNativeSignal(): Boolean {
        return try {
            nativeSignalCheck()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native signal check unavailable")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Native signal check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Native timing check via JNI
     */
    private fun checkNativeTiming(): Boolean {
        return try {
            nativeTimingCheck()
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native timing check unavailable")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Native timing check failed: ${e.message}")
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
     * Check for debugger using multiple environment indicators
     */
    fun checkDebuggerEnvironment(): Boolean {
        return try {
            // Check for debug-related environment variables
            val debugVars = arrayOf(
                "ANDROID_DEBUG", "DEBUG_MODE", "DALVIK_DEBUG",
                "ADB_DEBUG", "JAVA_DEBUG", "JDB_DEBUG"
            )
            
            for (debugVar in debugVars) {
                if (System.getenv(debugVar) != null) {
                    Log.d(TAG, "Debug environment variable found: $debugVar")
                    return true
                }
            }
            
            // Check for debug-related system properties
            val debugProps = arrayOf(
                "debug.assert", "dalvik.vm.debug.enabled", 
                "persist.sys.debug", "ro.debuggable"
            )
            
            for (debugProp in debugProps) {
                val value = getSystemProperty(debugProp)
                if (value == "1" || value == "true") {
                    Log.d(TAG, "Debug property found: $debugProp = $value")
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
     * Advanced timing-based detection with multiple samples
     */
    fun performAdvancedTimingCheck(): Boolean {
        return try {
            val samples = 10
            val timings = mutableListOf<Long>()
            
            repeat(samples) {
                val start = System.nanoTime()
                
                // Perform some computation
                var result = 0
                for (i in 0..1000) {
                    result = (result + i) * 31
                }
                
                val end = System.nanoTime()
                timings.add(end - start)
                
                // Small delay between samples
                Thread.sleep(randomDelay.toLong())
            }
            
            // Calculate statistics
            val average = timings.average()
            val max = timings.maxOrNull() ?: 0L
            val min = timings.minOrNull() ?: 0L
            val variance = max - min
            
            // If there's high variance or consistently slow execution, suspect debugging
            val isVarianceHigh = variance > average * 2
            val isSlow = average > 1000000 // 1ms threshold
            
            if (isVarianceHigh || isSlow) {
                Log.d(TAG, "Advanced timing check detected anomaly - avg: $average, variance: $variance")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Advanced timing check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for breakpoint instructions in memory
     */
    fun checkBreakpointInstructions(): Boolean {
        return try {
            // This would typically be done at native level
            // Here we check for common breakpoint patterns
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists()) return false
            
            BufferedReader(FileReader(mapsFile)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // Look for executable memory regions
                    if (line!!.contains("r-xp") && line.contains(".so")) {
                        // In a real implementation, we would read memory here
                        // and check for breakpoint instructions (0xCC, 0xD4200020, etc.)
                        Log.d(TAG, "Checking memory region: $line")
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.w(TAG, "Breakpoint instruction check failed: ${e.message}")
            false
        }
    }
}

