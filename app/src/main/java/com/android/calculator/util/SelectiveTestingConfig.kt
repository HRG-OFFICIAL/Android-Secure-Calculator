package com.android.calculator.util

/**
 * Selective Testing Configuration for AntiDebug Features
 * 
 * This class allows you to enable/disable individual security checks
 * for testing and demonstration purposes.
 * 
 * Usage:
 * 1. Set TEST_MODE = true for testing
 * 2. Enable only the features you want to test
 * 3. Disable all others to prevent interference
 */
object SelectiveTestingConfig {
    
    // ===== TESTING MODE CONTROL =====
    
    /**
     * Set to true to enable selective testing mode
     * Set to false for production (all checks enabled)
     * 
     * DEFAULT: true (allows selective testing configuration)
     * Set to false to force all features always enabled
     */
    const val TEST_MODE = true
    
    // ===== INDIVIDUAL FEATURE TOGGLES =====
    
    /**
     * Debugger Detection Testing
     * - Set to true to test debugger detection
     * - Set to false to disable debugger detection
     */
    const val ENABLE_DEBUGGER_TESTING = true
    const val DISABLE_DEBUGGER_DETECTION = false
    
    /**
     * Emulator Detection Testing  
     * - Set to true to test emulator detection
     * - Set to false to disable emulator detection
     */
    const val ENABLE_EMULATOR_TESTING = true
    const val DISABLE_EMULATOR_DETECTION = false
    
    /**
     * Root Detection Testing
     * - Set to true to test root detection
     * - Set to false to disable root detection
     */
    const val ENABLE_ROOT_TESTING = true
    const val DISABLE_ROOT_DETECTION = false
    
    /**
     * Tampering Detection Testing
     * - Set to true to test tampering detection
     * - Set to false to disable tampering detection
     */
    const val ENABLE_TAMPERING_TESTING = true
    const val DISABLE_TAMPERING_DETECTION = false
    
    // ===== TESTING SCENARIOS =====
    
    /**
     * Predefined testing scenarios for easy switching
     */
    object TestScenarios {
        
        /**
         * Test ONLY Debugger Detection
         * - Disables all other checks
         * - Perfect for capturing debugger detection screenshots
         */
        fun debuggerOnly(): TestingConfig {
            return TestingConfig(
                enableDebugger = true,
                enableEmulator = false,
                enableRoot = false,
                enableTampering = false,
                scenarioName = "DEBUGGER_ONLY"
            )
        }
        
        /**
         * Test ONLY Emulator Detection
         * - Disables all other checks
         * - Perfect for capturing emulator detection screenshots
         */
        fun emulatorOnly(): TestingConfig {
            return TestingConfig(
                enableDebugger = false,
                enableEmulator = true,
                enableRoot = false,
                enableTampering = false,
                scenarioName = "EMULATOR_ONLY"
            )
        }
        
        /**
         * Test ONLY Root Detection
         * - Disables all other checks
         * - Perfect for capturing root detection screenshots
         */
        fun rootOnly(): TestingConfig {
            return TestingConfig(
                enableDebugger = false,
                enableEmulator = false,
                enableRoot = true,
                enableTampering = false,
                scenarioName = "ROOT_ONLY"
            )
        }
        
        /**
         * Test ONLY Tampering Detection
         * - Disables all other checks
         * - Perfect for capturing tampering detection screenshots
         */
        fun tamperingOnly(): TestingConfig {
            return TestingConfig(
                enableDebugger = false,
                enableEmulator = false,
                enableRoot = false,
                enableTampering = true,
                scenarioName = "TAMPERING_ONLY"
            )
        }
        
        /**
         * Test ALL Features (Production Mode)
         * - Enables all checks
         * - Use for final testing
         */
        fun allFeatures(): TestingConfig {
            return TestingConfig(
                enableDebugger = true,
                enableEmulator = true,
                enableRoot = true,
                enableTampering = true,
                scenarioName = "ALL_FEATURES"
            )
        }
        
        /**
         * Disable ALL Features (Development Mode)
         * - Disables all checks
         * - Use for development without security interference
         */
        fun noFeatures(): TestingConfig {
            return TestingConfig(
                enableDebugger = false,
                enableEmulator = false,
                enableRoot = false,
                enableTampering = false,
                scenarioName = "NO_FEATURES"
            )
        }
    }
    
    /**
     * Current testing configuration
     * Change this to switch between different testing scenarios
     * 
     * DEFAULT: All features enabled (production mode)
     * For individual testing, change to specific scenarios below
     */
    val currentConfig: TestingConfig = TestScenarios.allFeatures() // DEFAULT: All features enabled
    // val currentConfig: TestingConfig = TestScenarios.noFeatures() // DEFAULT: Disable all features for development

    /*
     * ===== MANUAL TESTING SCENARIO SWITCHING =====
     * 
     * To test individual features, change the line above to one of these:
     * 
     * PRODUCTION MODE (All Features):
     * val currentConfig: TestingConfig = TestScenarios.allFeatures()
     * 
     * INDIVIDUAL FEATURE TESTING:
     * val currentConfig: TestingConfig = TestScenarios.debuggerOnly()    // Test debugger only
     * val currentConfig: TestingConfig = TestScenarios.emulatorOnly()    // Test emulator only
     * val currentConfig: TestingConfig = TestScenarios.rootOnly()        // Test root only
     * val currentConfig: TestingConfig = TestScenarios.tamperingOnly()   // Test tampering only
     * 
     * DEVELOPMENT MODE (No Features):
     * val currentConfig: TestingConfig = TestScenarios.noFeatures()      // Disable all features
     * 
     * CUSTOM CONFIGURATION:
     * val currentConfig: TestingConfig = TestingConfig(
     *     enableDebugger = true,    // Enable debugger detection
     *     enableEmulator = false,   // Disable emulator detection
     *     enableRoot = true,        // Enable root detection
     *     enableTampering = false,  // Disable tampering detection
     *     scenarioName = "CUSTOM_DEBUGGER_ROOT"
     * )
     * 
     * After changing, rebuild the APK:
     * .\gradlew assembleaggressiveRelease -x lintVitalAnalyzeAggressiveRelease
     * 
     * ================================================
     */
    
    /**
     * Testing configuration data class
     */
    data class TestingConfig(
        val enableDebugger: Boolean,
        val enableEmulator: Boolean,
        val enableRoot: Boolean,
        val enableTampering: Boolean,
        val scenarioName: String
    ) {
        
        /**
         * Check if any security features are enabled
         */
        fun hasAnyEnabled(): Boolean {
            return enableDebugger || enableEmulator || enableRoot || enableTampering
        }
        
        /**
         * Get list of enabled features for logging
         */
        fun getEnabledFeatures(): List<String> {
            val features = mutableListOf<String>()
            if (enableDebugger) features.add("Debugger")
            if (enableEmulator) features.add("Emulator")
            if (enableRoot) features.add("Root")
            if (enableTampering) features.add("Tampering")
            return features
        }
        
        /**
         * Get scenario description for logging
         */
        fun getDescription(): String {
            return "Testing Scenario: $scenarioName - Features: ${getEnabledFeatures().joinToString(", ")}"
        }
    }
    
    /**
     * Quick scenario switching methods
     */
    object QuickSwitch {
        
        /**
         * Switch to debugger testing only
         */
        fun switchToDebuggerOnly() {
            // This would require modifying the currentConfig, but since it's a val,
            // we'll use a different approach in MainActivity
        }
        
        /**
         * Switch to emulator testing only
         */
        fun switchToEmulatorOnly() {
            // Implementation in MainActivity
        }
        
        /**
         * Switch to root testing only
         */
        fun switchToRootOnly() {
            // Implementation in MainActivity
        }
        
        /**
         * Switch to tampering testing only
         */
        fun switchToTamperingOnly() {
            // Implementation in MainActivity
        }
    }
}

/**
 * Testing Helper Functions
 */
object TestingHelper {
    
    /**
     * Log current testing configuration
     */
    fun logCurrentConfig() {
        val config = SelectiveTestingConfig.currentConfig
        android.util.Log.i("SelectiveTesting", config.getDescription())
    }
    
    /**
     * Check if we're in testing mode
     */
    fun isTestingMode(): Boolean {
        return SelectiveTestingConfig.TEST_MODE
    }
    
    /**
     * Get testing configuration for current scenario
     */
    fun getCurrentConfig(): SelectiveTestingConfig.TestingConfig {
        return SelectiveTestingConfig.currentConfig
    }
}
