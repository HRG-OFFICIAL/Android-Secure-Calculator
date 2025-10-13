package com.android.calculator

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate.*
import com.android.calculator.obfuscation.ObfuscationManager

class AndroidCalculatorApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize comprehensive obfuscation framework
        try {
            Log.d("Obfuscation", "Initializing obfuscation framework...")
            ObfuscationManager.initialize(this)
            Log.d("Obfuscation", "Obfuscation framework initialized successfully")
        } catch (e: Exception) {
            Log.e("Obfuscation", "Failed to initialize obfuscation framework", e)
        }

        // if the theme is overriding the system, the first creation doesn't work properly
        val forceDayNight = MyPreferences(this).forceDayNight
        if (forceDayNight != MODE_NIGHT_UNSPECIFIED && forceDayNight != MODE_NIGHT_FOLLOW_SYSTEM)
            setDefaultNightMode(forceDayNight)
    }
}
