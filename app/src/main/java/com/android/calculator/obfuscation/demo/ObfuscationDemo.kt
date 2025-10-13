package com.android.calculator.obfuscation.demo

import java.util.*

/**
 * Obfuscation Demo Class
 * This class demonstrates how code gets obfuscated by ProGuard/R8
 * 
 * Original readable code will be transformed into obfuscated code
 * similar to the example provided by the user
 */
class ObfuscationDemo {
    
    private var calculationResult: Double = 0.0
    private var operationHistory: MutableList<String> = mutableListOf()
    
    /**
     * Original method - will be obfuscated to something like 'a(a b)'
     * This demonstrates the obfuscation transformation
     */
    private fun calculate(calculationList: CalculationList) {
        while (calculationList.hasMore()) {
            val currentItem = calculationList.getNext(true)
            currentItem.calculate()
            processCalculation(currentItem)
        }
    }
    
    /**
     * Another method that will be obfuscated
     */
    private fun processCalculation(item: CalculationItem) {
        when (item.operation) {
            "add" -> {
                calculationResult += item.value
                operationHistory.add("Added ${item.value}")
            }
            "subtract" -> {
                calculationResult -= item.value
                operationHistory.add("Subtracted ${item.value}")
            }
            "multiply" -> {
                calculationResult *= item.value
                operationHistory.add("Multiplied by ${item.value}")
            }
            "divide" -> {
                if (item.value != 0.0) {
                    calculationResult /= item.value
                    operationHistory.add("Divided by ${item.value}")
                }
            }
        }
    }
    
    /**
     * Method that will be heavily obfuscated
     */
    private fun performComplexCalculation(data: CalculationData) {
        val tempResult = data.baseValue
        val multiplier = data.multiplier
        val divisor = data.divisor
        
        val intermediateResult = tempResult * multiplier
        val finalResult = intermediateResult / divisor
        
        calculationResult = finalResult
        operationHistory.add("Complex calculation: $tempResult * $multiplier / $divisor = $finalResult")
    }
    
    /**
     * Method with multiple parameters - will be obfuscated
     */
    private fun validateAndCalculate(
        inputValue: Double,
        operationType: String,
        validationRules: ValidationRules
    ): Double {
        if (validationRules.isValid(inputValue)) {
            when (operationType) {
                "square" -> return inputValue * inputValue
                "cube" -> return inputValue * inputValue * inputValue
                "sqrt" -> return kotlin.math.sqrt(inputValue)
                "log" -> return kotlin.math.ln(inputValue)
            }
        }
        return 0.0
    }
    
    /**
     * Nested method calls - will be obfuscated
     */
    private fun processNestedCalculations(calculator: Calculator) {
        val result1 = calculator.add(10.0, 20.0)
        val result2 = calculator.multiply(result1, 2.0)
        val result3 = calculator.divide(result2, 5.0)
        
        calculationResult = result3
        operationHistory.add("Nested calculation result: $result3")
    }
    
    /**
     * Method with loops and conditions - will be obfuscated
     */
    private fun processBatchCalculations(items: List<CalculationItem>) {
        for (item in items) {
            if (item.isValid()) {
                calculate(createCalculationList(item))
            }
        }
    }
    
    private fun createCalculationList(item: CalculationItem): CalculationList {
        return CalculationList(listOf(item))
    }
    
    /**
     * Public method that will be kept (not obfuscated due to ProGuard rules)
     */
    fun getCalculationResult(): Double {
        return calculationResult
    }
    
    fun getOperationHistory(): List<String> {
        return operationHistory.toList()
    }
}

/**
 * Supporting classes that will be obfuscated
 */
class CalculationList(private val items: List<CalculationItem>) {
    private var currentIndex = 0
    
    fun hasMore(): Boolean {
        return currentIndex < items.size
    }
    
    fun getNext(includeValidation: Boolean): CalculationItem {
        val item = items[currentIndex]
        currentIndex++
        return if (includeValidation && !item.isValid()) {
            CalculationItem("error", 0.0, false)
        } else {
            item
        }
    }
}

class CalculationItem(
    val operation: String,
    val value: Double,
    private val isValid: Boolean = true
) {
    fun calculate() {
        // Simulate calculation
    }
    
    fun isValid(): Boolean {
        return isValid
    }
}

class CalculationData(
    val baseValue: Double,
    val multiplier: Double,
    val divisor: Double
)

class ValidationRules {
    fun isValid(value: Double): Boolean {
        return value > 0 && value.isFinite()
    }
}

class Calculator {
    fun add(a: Double, b: Double): Double = a + b
    fun subtract(a: Double, b: Double): Double = a - b
    fun multiply(a: Double, b: Double): Double = a * b
    fun divide(a: Double, b: Double): Double = a / b
}
