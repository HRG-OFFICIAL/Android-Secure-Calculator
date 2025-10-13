package com.android.calculator.obfuscation.runtime

import java.lang.reflect.Method
import java.security.SecureRandom
import kotlin.random.Random
import com.android.calculator.obfuscation.static.ControlFlowObfuscator

/**
 * Reflection Indirection Utility
 * This class provides methods to invoke methods through reflection to obfuscate call patterns
 */
object ReflectionIndirection {
    
    private val secureRandom = SecureRandom()
    private val random = Random(System.nanoTime())
    
    /**
     * Invoke a method through reflection with obfuscation
     */
    fun invokeMethod(target: Any, methodName: String, vararg args: Any?): Any? {
        return ControlFlowObfuscator.executeWithObfuscation {
            try {
                val clazz = if (target is Class<*>) target else target::class.java
                val method = findMethod(clazz, methodName, args)
                
                insertAntiAnalysisDelay()
                
                if (method != null) {
                    method.isAccessible = true
                    val result = if (target is Class<*>) {
                        method.invoke(null, *args)
                    } else {
                        method.invoke(target, *args)
                    }
                    
                    insertNoise()
                    result
                } else {
                    insertFakeError()
                    null
                }
            } catch (e: Exception) {
                // Obfuscate real errors
                insertNoise()
                null
            }
        }
    }
    
    /**
     * Invoke a static method through reflection
     */
    fun invokeStaticMethod(className: String, methodName: String, vararg args: Any?): Any? {
        return ControlFlowObfuscator.executeWithObfuscation {
            try {
                val clazz = Class.forName(obfuscateClassName(className))
                invokeMethod(clazz, methodName, *args)
            } catch (e: ClassNotFoundException) {
                insertNoise()
                null
            }
        }
    }
    
    /**
     * Get field value through reflection with obfuscation
     */
    fun getField(target: Any, fieldName: String): Any? {
        return ControlFlowObfuscator.executeWithObfuscation {
            try {
                val clazz = if (target is Class<*>) target else target::class.java
                val field = clazz.getDeclaredField(obfuscateFieldName(fieldName))
                
                insertAntiAnalysisDelay()
                field.isAccessible = true
                
                val result = if (target is Class<*>) {
                    field.get(null)
                } else {
                    field.get(target)
                }
                
                insertNoise()
                result
            } catch (e: Exception) {
                insertFakeError()
                null
            }
        }
    }
    
    /**
     * Set field value through reflection with obfuscation
     */
    fun setField(target: Any, fieldName: String, value: Any?): Boolean {
        return ControlFlowObfuscator.executeWithObfuscation {
            try {
                val clazz = if (target is Class<*>) target else target::class.java
                val field = clazz.getDeclaredField(obfuscateFieldName(fieldName))
                
                insertAntiAnalysisDelay()
                field.isAccessible = true
                
                if (target is Class<*>) {
                    field.set(null, value)
                } else {
                    field.set(target, value)
                }
                
                insertNoise()
                true
            } catch (e: Exception) {
                insertFakeError()
                false
            }
        }
    }
    
    /**
     * Create instance through reflection with obfuscation
     */
    fun createInstance(className: String, vararg args: Any?): Any? {
        return ControlFlowObfuscator.executeWithObfuscation {
            try {
                val clazz = Class.forName(obfuscateClassName(className))
                val constructor = findConstructor(clazz, args)
                
                insertAntiAnalysisDelay()
                
                if (constructor != null) {
                    constructor.isAccessible = true
                    val result = constructor.newInstance(*args)
                    insertNoise()
                    result
                } else {
                    insertFakeError()
                    null
                }
            } catch (e: Exception) {
                insertNoise()
                null
            }
        }
    }
    
    /**
     * Find method with parameter matching and obfuscation
     */
    private fun findMethod(clazz: Class<*>, methodName: String, args: Array<out Any?>): Method? {
        val obfuscatedName = obfuscateMethodName(methodName)
        
        return try {
            // Try exact parameter match first
            val paramTypes = args.map { it?.javaClass ?: Any::class.java }.toTypedArray()
            clazz.getDeclaredMethod(obfuscatedName, *paramTypes)
        } catch (e: NoSuchMethodException) {
            try {
                // Try parameter-less method
                clazz.getDeclaredMethod(obfuscatedName)
            } catch (e2: NoSuchMethodException) {
                // Search through all methods
                clazz.declaredMethods.find { method ->
                    method.name == obfuscatedName && 
                    method.parameterCount == args.size
                }
            }
        }
    }
    
    /**
     * Find constructor with parameter matching
     */
    private fun findConstructor(clazz: Class<*>, args: Array<out Any?>): java.lang.reflect.Constructor<*>? {
        return try {
            val paramTypes = args.map { it?.javaClass ?: Any::class.java }.toTypedArray()
            clazz.getDeclaredConstructor(*paramTypes)
        } catch (e: NoSuchMethodException) {
            clazz.declaredConstructors.find { constructor ->
                constructor.parameterCount == args.size
            }
        }
    }
    
    /**
     * Obfuscate class name (in real implementation, this would do actual obfuscation)
     */
    private fun obfuscateClassName(className: String): String {
        insertNoise()
        return className // In real implementation, apply name mapping
    }
    
    /**
     * Obfuscate method name (in real implementation, this would do actual obfuscation)
     */
    private fun obfuscateMethodName(methodName: String): String {
        insertNoise()
        return methodName // In real implementation, apply name mapping
    }
    
    /**
     * Obfuscate field name (in real implementation, this would do actual obfuscation)
     */
    private fun obfuscateFieldName(fieldName: String): String {
        insertNoise()
        return fieldName // In real implementation, apply name mapping
    }
    
    /**
     * Insert anti-analysis delay
     */
    private fun insertAntiAnalysisDelay() {
        if (opaqueCondition()) {
            val delay = random.nextInt(50) + 10
            Thread.sleep(delay.toLong())
        }
    }
    
    /**
     * Insert anti-analysis noise
     */
    private fun insertNoise() {
        if (opaqueCondition()) {
            val noise = ByteArray(random.nextInt(20) + 5)
            secureRandom.nextBytes(noise)
            
            // Meaningless computation
            var hash = 0L
            for (byte in noise) {
                hash = (hash * 17 + byte) and 0x7FFFFFFF
            }
            
            // Fake condition that's never true
            if (hash < 0) {
                throw RuntimeException("Hash computation error")
            }
        }
    }
    
    /**
     * Insert fake error handling
     */
    private fun insertFakeError() {
        if (opaqueCondition()) {
            val fakeErrorCodes = intArrayOf(404, 500, 403, 401)
            val errorCode = fakeErrorCodes[random.nextInt(fakeErrorCodes.size)]
            
            // Fake error processing that does nothing
            if (errorCode == -1) {
                System.err.println("Fake error: $errorCode")
            }
        }
    }
    
    /**
     * Create reflection indirection for a class and method
     */
    fun createIndirection(className: String, methodName: String): Any? {
        return try {
            val clazz = Class.forName(className)
            val method = clazz.getDeclaredMethod(methodName)
            method.isAccessible = true
            method
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Opaque condition for control flow obfuscation
     */
    private fun opaqueCondition(): Boolean {
        val currentTime = System.nanoTime()
        val randomValue = secureRandom.nextLong()
        
        // Always true but hard to determine statically
        return (currentTime > 0) && ((randomValue xor randomValue) == 0L)
    }
}
