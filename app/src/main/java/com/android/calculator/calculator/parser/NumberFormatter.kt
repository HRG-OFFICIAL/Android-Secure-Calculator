package com.android.calculator.calculator.parser

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class NumberFormatter {

    fun format(
        number: BigDecimal,
        precision: Int = 10,
        useScientificNotation: Boolean = false,
        numberingSystem: NumberingSystem = NumberingSystem.NONE
    ): String {
        return when {
            useScientificNotation && shouldUseScientificNotation(number) -> {
                formatScientific(number, precision)
            }
            else -> {
                formatStandard(number, precision, numberingSystem)
            }
        }
    }

    private fun shouldUseScientificNotation(number: BigDecimal): Boolean {
        val absValue = number.abs()
        return absValue.compareTo(BigDecimal("1E6")) >= 0 || 
               (absValue.compareTo(BigDecimal.ZERO) > 0 && absValue.compareTo(BigDecimal("1E-4")) < 0)
    }

    private fun formatScientific(number: BigDecimal, precision: Int): String {
        val format = DecimalFormat("0.${createPrecisionPattern(precision - 1)}E0")
        return format.format(number.toDouble())
    }

    private fun formatStandard(number: BigDecimal, precision: Int, numberingSystem: NumberingSystem): String {
        // Round to specified precision
        val rounded = number.setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros()
        
        val symbols = when (numberingSystem) {
            NumberingSystem.EUROPEAN -> DecimalFormatSymbols(Locale.GERMANY)
            NumberingSystem.INDIAN -> createIndianSymbols()
            else -> DecimalFormatSymbols(Locale.US)
        }

        val pattern = when (numberingSystem) {
            NumberingSystem.INDIAN -> createIndianPattern()
            NumberingSystem.EUROPEAN -> "#,##0.###"
            else -> "#,##0.###"
        }

        val format = DecimalFormat(pattern, symbols)
        format.maximumFractionDigits = precision
        format.isGroupingUsed = numberingSystem != NumberingSystem.NONE

        return format.format(rounded.toDouble())
    }

    private fun createPrecisionPattern(digits: Int): String {
        return "0".repeat(digits)
    }

    private fun createIndianSymbols(): DecimalFormatSymbols {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        symbols.decimalSeparator = '.'
        return symbols
    }

    private fun createIndianPattern(): String {
        // Indian numbering system: 1,00,000 instead of 100,000
        return "##,##,##0.###"
    }

    fun removeGroupingSeparators(text: String, numberingSystem: NumberingSystem): String {
        return when (numberingSystem) {
            NumberingSystem.EUROPEAN -> text.replace(".", "").replace(",", ".")
            NumberingSystem.INDIAN, NumberingSystem.NONE -> text.replace(",", "")
        }
    }

    fun addGroupingSeparators(text: String, numberingSystem: NumberingSystem): String {
        try {
            val number = BigDecimal(text)
            return formatStandard(number, 10, numberingSystem)
        } catch (e: NumberFormatException) {
            return text
        }
    }
}
