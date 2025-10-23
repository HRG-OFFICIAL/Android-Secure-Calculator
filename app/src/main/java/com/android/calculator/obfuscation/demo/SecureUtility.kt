package com.android.calculator.obfuscation.demo

/**
 * SecureUtility - A simple utility class for runtime decryption demonstration
 * This class will be compiled separately, encrypted, and loaded at runtime
 */
class SecureUtility {
    
    companion object {
        private const val SECRET_CONSTANT = 42
        private val SECRET_STRING = "SecureUtility2025"
    }
    
    /**
     * Perform a secure calculation
     */
    fun performSecureCalculation(input: Int): Int {
        // Obfuscated calculation logic
        val obfuscatedValue = input xor SECRET_CONSTANT
        val result = obfuscatedValue * 2 + 1
        
        // Additional obfuscation
        val checksum = calculateChecksum(result)
        return result + (checksum % 10)
    }
    
    /**
     * Get a secure string
     */
    fun getSecureString(): String {
        val timestamp = System.currentTimeMillis()
        val obfuscated = SECRET_STRING + "_" + (timestamp % 10000)
        return obfuscated.reversed()
    }
    
    /**
     * Verify security status
     */
    fun verifySecurityStatus(): Boolean {
        // Simulate security checks
        val currentTime = System.currentTimeMillis()
        val isValidTime = currentTime > 0
        val hasValidChecksum = calculateChecksum(SECRET_CONSTANT) > 0
        
        return isValidTime && hasValidChecksum
    }
    
    /**
     * Calculate a simple checksum for obfuscation
     */
    private fun calculateChecksum(value: Int): Int {
        var checksum = 0
        var temp = value
        
        while (temp > 0) {
            checksum += temp % 10
            temp /= 10
        }
        
        return checksum
    }
    
    /**
     * Get class information for verification
     */
    fun getClassInfo(): String {
        return "SecureUtility v1.0 - Runtime Loaded"
    }
}
