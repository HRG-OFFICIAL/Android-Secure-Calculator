package com.android.calculator.util

import java.security.SecureRandom
import kotlin.random.Random

/**
 * Control Flow Obfuscation Utility
 * This class provides methods to obfuscate control flow and make reverse engineering difficult
 */
object ControlFlowObfuscator {
    
    private val secureRandom = SecureRandom()
    private val random = Random(System.nanoTime())
    
    /**
     * Execute a block of code with obfuscated control flow
     */
    fun <T> executeWithObfuscation(block: () -> T): T {
        // Insert opaque predicates and fake branches
        return if (opaqueTrue()) {
            insertNoise()
            val result = block()
            insertMoreNoise()
            result
        } else {
            // Dead branch that should never execute
            throw RuntimeException("Control flow corruption detected")
        }
    }
    
    /**
     * Run a block with additional obfuscation
     */
    fun <T> run(block: ControlFlowObfuscator.() -> T): T {
        insertNoise()
        return this.block()
    }
    
    /**
     * Opaque predicate that always returns true but is hard to determine statically
     */
    fun opaqueTrue(): Boolean {
        val currentTime = System.nanoTime()
        val randomValue = secureRandom.nextLong()
        
        // Complex condition that's always true
        return (currentTime > 0) && 
               ((randomValue xor randomValue) == 0L) &&
               (System.currentTimeMillis() > 0)
    }
    
    /**
     * Opaque predicate that always returns false
     */
    fun opaqueFalse(): Boolean {
        val value1 = secureRandom.nextInt()
        val value2 = secureRandom.nextInt()
        
        // Always false but hard to determine
        return (value1 == value2) && (value1 == Int.MAX_VALUE) && (value2 == Int.MIN_VALUE)
    }
    
    /**
     * Insert anti-analysis noise
     */
    fun insertNoise() {
        if (opaqueTrue()) {
            val noise = ByteArray(random.nextInt(32) + 8)
            secureRandom.nextBytes(noise)
            
            // Meaningless computation
            var checksum = 0L
            for (byte in noise) {
                checksum = (checksum * 31 + byte) and 0xFFFF
            }
            
            // Fake branch that's never taken
            if (opaqueFalse()) {
                throw RuntimeException("Noise computation error")
            }
        }
    }
    
    /**
     * Insert more complex anti-analysis noise
     */
    fun insertMoreNoise() {
        val iterations = random.nextInt(5) + 1
        repeat(iterations) {
            if (opaqueTrue()) {
                val dummy = Array(random.nextInt(10) + 5) { random.nextInt() }
                dummy.sort()
                
                // Fake condition
                if (dummy.size < 0) {
                    System.gc() // Never executed
                }
            }
        }
    }
    
    /**
     * Create fake conditional branches
     */
    fun createFakeBranches(value: Int): Int {
        return when {
            opaqueFalse() -> {
                // Dead branch
                value * -1
            }
            opaqueTrue() -> {
                insertNoise()
                value
            }
            else -> {
                // Another dead branch
                value + Int.MAX_VALUE
            }
        }
    }
    
    /**
     * Obfuscated loop with fake iterations
     */
    fun obfuscatedLoop(iterations: Int, action: (Int) -> Unit) {
        val realIterations = createFakeBranches(iterations)
        
        for (i in 0 until realIterations) {
            if (opaqueTrue()) {
                insertNoise()
                action(i)
            } else {
                // Dead branch
                break
            }
        }
        
        insertMoreNoise()
    }
}
