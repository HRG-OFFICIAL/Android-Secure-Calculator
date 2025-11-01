package com.example.raspsdk

import android.content.Context
import kotlinx.coroutines.*

/**
 * RASPSDK - Main entry point for the security SDK
 * 
 * This class provides a unified interface to access all security detection features
 * including debugger detection, root detection, emulator detection, tamper detection,
 * hook detection, behavioral analysis, and data protection.
 * 
 * Usage:
 * ```kotlin
 * RASP.init(context)
 * if (RASP.isDebuggerAttached()) {
 *     // Handle threat
 * }
 * ```
 */
object RASP {
    
    private var initialized = false
    private lateinit var context: Context
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Detection modules
    private lateinit var debuggerDetection: DebuggerDetection
    private lateinit var rootDetection: RootDetection
    private lateinit var emulatorDetection: EmulatorDetection
    private lateinit var tamperDetection: TamperDetection
    private lateinit var hookDetection: HookDetection
    private lateinit var behavioralDetection: BehavioralDetection
    private lateinit var responseHandler: ResponseHandler
    private lateinit var dataProtection: DataProtection
    
    // Native library loading
    init {
        try {
            System.loadLibrary("rasp-native")
        } catch (e: UnsatisfiedLinkError) {
            // Log error but continue - some features will be limited
            android.util.Log.e("RASP", "Failed to load native library: ${e.message}")
        }
    }
    
    /**
     * Initialize the RASP SDK with application context
     * 
     * @param context Application context
     * @param enableContinuousMonitoring Enable background monitoring (optional)
     */
    @JvmStatic
    @JvmOverloads
    fun init(context: Context, enableContinuousMonitoring: Boolean = false) {
        if (initialized) return
        
        this.context = context.applicationContext
        
        // Initialize detection modules
        debuggerDetection = DebuggerDetection(this.context)
        rootDetection = RootDetection(this.context)
        emulatorDetection = EmulatorDetection(this.context)
        tamperDetection = TamperDetection(this.context)
        hookDetection = HookDetection(this.context)
        behavioralDetection = BehavioralDetection(this.context)
        responseHandler = ResponseHandler(this.context)
        dataProtection = DataProtection(this.context)
        
        // Initialize certificate fingerprints for tamper detection
        // Note: Certificate fingerprints will be loaded at runtime from the main app
        val debugFingerprints = setOf(
            "SHA256: 14:6D:E9:83:C5:73:17:34:02:85:12:8F:32:37:4E:85:D3:ED:F3:AA:8C:0A:BC:10:24:02:1C:60:5D:BE:AB:A6"
        )
        val releaseFingerprints = setOf(
            "TODO: Add production certificate fingerprint here"
        )
        val allFingerprints = debugFingerprints + releaseFingerprints
        TamperDetection.initializeFingerprints(allFingerprints)
        
        initialized = true
        
        // Start continuous monitoring if enabled
        if (enableContinuousMonitoring) {
            startContinuousMonitoring()
        }
    }
    
    /**
     * Check if a debugger is currently attached to the process
     * 
     * @return true if debugger is detected
     */
    @JvmStatic
    fun isDebuggerAttached(): Boolean {
        ensureInitialized()
        return debuggerDetection.isDebuggerAttached()
    }
    
    /**
     * Check if the device is rooted
     * 
     * @return true if root is detected
     */
    @JvmStatic
    fun isDeviceRooted(): Boolean {
        ensureInitialized()
        return rootDetection.isDeviceRooted()
    }
    
    /**
     * Check if running on an emulator
     * 
     * @return true if emulator is detected
     */
    @JvmStatic
    fun isRunningOnEmulator(): Boolean {
        ensureInitialized()
        return emulatorDetection.isEmulator()
    }
    
    /**
     * Check if the application has been tampered with
     * 
     * @return true if tampering is detected
     */
    @JvmStatic
    fun isApplicationTampered(): Boolean {
        ensureInitialized()
        return tamperDetection.isAppTampered()
    }
    
    /**
     * Check if hooking frameworks are present
     * 
     * @return true if hooks are detected
     */
    @JvmStatic
    fun areHooksDetected(): Boolean {
        ensureInitialized()
        return hookDetection.areHooksDetected()
    }
    
    /**
     * Perform behavioral analysis for suspicious activity
     * 
     * @return true if suspicious behavior is detected
     */
    @JvmStatic
    fun isSuspiciousBehavior(): Boolean {
        ensureInitialized()
        return behavioralDetection.isSuspiciousBehavior()
    }
    
    /**
     * Perform comprehensive security check
     * 
     * @return SecurityReport containing all detection results
     */
    @JvmStatic
    fun performSecurityCheck(): SecurityReport {
        ensureInitialized()
        
        return SecurityReport(
            debuggerDetected = isDebuggerAttached(),
            rootDetected = isDeviceRooted(),
            emulatorDetected = isRunningOnEmulator(),
            tamperingDetected = isApplicationTampered(),
            hooksDetected = areHooksDetected(),
            suspiciousBehavior = isSuspiciousBehavior(),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Get data protection instance for secure storage
     * 
     * @return DataProtection instance
     */
    @JvmStatic
    fun getDataProtection(): DataProtection {
        ensureInitialized()
        return dataProtection
    }
    
    /**
     * Configure response handler behavior
     * 
     * @param responseType Type of response when threats are detected
     */
    @JvmStatic
    fun configureResponse(responseType: ResponseHandler.ResponseType) {
        ensureInitialized()
        responseHandler.setResponseType(responseType)
    }
    
    /**
     * Manually trigger response for detected threat
     * 
     * @param threatType Type of threat detected
     */
    @JvmStatic
    fun handleThreat(threatType: ThreatType) {
        ensureInitialized()
        responseHandler.handleThreat(threatType)
    }
    
    /**
     * Start continuous monitoring in background
     */
    private fun startContinuousMonitoring() {
        scope.launch {
            while (isActive) {
                try {
                    val report = performSecurityCheck()
                    if (report.hasThreats()) {
                        // Handle detected threats
                        when {
                            report.debuggerDetected -> handleThreat(ThreatType.DEBUGGER)
                            report.rootDetected -> handleThreat(ThreatType.ROOT)
                            report.emulatorDetected -> handleThreat(ThreatType.EMULATOR)
                            report.tamperingDetected -> handleThreat(ThreatType.TAMPERING)
                            report.hooksDetected -> handleThreat(ThreatType.HOOKS)
                            report.suspiciousBehavior -> handleThreat(ThreatType.SUSPICIOUS_BEHAVIOR)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RASP", "Error in continuous monitoring", e)
                }
                
                // Wait before next check (randomized to avoid detection)
                delay((5000..15000).random().toLong())
            }
        }
    }
    
    /**
     * Stop continuous monitoring
     */
    @JvmStatic
    fun stopMonitoring() {
        scope.cancel()
    }
    
    /**
     * Ensure SDK is initialized before use
     */
    private fun ensureInitialized() {
        if (!initialized) {
            throw IllegalStateException("RASP SDK not initialized. Call RASP.init(context) first.")
        }
    }
    
    /**
     * Clean up resources when no longer needed
     */
    @JvmStatic
    fun cleanup() {
        if (initialized) {
            stopMonitoring()
            initialized = false
        }
    }
}

/**
 * Security report containing all detection results
 */
data class SecurityReport(
    val debuggerDetected: Boolean,
    val rootDetected: Boolean,
    val emulatorDetected: Boolean,
    val tamperingDetected: Boolean,
    val hooksDetected: Boolean,
    val suspiciousBehavior: Boolean,
    val timestamp: Long
) {
    fun hasThreats(): Boolean {
        return debuggerDetected || rootDetected || emulatorDetected || 
               tamperingDetected || hooksDetected || suspiciousBehavior
    }
    
    fun getThreatCount(): Int {
        return listOf(
            debuggerDetected, rootDetected, emulatorDetected,
            tamperingDetected, hooksDetected, suspiciousBehavior
        ).count { it }
    }
}

/**
 * Types of security threats that can be detected
 */
enum class ThreatType {
    DEBUGGER,
    ROOT,
    EMULATOR,
    TAMPERING,
    HOOKS,
    SUSPICIOUS_BEHAVIOR
}

