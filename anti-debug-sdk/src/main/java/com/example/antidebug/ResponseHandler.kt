package com.example.antidebug

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import kotlin.random.Random
import kotlin.system.exitProcess

/**
 * ResponseHandler - Configurable threat response mechanisms
 * 
 * This class handles responses when security threats are detected:
 * - Process termination (immediate or delayed)
 * - Application crash simulation
 * - Fake screen/misleading behavior
 * - Data corruption/clearing
 * - Silent monitoring and logging
 * - Randomized response triggers
 */
class ResponseHandler(private val context: Context) {
    
    companion object {
        private const val TAG = "ResponseHandler"
        
        // Response delay ranges (milliseconds)
        private val IMMEDIATE_DELAY_RANGE = 0..100
        private val SHORT_DELAY_RANGE = 1000..5000
        private val LONG_DELAY_RANGE = 10000..30000
    }
    
    private var responseType = ResponseType.SILENT_MONITOR
    private var randomizedTrigger = false
    private var responseCallback: ((ThreatType, ResponseType) -> Unit)? = null
    
    /**
     * Types of responses available when threats are detected
     */
    enum class ResponseType {
        // Passive responses
        SILENT_MONITOR,        // Just log and monitor
        LOG_ONLY,             // Log threats without action
        
        // Misleading responses  
        FAKE_SCREEN,          // Show fake/misleading content
        CORRUPT_DATA,         // Corrupt sensitive data display
        RANDOM_BEHAVIOR,      // Random misleading behavior
        
        // Termination responses
        IMMEDIATE_EXIT,       // Exit immediately
        DELAYED_EXIT,         // Exit after random delay
        CRASH_APP,           // Simulate application crash
        KILL_PROCESS,        // Force kill process
        
        // Advanced responses
        RESTART_APP,         // Restart application
        CLEAR_DATA,          // Clear application data
        DISABLE_FEATURES     // Disable critical features
    }
    
    /**
     * Set the response type for threat handling
     */
    fun setResponseType(type: ResponseType) {
        this.responseType = type
        Log.d(TAG, "Response type set to: $type")
    }
    
    /**
     * Enable or disable randomized trigger behavior
     */
    fun setRandomizedTrigger(enabled: Boolean) {
        this.randomizedTrigger = enabled
        Log.d(TAG, "Randomized trigger set to: $enabled")
    }
    
    /**
     * Set callback for custom response handling
     */
    fun setResponseCallback(callback: (ThreatType, ResponseType) -> Unit) {
        this.responseCallback = callback
    }
    
    /**
     * Handle detected threat with configured response
     */
    fun handleThreat(threatType: ThreatType) {
        try {
            Log.w(TAG, "Threat detected: $threatType, response: $responseType")
            
            // Check if we should trigger response (for randomized behavior)
            if (randomizedTrigger && !shouldTriggerResponse()) {
                Log.d(TAG, "Randomized trigger - skipping response this time")
                return
            }
            
            // Call custom callback if set
            responseCallback?.invoke(threatType, responseType)
            
            // Execute configured response
            when (responseType) {
                ResponseType.SILENT_MONITOR -> handleSilentMonitor(threatType)
                ResponseType.LOG_ONLY -> handleLogOnly(threatType)
                ResponseType.FAKE_SCREEN -> handleFakeScreen(threatType)
                ResponseType.CORRUPT_DATA -> handleCorruptData(threatType)
                ResponseType.RANDOM_BEHAVIOR -> handleRandomBehavior(threatType)
                ResponseType.IMMEDIATE_EXIT -> handleImmediateExit(threatType)
                ResponseType.DELAYED_EXIT -> handleDelayedExit(threatType)
                ResponseType.CRASH_APP -> handleCrashApp(threatType)
                ResponseType.KILL_PROCESS -> handleKillProcess(threatType)
                ResponseType.RESTART_APP -> handleRestartApp(threatType)
                ResponseType.CLEAR_DATA -> handleClearData(threatType)
                ResponseType.DISABLE_FEATURES -> handleDisableFeatures(threatType)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling threat response", e)
        }
    }
    
    /**
     * Silent monitoring - log threat but continue execution
     */
    private fun handleSilentMonitor(threatType: ThreatType) {
        Log.i(TAG, "SILENT MONITOR: Threat $threatType detected and logged")
        // Could send to analytics/crash reporting here
    }
    
    /**
     * Log only - record threat information
     */
    private fun handleLogOnly(threatType: ThreatType) {
        val timestamp = System.currentTimeMillis()
        Log.w(TAG, "THREAT LOG: $threatType at $timestamp")
        
        // Store in persistent storage for analysis
        try {
            val prefs = context.getSharedPreferences("threat_log", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            val currentCount = prefs.getInt("${threatType.name}_count", 0)
            editor.putInt("${threatType.name}_count", currentCount + 1)
            editor.putLong("${threatType.name}_last", timestamp)
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log threat", e)
        }
    }
    
    /**
     * Show fake screen or misleading content
     */
    private fun handleFakeScreen(threatType: ThreatType) {
        Log.d(TAG, "FAKE SCREEN: Displaying misleading content for $threatType")
        
        // This would typically involve showing a fake activity or modifying UI
        // For now, we'll simulate by setting a flag that the app can check
        try {
            val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("show_fake_content", true).apply()
            
            // Could also start a fake activity here
            // val intent = Intent(context, FakeActivity::class.java)
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // context.startActivity(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show fake screen", e)
        }
    }
    
    /**
     * Corrupt or modify sensitive data display
     */
    private fun handleCorruptData(threatType: ThreatType) {
        Log.d(TAG, "CORRUPT DATA: Modifying data display for $threatType")
        
        try {
            val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            // Set flags to corrupt various data displays
            editor.putBoolean("corrupt_calculations", true)
            editor.putBoolean("corrupt_display", true)
            editor.putString("corruption_type", threatType.name)
            editor.apply()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to corrupt data", e)
        }
    }
    
    /**
     * Random misleading behavior
     */
    private fun handleRandomBehavior(threatType: ThreatType) {
        Log.d(TAG, "RANDOM BEHAVIOR: Triggering random response for $threatType")
        
        val behaviors = listOf(
            { handleFakeScreen(threatType) },
            { handleCorruptData(threatType) },
            { Thread.sleep(Random.nextLong(1000, 5000)) }, // Random delay
            { 
                // Random wrong calculations
                val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
                prefs.edit().putInt("calculation_offset", Random.nextInt(-100, 100)).apply()
            }
        )
        
        // Execute random behavior
        behaviors.random().invoke()
    }
    
    /**
     * Immediate application exit
     */
    private fun handleImmediateExit(threatType: ThreatType) {
        Log.w(TAG, "IMMEDIATE EXIT: Terminating due to $threatType")
        
        Thread {
            Thread.sleep(Random.nextLong(IMMEDIATE_DELAY_RANGE.first.toLong(), IMMEDIATE_DELAY_RANGE.last.toLong()))
            exitProcess(0)
        }.start()
    }
    
    /**
     * Delayed application exit
     */
    private fun handleDelayedExit(threatType: ThreatType) {
        Log.w(TAG, "DELAYED EXIT: Will terminate due to $threatType")
        
        Thread {
            val delay = Random.nextLong(SHORT_DELAY_RANGE.first.toLong(), SHORT_DELAY_RANGE.last.toLong())
            Log.d(TAG, "Delaying exit for ${delay}ms")
            Thread.sleep(delay)
            exitProcess(0)
        }.start()
    }
    
    /**
     * Simulate application crash
     */
    private fun handleCrashApp(threatType: ThreatType) {
        Log.w(TAG, "CRASH APP: Simulating crash due to $threatType")
        
        Thread {
            Thread.sleep(Random.nextLong(SHORT_DELAY_RANGE.first.toLong(), SHORT_DELAY_RANGE.last.toLong()))
            
            // Simulate different types of crashes
            when (Random.nextInt(3)) {
                0 -> throw RuntimeException("Security violation detected: $threatType")
                1 -> throw OutOfMemoryError("Memory allocation failed")
                2 -> {
                    // Force null pointer exception
                    val nullString: String? = null
                    nullString!!.length
                }
            }
        }.start()
    }
    
    /**
     * Force kill the process
     */
    private fun handleKillProcess(threatType: ThreatType) {
        Log.w(TAG, "KILL PROCESS: Force killing due to $threatType")
        
        Thread {
            Thread.sleep(Random.nextLong(IMMEDIATE_DELAY_RANGE.first.toLong(), IMMEDIATE_DELAY_RANGE.last.toLong()))
            Process.killProcess(Process.myPid())
        }.start()
    }
    
    /**
     * Restart application
     */
    private fun handleRestartApp(threatType: ThreatType) {
        Log.w(TAG, "RESTART APP: Restarting due to $threatType")
        
        try {
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                
                // Kill current process after starting new one
                Thread {
                    Thread.sleep(1000)
                    Process.killProcess(Process.myPid())
                }.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart app", e)
            handleKillProcess(threatType) // Fallback to kill
        }
    }
    
    /**
     * Clear application data
     */
    private fun handleClearData(threatType: ThreatType) {
        Log.w(TAG, "CLEAR DATA: Clearing app data due to $threatType")
        
        try {
            // Clear SharedPreferences
            val prefsFiles = arrayOf("app_prefs", "user_data", "settings", "cache")
            for (prefsFile in prefsFiles) {
                try {
                    context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
                        .edit().clear().apply()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear $prefsFile", e)
                }
            }
            
            // Clear cache directories
            context.cacheDir.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear data", e)
        }
    }
    
    /**
     * Disable critical application features
     */
    private fun handleDisableFeatures(threatType: ThreatType) {
        Log.w(TAG, "DISABLE FEATURES: Disabling features due to $threatType")
        
        try {
            val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            // Disable various features based on threat type
            when (threatType) {
                ThreatType.DEBUGGER -> {
                    editor.putBoolean("debug_features_disabled", true)
                    editor.putBoolean("advanced_calculations_disabled", true)
                }
                ThreatType.ROOT -> {
                    editor.putBoolean("root_features_disabled", true)
                    editor.putBoolean("sensitive_operations_disabled", true)
                }
                ThreatType.HOOKS -> {
                    editor.putBoolean("hook_sensitive_features_disabled", true)
                    editor.putBoolean("api_access_disabled", true)
                }
                else -> {
                    editor.putBoolean("general_features_disabled", true)
                }
            }
            
            editor.putString("disabled_reason", threatType.name)
            editor.putLong("disabled_timestamp", System.currentTimeMillis())
            editor.apply()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable features", e)
        }
    }
    
    /**
     * Determine if response should be triggered (for randomized behavior)
     */
    private fun shouldTriggerResponse(): Boolean {
        // Implement sophisticated trigger logic
        val random = Random.nextDouble()
        
        // Base probability of 60%
        var triggerProbability = 0.6
        
        // Increase probability based on recent threat count
        try {
            val prefs = context.getSharedPreferences("threat_log", Context.MODE_PRIVATE)
            val recentThreats = prefs.getAll().values.sumOf { 
                if (it is Int) it else 0 
            }
            triggerProbability += (recentThreats * 0.1).coerceAtMost(0.3)
        } catch (e: Exception) {
            // Use base probability
        }
        
        return random < triggerProbability
    }
    
    /**
     * Get response configuration info
     */
    fun getResponseConfig(): ResponseConfig {
        return ResponseConfig(
            responseType = responseType,
            randomizedTrigger = randomizedTrigger,
            hasCustomCallback = responseCallback != null
        )
    }
    
    /**
     * Reset response handler state
     */
    fun reset() {
        responseType = ResponseType.SILENT_MONITOR
        randomizedTrigger = false
        responseCallback = null
    }
}

/**
 * Response configuration information
 */
data class ResponseConfig(
    val responseType: ResponseHandler.ResponseType,
    val randomizedTrigger: Boolean,
    val hasCustomCallback: Boolean
)
