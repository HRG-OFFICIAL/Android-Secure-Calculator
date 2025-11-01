package com.example.raspsdk

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

/**
 * BehavioralDetection - Runtime anomaly and behavioral analysis
 * 
 * This class implements behavioral detection techniques:
 * - Timing analysis and performance monitoring
 * - Process enumeration and analysis
 * - Port scanning for suspicious services
 * - Single-step detection via execution timing
 * - System call frequency analysis
 * - CPU and memory usage patterns
 */
class BehavioralDetection(private val context: Context) {
    
    companion object {
        private const val TAG = "BehavioralDetection"
        
        // Timing thresholds (in nanoseconds)
        private const val NORMAL_EXECUTION_THRESHOLD = 1000000L // 1ms
        private const val SUSPICIOUS_VARIANCE_THRESHOLD = 5000000L // 5ms
        
        // Process monitoring intervals
        private const val MONITORING_INTERVAL = 5000L // 5 seconds
        
        // Suspicious ports to check
        private val SUSPICIOUS_PORTS = arrayOf(
            // Debug ports
            5005, 8000, 8080, 8443,
            // Frida ports
            27042, 27043, 27045,
            // Common hacking tool ports
            12345, 23946, 31337, 4444
        )
    }
    
    private val timingSamples = mutableListOf<Long>()
    private val processCheckCount = AtomicLong(0)
    private var lastProcessCheck = 0L
    
    /**
     * Main method to check for suspicious behavioral patterns
     */
    fun isSuspiciousBehavior(): Boolean {
        return try {
            val checks = listOf(
                ::performTimingAnalysis,
                ::checkProcessAnomalies,
                ::scanSuspiciousPorts,
                ::detectSingleStepping,
                ::analyzeSystemCalls,
                ::checkMemoryPatterns,
                ::monitorCpuUsage
            )
            
            // Return true if any check detects suspicious behavior
            checks.any { check ->
                try {
                    check.invoke()
                } catch (e: Exception) {
                    Log.w(TAG, "Behavioral check failed: ${e.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in behavioral detection", e)
            false
        }
    }
    
    /**
     * Perform timing analysis to detect debugging/instrumentation
     */
    private fun performTimingAnalysis(): Boolean {
        return try {
            val samples = 20
            val timings = mutableListOf<Long>()
            
            repeat(samples) {
                val startTime = System.nanoTime()
                
                // Perform a consistent computation
                var result = 1
                for (i in 1..1000) {
                    result = (result * 31 + i) % 1000007
                }
                
                val endTime = System.nanoTime()
                val duration = endTime - startTime
                timings.add(duration)
                
                // Random delay to prevent pattern detection
                Thread.sleep(Random.nextLong(1, 10))
            }
            
            // Statistical analysis
            val average = timings.average()
            val variance = timings.map { (it - average) * (it - average) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            
            // Check for suspicious timing patterns
            val isSlow = average > NORMAL_EXECUTION_THRESHOLD
            val isHighVariance = stdDev > SUSPICIOUS_VARIANCE_THRESHOLD
            val hasOutliers = timings.any { kotlin.math.abs(it - average) > 3 * stdDev }
            
            if (isSlow || isHighVariance || hasOutliers) {
                Log.d(TAG, "Suspicious timing detected - avg: $average, stdDev: $stdDev, outliers: $hasOutliers")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Timing analysis failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check for process anomalies
     */
    private fun checkProcessAnomalies(): Boolean {
        return try {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastProcessCheck < MONITORING_INTERVAL) {
                return false // Don't check too frequently
            }
            lastProcessCheck = currentTime
            
            val suspiciousProcesses = arrayOf(
                "gdb", "lldb", "strace", "ltrace", "frida", "xposed",
                "debuggerd", "tombstoned", "logcat", "su", "busybox"
            )
            
            // Get process list
            val processOutput = executeCommand("ps -A") // Android 8.0+
            val fallbackOutput = if (processOutput.isEmpty()) executeCommand("ps") else ""
            val allOutput = (processOutput + fallbackOutput).lowercase()
            
            var suspiciousCount = 0
            for (process in suspiciousProcesses) {
                if (allOutput.contains(process)) {
                    suspiciousCount++
                    Log.d(TAG, "Suspicious process detected: $process")
                }
            }
            
            // Check process count frequency
            processCheckCount.incrementAndGet()
            
            // If too many suspicious processes or frequent checks, flag as suspicious
            return suspiciousCount >= 2 || processCheckCount.get() > 100
            
        } catch (e: Exception) {
            Log.w(TAG, "Process anomaly check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Scan for suspicious open ports
     */
    private fun scanSuspiciousPorts(): Boolean {
        return try {
            var openPortCount = 0
            
            for (port in SUSPICIOUS_PORTS) {
                try {
                    val socket = Socket("127.0.0.1", port)
                    socket.soTimeout = 100 // 100ms timeout
                    socket.close()
                    openPortCount++
                    Log.d(TAG, "Suspicious port open: $port")
                } catch (e: Exception) {
                    // Port not open or connection failed
                }
            }
            
            // If multiple suspicious ports are open, flag as suspicious
            return openPortCount >= 2
            
        } catch (e: Exception) {
            Log.w(TAG, "Port scanning failed: ${e.message}")
            false
        }
    }
    
    /**
     * Detect single-stepping by analyzing execution patterns
     */
    private fun detectSingleStepping(): Boolean {
        return try {
            val measurements = mutableListOf<Long>()
            
            // Perform rapid consecutive operations
            repeat(10) {
                val start = System.nanoTime()
                
                // Simple operations that should execute quickly
                val dummy = (1..100).sum()
                val result = dummy * 2 + dummy / 2
                
                val end = System.nanoTime()
                measurements.add(end - start)
            }
            
            // Check for consistent slow execution (indicating single-stepping)
            val average = measurements.average()
            val consistentlySlow = measurements.all { it > NORMAL_EXECUTION_THRESHOLD }
            val lowVariance = measurements.map { kotlin.math.abs(it - average) }.average() < average * 0.1
            
            if (consistentlySlow && lowVariance && average > NORMAL_EXECUTION_THRESHOLD * 10) {
                Log.d(TAG, "Single-stepping detected - consistent slow execution")
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Single-step detection failed: ${e.message}")
            false
        }
    }
    
    /**
     * Analyze system calls for unusual patterns
     */
    private fun analyzeSystemCalls(): Boolean {
        return try {
            // Check /proc/self/syscall for current system call
            val syscallFile = File("/proc/self/syscall")
            if (!syscallFile.exists()) return false
            
            val syscallInfo = syscallFile.readText().trim()
            
            // Check for suspicious system calls (ptrace, etc.)
            val suspiciousSyscalls = arrayOf(
                "ptrace", "process_vm_readv", "process_vm_writev"
            )
            
            for (syscall in suspiciousSyscalls) {
                if (syscallInfo.contains(syscall)) {
                    Log.d(TAG, "Suspicious system call detected: $syscallInfo")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "System call analysis failed: ${e.message}")
            false
        }
    }
    
    /**
     * Check memory access patterns
     */
    private fun checkMemoryPatterns(): Boolean {
        return try {
            val statusFile = File("/proc/self/status")
            if (!statusFile.exists()) return false
            
            val statusContent = statusFile.readText()
            
            // Parse memory information
            val vmSizeMatch = Regex("VmSize:\\s*(\\d+)\\s*kB").find(statusContent)
            val vmRssMatch = Regex("VmRSS:\\s*(\\d+)\\s*kB").find(statusContent)
            
            if (vmSizeMatch != null && vmRssMatch != null) {
                val vmSize = vmSizeMatch.groupValues[1].toLong()
                val vmRss = vmRssMatch.groupValues[1].toLong()
                
                // Check for unusual memory patterns
                val memoryRatio = vmRss.toDouble() / vmSize
                val isSuspiciousMemory = vmSize > 500000 || memoryRatio < 0.1 // Large virtual memory or low RSS ratio
                
                if (isSuspiciousMemory) {
                    Log.d(TAG, "Suspicious memory pattern - VmSize: ${vmSize}kB, VmRSS: ${vmRss}kB")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "Memory pattern check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Monitor CPU usage patterns
     */
    private fun monitorCpuUsage(): Boolean {
        return try {
            val statFile = File("/proc/self/stat")
            if (!statFile.exists()) return false
            
            val statContent = statFile.readText().split(" ")
            
            if (statContent.size > 15) {
                val utime = statContent[13].toLongOrNull() ?: 0
                val stime = statContent[14].toLongOrNull() ?: 0
                val totalTime = utime + stime
                
                // Check for unusually high CPU usage (indicating debugging/analysis)
                if (totalTime > 1000) { // Arbitrary threshold
                    Log.d(TAG, "High CPU usage detected - utime: $utime, stime: $stime")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            Log.w(TAG, "CPU usage monitoring failed: ${e.message}")
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
     * Advanced behavioral analysis with machine learning-like scoring
     */
    fun performAdvancedBehavioralAnalysis(): BehaviorReport {
        return try {
            var suspicionScore = 0
            
            // Weight different behavioral indicators
            if (performTimingAnalysis()) suspicionScore += 3
            if (checkProcessAnomalies()) suspicionScore += 2
            if (scanSuspiciousPorts()) suspicionScore += 2
            if (detectSingleStepping()) suspicionScore += 4
            if (analyzeSystemCalls()) suspicionScore += 3
            if (checkMemoryPatterns()) suspicionScore += 1
            if (monitorCpuUsage()) suspicionScore += 1
            
            val isSuspicious = suspicionScore >= 4
            
            BehaviorReport(
                timingAnomalies = performTimingAnalysis(),
                processAnomalies = checkProcessAnomalies(),
                suspiciousPorts = scanSuspiciousPorts(),
                singleStepping = detectSingleStepping(),
                syscallAnomalies = analyzeSystemCalls(),
                memoryAnomalies = checkMemoryPatterns(),
                cpuAnomalies = monitorCpuUsage(),
                suspicionScore = suspicionScore,
                isSuspicious = isSuspicious,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Advanced behavioral analysis failed", e)
            BehaviorReport()
        }
    }
    
    /**
     * Reset behavioral monitoring state
     */
    fun resetBehavioralState() {
        timingSamples.clear()
        processCheckCount.set(0)
        lastProcessCheck = 0L
    }
}

/**
 * Behavioral analysis report
 */
data class BehaviorReport(
    val timingAnomalies: Boolean = false,
    val processAnomalies: Boolean = false,
    val suspiciousPorts: Boolean = false,
    val singleStepping: Boolean = false,
    val syscallAnomalies: Boolean = false,
    val memoryAnomalies: Boolean = false,
    val cpuAnomalies: Boolean = false,
    val suspicionScore: Int = 0,
    val isSuspicious: Boolean = false,
    val timestamp: Long = 0L
) {
    fun getAnomalyCount(): Int {
        return listOf(
            timingAnomalies, processAnomalies, suspiciousPorts, singleStepping,
            syscallAnomalies, memoryAnomalies, cpuAnomalies
        ).count { it }
    }
    
    fun getRiskLevel(): String {
        return when (suspicionScore) {
            0 -> "None"
            in 1..2 -> "Low"
            in 3..5 -> "Medium"
            in 6..8 -> "High"
            else -> "Critical"
        }
    }
}

