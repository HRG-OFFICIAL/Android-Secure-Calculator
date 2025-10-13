package com.android.calculator.obfuscation.static

import kotlin.random.Random

/**
 * Control Flow Obfuscation Utility
 * This class provides methods to obfuscate control flow and make static analysis harder
 * 
 * Techniques implemented:
 * - Opaque predicates
 * - Dummy code insertion
 * - Control flow flattening helpers
 * - Dead code generation
 */
object FlowObfuscator {
    
    // Obfuscated constants for opaque predicates
    private const val MAGIC_CONSTANT_1 = 0x1337
    private const val MAGIC_CONSTANT_2 = 0xDEAD
    private const val MAGIC_CONSTANT_3 = 0xBEEF
    
    // Random seed based on system properties
    private val random = Random(System.nanoTime() xor System.currentTimeMillis())
    
    /**
     * Opaque predicate that always returns true
     * Used to hide real control flow paths
     */
    fun alwaysTrue(): Boolean {
        val x = System.currentTimeMillis()
        val y = System.nanoTime()
        
        // This will always be true, but hard for static analysis to determine
        return ((x and 1) == 0L) || ((x and 1) == 1L) || ((y * 2) >= (y + y - 1))
    }
    
    /**
     * Opaque predicate that always returns false
     * Used to hide dummy code branches
     */
    fun alwaysFalse(): Boolean {
        val x = MAGIC_CONSTANT_1
        val y = MAGIC_CONSTANT_2
        
        // This will always be false, but obfuscated
        return (x * y) < (x + y) && (x - y) > (x + y)
    }
    
    /**
     * Complex opaque predicate with variable result
     * Returns true approximately 50% of the time based on current time
     */
    fun probabilisticPredicate(): Boolean {
        val timestamp = System.currentTimeMillis()
        val nanoTime = System.nanoTime()
        
        // Complex calculation that's hard to predict statically
        val result = ((timestamp xor nanoTime) and MAGIC_CONSTANT_3.toLong()) > 
                    (MAGIC_CONSTANT_1.toLong() shl 2)
        
        return result
    }
    
    /**
     * Insert dummy computation that doesn't affect real logic
     * This creates noise in the control flow graph
     */
    fun dummyComputation(): Int {
        var result = MAGIC_CONSTANT_1
        
        // Meaningless computation that looks important
        for (i in 0..7) {
            result = result xor MAGIC_CONSTANT_2
            result = result shl 1
            result = result or MAGIC_CONSTANT_3
            result = result and 0xFFFF
        }
        
        // Always return a predictable value, but analysis can't easily determine this
        return result and 0x1 // Will be 0 or 1
    }
    
    /**
     * Create fake computation branches
     * Adds multiple paths that don't affect real logic
     */
    fun <T> obfuscatedBranch(realLogic: () -> T): T {
        val dummyValue = dummyComputation()
        
        when {
            alwaysFalse() -> {
                // This branch never executes, but adds complexity
                val fakeResult = System.currentTimeMillis().toString()
                println(fakeResult) // Dead code that looks active
            }
            dummyValue > 10 -> {
                // Another fake branch
                val fakeCalc = MAGIC_CONSTANT_1 * MAGIC_CONSTANT_2
                if (fakeCalc < 0) {
                    throw RuntimeException("Fake error") // Never reached
                }
            }
            probabilisticPredicate() -> {
                // Sometimes executed dummy logic
                val noise = random.nextInt(100)
                if (noise < 0) { // Never true
                    return realLogic() // Fake return to confuse analysis
                }
            }
        }
        
        // Real logic execution (always reached)
        return realLogic()
    }
    
    /**
     * Execute code with obfuscated conditional logic
     * Makes it harder to determine when code actually runs
     */
    fun <T> conditionalExecution(
        condition: Boolean, 
        trueAction: () -> T, 
        falseAction: () -> T
    ): T {
        // Add noise to condition evaluation
        val noisyCondition = when {
            alwaysFalse() -> !condition // Never executed
            alwaysTrue() -> condition   // Always executed
            else -> condition // Never reached, but adds complexity
        }
        
        // Insert dummy branches
        if (dummyComputation() == -1) { // Never true
            return trueAction() // Fake path
        }
        
        if (System.currentTimeMillis() < 0) { // Never true
            return falseAction() // Another fake path
        }
        
        // Real conditional logic (obfuscated)
        return if (noisyCondition) {
            if (alwaysFalse()) {
                falseAction() // Never executed
            } else {
                trueAction() // Real path
            }
        } else {
            if (alwaysFalse()) {
                trueAction() // Never executed
            } else {
                falseAction() // Real path
            }
        }
    }
    
    /**
     * Create a dispatcher-style control flow obfuscation
     * Flattens nested control structures into a state machine
     */
    fun <T> flattenedExecution(vararg actions: () -> T): T {
        var state = 0
        var result: T? = null
        
        // Obfuscated state machine
        while (state < actions.size) {
            when (state) {
                0 -> {
                    if (alwaysFalse()) {
                        state = actions.size // Never executed
                    } else {
                        result = actions[0]()
                        state = 1
                    }
                }
                1 -> {
                    if (actions.size > 1) {
                        if (alwaysTrue()) {
                            result = actions[1]()
                            state = 2
                        }
                    } else {
                        break
                    }
                }
                else -> {
                    if (state < actions.size) {
                        result = actions[state]()
                    }
                    state++
                }
            }
            
            // Add dummy state transitions
            if (dummyComputation() == MAGIC_CONSTANT_1) { // Never true
                state = -1 // Fake transition
            }
        }
        
        return result ?: actions.last()()
    }
    
    /**
     * Anti-debugging timing check with obfuscated control flow
     * Measures execution time to detect debugging/instrumentation
     */
    fun timingCheck(normalAction: () -> Unit): Boolean {
        val start = System.nanoTime()
        
        // Execute action with obfuscated flow
        obfuscatedBranch {
            normalAction()
        }
        
        val end = System.nanoTime()
        val duration = end - start
        
        // Obfuscated threshold check
        val suspiciousThreshold = when {
            alwaysFalse() -> Long.MAX_VALUE // Never used
            alwaysTrue() -> 1_000_000L // 1ms threshold
            else -> 500_000L // Never reached
        }
        
        // Complex timing analysis with dummy branches
        return conditionalExecution(
            condition = duration > suspiciousThreshold,
            trueAction = { 
                // Suspicious timing detected
                if (dummyComputation() > 0) { // Always true, but obfuscated
                    true
                } else {
                    false // Never reached
                }
            },
            falseAction = {
                // Normal timing
                false
            }
        )
    }
}
