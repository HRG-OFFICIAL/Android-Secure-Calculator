package com.android.calculator.util

enum class ScientificMode {
    OFF,
    ON,
    ALWAYS;

    companion object {
        fun fromInt(value: Int): ScientificMode {
            return when (value) {
                0 -> OFF
                1 -> ON
                2 -> ALWAYS
                else -> OFF
            }
        }
    }
}
