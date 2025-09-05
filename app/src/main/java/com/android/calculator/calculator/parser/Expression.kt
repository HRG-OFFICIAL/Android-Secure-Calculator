package com.android.calculator.calculator.parser

class Expression {

    fun getCleanExpression(calculation: String, decimalSeparatorSymbol: String, groupingSeparatorSymbol: String): String {
        var cleanCalculation = replaceSymbolsFromCalculation(calculation, decimalSeparatorSymbol, groupingSeparatorSymbol)
        cleanCalculation = addMultiply(cleanCalculation)
        if (cleanCalculation.contains('√')) {
            cleanCalculation = formatSquare(cleanCalculation)
        }
        if (cleanCalculation.contains('%')) {
            cleanCalculation = getPercentString(cleanCalculation)
            cleanCalculation = cleanCalculation.replace("%", "/100")
        }
        if (cleanCalculation.contains('!')) {
            cleanCalculation = formatFactorial(cleanCalculation)
        }

        cleanCalculation = addParenthesis(cleanCalculation)

        return cleanCalculation
    }

    private fun replaceSymbolsFromCalculation(calculation: String, decimalSeparatorSymbol: String, groupingSeparatorSymbol: String): String {
        var calculation2 = calculation.replace('×', '*')
        calculation2 = calculation2.replace('÷', '/')
        calculation2 = calculation2.replace("log₂(", "logtwo(")
        calculation2 = calculation2.replace("log₁₀(", "log(")
        calculation2 = calculation2.replace("ln(", "ln(")
        calculation2 = calculation2.replace("√(", "sqrt(")
        calculation2 = calculation2.replace("∛(", "cbrt(")
        calculation2 = calculation2.replace("sin⁻¹(", "asin(")
        calculation2 = calculation2.replace("cos⁻¹(", "acos(")
        calculation2 = calculation2.replace("tan⁻¹(", "atan(")
        calculation2 = calculation2.replace("sinh⁻¹(", "asinh(")
        calculation2 = calculation2.replace("cosh⁻¹(", "acosh(")
        calculation2 = calculation2.replace("tanh⁻¹(", "atanh(")
        calculation2 = calculation2.replace("π", "pi")
        calculation2 = calculation2.replace("e", "e")

        // Handle decimal and grouping separators
        if (decimalSeparatorSymbol != ".") {
            calculation2 = calculation2.replace(decimalSeparatorSymbol, ".")
        }
        if (groupingSeparatorSymbol.isNotEmpty()) {
            calculation2 = calculation2.replace(groupingSeparatorSymbol, "")
        }

        return calculation2
    }

    private fun addMultiply(calculation: String): String {
        var result = calculation

        // Add multiplication between number and opening parenthesis
        result = result.replace(Regex("(\\d)\\("), "$1*(")
        
        // Add multiplication between closing and opening parenthesis
        result = result.replace(Regex("\\)\\("), ")*(")
        
        // Add multiplication between closing parenthesis and number
        result = result.replace(Regex("\\)(\\d)"), ")*$1")
        
        // Add multiplication between number and function
        result = result.replace(Regex("(\\d)(sin|cos|tan|log|ln|sqrt|cbrt|asin|acos|atan|sinh|cosh|tanh|asinh|acosh|atanh)"), "$1*$2")
        
        // Add multiplication between constant and number/function
        result = result.replace(Regex("(pi|e)(\\d|sin|cos|tan|log|ln|sqrt|cbrt|asin|acos|atan|sinh|cosh|tanh|asinh|acosh|atanh|\\()"), "$1*$2")
        
        // Add multiplication between number and constant
        result = result.replace(Regex("(\\d)(pi|e)"), "$1*$2")

        return result
    }

    private fun formatSquare(calculation: String): String {
        var result = calculation
        
        // Replace √number with sqrt(number)
        result = result.replace(Regex("√(\\d+(?:\\.\\d+)?)"), "sqrt($1)")
        
        // Replace √(expression) with sqrt(expression)
        result = result.replace("√", "sqrt")
        
        return result
    }

    private fun getPercentString(calculation: String): String {
        var result = calculation
        
        // Handle percentage calculations
        // This is a simplified implementation
        // In a real calculator, you'd need more sophisticated percentage handling
        
        return result
    }

    private fun formatFactorial(calculation: String): String {
        var result = calculation
        
        // Replace number! with factorial(number)
        result = result.replace(Regex("(\\d+(?:\\.\\d+)?)!"), "factorial($1)")
        
        // Replace )! with factorial(...)
        var openParenCount = 0
        var factorialIndex = -1
        
        for (i in result.indices) {
            when (result[i]) {
                '(' -> openParenCount++
                ')' -> {
                    openParenCount--
                    if (i + 1 < result.length && result[i + 1] == '!') {
                        factorialIndex = i + 1
                        break
                    }
                }
            }
        }
        
        if (factorialIndex > 0) {
            // Find the matching opening parenthesis
            var closeParenCount = 0
            var startIndex = -1
            
            for (i in factorialIndex - 1 downTo 0) {
                when (result[i]) {
                    ')' -> closeParenCount++
                    '(' -> {
                        closeParenCount--
                        if (closeParenCount == 0) {
                            startIndex = i
                            break
                        }
                    }
                }
            }
            
            if (startIndex >= 0) {
                val expression = result.substring(startIndex, factorialIndex)
                result = result.substring(0, startIndex) + "factorial" + expression + result.substring(factorialIndex + 1)
            }
        }
        
        return result
    }

    private fun addParenthesis(calculation: String): String {
        var result = calculation
        var openCount = 0
        var closeCount = 0
        
        // Count existing parentheses
        for (char in result) {
            when (char) {
                '(' -> openCount++
                ')' -> closeCount++
            }
        }
        
        // Add missing closing parentheses
        repeat(openCount - closeCount) {
            result += ")"
        }
        
        return result
    }

    fun isExpressionBalanced(expression: String): Boolean {
        var openCount = 0
        
        for (char in expression) {
            when (char) {
                '(' -> openCount++
                ')' -> {
                    openCount--
                    if (openCount < 0) return false
                }
            }
        }
        
        return openCount == 0
    }

    fun formatExpression(expression: String): String {
        // Format the expression for display
        var result = expression
        
        // Replace function names with symbols
        result = result.replace("sqrt", "√")
        result = result.replace("pi", "π")
        result = result.replace("*", "×")
        result = result.replace("/", "÷")
        
        return result
    }
}
