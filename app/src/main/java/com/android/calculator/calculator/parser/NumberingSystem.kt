package com.android.calculator.calculator.parser

enum class NumberingSystem(val value: Int, val description: String) {
    INTERNATIONAL(0, "International Numbering System"),
    INDIAN(1, "Indian Numbering System");

    companion object {
        fun getDescription(value: Int): String {
            return when (value) {
                0 -> INTERNATIONAL.description
                1 -> INDIAN.description
                else -> INTERNATIONAL.description
            }
        }

        fun Int.toNumberingSystem() : NumberingSystem {
            return when (this) {
                0 -> INTERNATIONAL
                1 -> INDIAN
                else -> INTERNATIONAL
            }
        }
    }
}
