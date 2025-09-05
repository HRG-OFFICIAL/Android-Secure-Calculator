package com.android.calculator.calculator.parser

enum class NumberingSystem {
    NONE,
    EUROPEAN,
    INDIAN;

    companion object {
        fun Int.toNumberingSystem(): NumberingSystem {
            return when (this) {
                0 -> NONE
                1 -> EUROPEAN
                2 -> INDIAN
                else -> NONE
            }
        }
    }
}
