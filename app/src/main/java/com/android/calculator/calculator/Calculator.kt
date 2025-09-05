package com.android.calculator.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

var division_by_0 = "division_by_0"
var domain_error = "domain_error"
var syntax_error = "syntax_error"
var is_infinity = "is_infinity"
var require_real_number = "require_real_number"

class Calculator(
    private val precision: Int = 10
) {
    private val mathContext = MathContext(precision, RoundingMode.HALF_UP)

    fun evaluate(expression: String, isDegreeMode: Boolean = true): BigDecimal {
        try {
            val cleanExpression = preprocessExpression(expression)
            return evaluateExpression(cleanExpression, isDegreeMode)
        } catch (e: ArithmeticException) {
            if (e.message?.contains("/ by zero") == true) {
                throw Exception(division_by_0)
            }
            throw Exception(syntax_error)
        } catch (e: NumberFormatException) {
            throw Exception(syntax_error)
        } catch (e: Exception) {
            when (e.message) {
                division_by_0, domain_error, syntax_error, is_infinity, require_real_number -> throw e
                else -> throw Exception(syntax_error)
            }
        }
    }

    private fun preprocessExpression(expression: String): String {
        var result = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", PI.toString())
            .replace("e", Math.E.toString())
            .trim()

        // Handle implicit multiplication (e.g., "2(3)" -> "2*(3)")
        result = addImplicitMultiplication(result)

        return result
    }

    private fun addImplicitMultiplication(expression: String): String {
        var result = expression
        
        // Add multiplication between number and opening parenthesis
        result = result.replace(Regex("(\\d)\\("), "$1*(")
        
        // Add multiplication between closing and opening parenthesis
        result = result.replace(Regex("\\)\\("), ")*(")
        
        // Add multiplication between closing parenthesis and number
        result = result.replace(Regex("\\)(\\d)"), ")*$1")
        
        return result
    }

    private fun evaluateExpression(expression: String, isDegreeMode: Boolean): BigDecimal {
        // This is a simplified implementation
        // In a real calculator, you would implement a proper expression parser
        // For now, we'll handle basic operations
        
        if (expression.isEmpty()) {
            throw Exception(syntax_error)
        }

        // Handle basic arithmetic operations
        return try {
            val result = evaluateBasicExpression(expression)
            
            if (result.toString().contains("Infinity")) {
                throw Exception(is_infinity)
            }
            
            result
        } catch (e: ArithmeticException) {
            throw Exception(division_by_0)
        }
    }

    private fun evaluateBasicExpression(expression: String): BigDecimal {
        // Simple evaluation for basic operations
        // This would need to be expanded for a full calculator
        
        try {
            // Handle simple number
            if (expression.matches(Regex("-?\\d+(\\.\\d+)?"))) {
                return BigDecimal(expression, mathContext)
            }
            
            // Handle basic operations (simplified)
            when {
                expression.contains("+") -> {
                    val parts = expression.split("+")
                    if (parts.size == 2) {
                        return BigDecimal(parts[0].trim(), mathContext)
                            .add(BigDecimal(parts[1].trim(), mathContext), mathContext)
                    }
                }
                expression.contains("-") && !expression.startsWith("-") -> {
                    val parts = expression.split("-")
                    if (parts.size == 2) {
                        return BigDecimal(parts[0].trim(), mathContext)
                            .subtract(BigDecimal(parts[1].trim(), mathContext), mathContext)
                    }
                }
                expression.contains("*") -> {
                    val parts = expression.split("*")
                    if (parts.size == 2) {
                        return BigDecimal(parts[0].trim(), mathContext)
                            .multiply(BigDecimal(parts[1].trim(), mathContext), mathContext)
                    }
                }
                expression.contains("/") -> {
                    val parts = expression.split("/")
                    if (parts.size == 2) {
                        val divisor = BigDecimal(parts[1].trim(), mathContext)
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw Exception(division_by_0)
                        }
                        return BigDecimal(parts[0].trim(), mathContext)
                            .divide(divisor, mathContext)
                    }
                }
            }
            
            // If we can't parse it, try to convert directly
            return BigDecimal(expression, mathContext)
            
        } catch (e: NumberFormatException) {
            throw Exception(syntax_error)
        } catch (e: ArithmeticException) {
            throw Exception(division_by_0)
        }
    }

    // Scientific functions
    fun sin(value: BigDecimal, isDegreeMode: Boolean): BigDecimal {
        val radians = if (isDegreeMode) {
            value.multiply(BigDecimal(PI), mathContext).divide(BigDecimal(180), mathContext)
        } else {
            value
        }
        return BigDecimal(sin(radians.toDouble()), mathContext)
    }

    fun cos(value: BigDecimal, isDegreeMode: Boolean): BigDecimal {
        val radians = if (isDegreeMode) {
            value.multiply(BigDecimal(PI), mathContext).divide(BigDecimal(180), mathContext)
        } else {
            value
        }
        return BigDecimal(cos(radians.toDouble()), mathContext)
    }

    fun tan(value: BigDecimal, isDegreeMode: Boolean): BigDecimal {
        val radians = if (isDegreeMode) {
            value.multiply(BigDecimal(PI), mathContext).divide(BigDecimal(180), mathContext)
        } else {
            value
        }
        return BigDecimal(tan(radians.toDouble()), mathContext)
    }

    fun sqrt(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw Exception(domain_error)
        }
        return BigDecimal(sqrt(value.toDouble()), mathContext)
    }

    fun ln(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw Exception(domain_error)
        }
        return BigDecimal(ln(value.toDouble()), mathContext)
    }

    fun log10(value: BigDecimal): BigDecimal {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw Exception(domain_error)
        }
        return BigDecimal(log10(value.toDouble()), mathContext)
    }

    fun pow(base: BigDecimal, exponent: BigDecimal): BigDecimal {
        return BigDecimal(base.toDouble().pow(exponent.toDouble()), mathContext)
    }

    fun factorial(n: BigDecimal): BigDecimal {
        if (n.remainder(BigDecimal.ONE, mathContext).compareTo(BigDecimal.ZERO) != 0) {
            throw Exception(require_real_number)
        }
        
        val intValue = n.toInt()
        if (intValue < 0) {
            throw Exception(domain_error)
        }
        
        var result = BigDecimal.ONE
        for (i in 1..intValue) {
            result = result.multiply(BigDecimal(i), mathContext)
        }
        return result
    }
}
