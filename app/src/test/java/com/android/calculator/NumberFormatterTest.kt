package com.android.calculator

import com.android.calculator.calculator.parser.NumberFormatter
import com.android.calculator.calculator.parser.NumberingSystem
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.math.BigDecimal

class NumberFormatterTest {

    @Test
    fun `given a decimal number when formatting then format with grouping separator`() {
        // Given
        val number = BigDecimal("12345")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.NONE)

        // Then
        val expected = "12,345"
        assertEquals(expected, result)
    }

    @Test
    fun `given a floating-point number when formatting then format with grouping separator and decimal separator`() {
        // Given
        val number = BigDecimal("1234.567")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.NONE)

        // Then
        val expected = "1,234.567"
        assertEquals(expected, result)
    }

    @Test
    fun `given a large number when formatting then format with grouping separator`() {
        // Given
        val number = BigDecimal("9223372036854775807")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.NONE)

        // Then
        val expected = "9,223,372,036,854,775,807"
        assertEquals(expected, result)
    }

    @Test
    fun `given a negative integer number when formatting then format with grouping separator and negative sign`() {
        // Given
        val number = BigDecimal("-12345")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.NONE)

        // Then
        val expected = "-12,345"
        assertEquals(expected, result)
    }

    @Test
    fun `given a negative floating-point number when formatting then format with grouping separator, decimal separator, and negative sign`() {
        // Given
        val number = BigDecimal("-1234.567")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.NONE)

        // Then
        val expected = "-1,234.567"
        assertEquals(expected, result)
    }

    @Test
    fun `given a zero input when formatting then return zero`() {
        // Given
        val number = BigDecimal("0")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.NONE)

        // Then
        val expected = "0"
        assertEquals(expected, result)
    }

    @Test
    fun `given a zero input with decimal when formatting then return zero with decimal`() {
        // Given
        val number = BigDecimal("0.0")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.NONE)

        // Then
        val expected = "0"
        assertEquals(expected, result)
    }

    @Test
    fun `given a floating point number then formatting in Indian Numbering System`() {
        // Given
        val number = BigDecimal("1234567.890")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "12,34,567.89"
        assertEquals(expected, result)
    }

    @Test
    fun `given a large integer number then formatting in Indian Numbering System`() {
        // Given
        val number = BigDecimal("987654321")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "98,76,54,321"
        assertEquals(expected, result)
    }

    @Test
    fun `given a number with two-digit integer part then no formatting is applied`() {
        // Given
        val number = BigDecimal("12.34")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "12.34"
        assertEquals(expected, result)
    }

    @Test
    fun `given a number with exactly three digits then no comma is added`() {
        // Given
        val number = BigDecimal("999")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "999"
        assertEquals(expected, result)
    }

    @Test
    fun `given a number with four digits then a comma is added correctly`() {
        // Given
        val number = BigDecimal("1234")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "1,234"
        assertEquals(expected, result)
    }

    @Test
    fun `given a number with five digits then a comma is added correctly`() {
        // Given
        val number = BigDecimal("12345")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "12,345"
        assertEquals(expected, result)
    }

    @Test
    fun `given a number with six digits then commas are added correctly`() {
        // Given
        val number = BigDecimal("123456")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "1,23,456"
        assertEquals(expected, result)
    }

    @Test
    fun `given a number with decimals then only integer part is formatted`() {
        // Given
        val number = BigDecimal("9020555.555")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "90,20,555.555"
        assertEquals(expected, result)
    }

    @Test
    fun `given a small number then no formatting is applied`() {
        // Given
        val number = BigDecimal("5")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "5"
        assertEquals(expected, result)
    }

    @Test
    fun `given a negative number then formatting is applied correctly`() {
        // Given
        val number = BigDecimal("-1234567")

        // When
        val result = formatter.format(number, 10, false, NumberingSystem.INDIAN)

        // Then
        val expected = "-12,34,567"
        assertEquals(expected, result)
    }

    @Test
    fun `given scientific notation requirement then format in scientific notation`() {
        // Given
        val number = BigDecimal("1234567890")

        // When
        val result = formatter.format(number, 5, true, NumberingSystem.NONE)

        // Then
        val expected = "1.2346E9"
        assertEquals(expected, result)
    }

    companion object {
        lateinit var formatter: NumberFormatter

        @JvmStatic
        @BeforeClass
        fun setup(): Unit {
            formatter = NumberFormatter()
        }
    }
}
