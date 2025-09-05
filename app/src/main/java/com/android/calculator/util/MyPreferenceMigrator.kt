package com.android.calculator.util

import android.content.SharedPreferences

class MyPreferenceMigrator {

    companion object {
        fun migrateScientificMode(preferences: SharedPreferences, key: String): Int {
            // Check if old boolean preference exists
            if (preferences.contains("${key}_boolean")) {
                val oldValue = preferences.getBoolean("${key}_boolean", false)
                val newValue = if (oldValue) ScientificMode.ON.ordinal else ScientificMode.OFF.ordinal
                
                // Save new value and remove old one
                preferences.edit()
                    .putInt(key, newValue)
                    .remove("${key}_boolean")
                    .apply()
                
                return newValue
            }
            
            // Return default if no migration needed
            return ScientificMode.OFF.ordinal
        }
        
        fun migrateThemePreferences(preferences: SharedPreferences) {
            // Migrate old theme preferences if they exist
            if (preferences.contains("old_theme_key")) {
                val oldTheme = preferences.getInt("old_theme_key", 0)
                preferences.edit()
                    .putInt("android_calculator.THEME", oldTheme)
                    .remove("old_theme_key")
                    .apply()
            }
        }
        
        fun migrateAllPreferences(preferences: SharedPreferences) {
            // Migrate all old preferences to new package structure
            val oldPrefixes = listOf("harsh.opencalculator", "harsh.android_calculator")
            val newPrefix = "android_calculator"
            
            val allPreferences = preferences.all
            val editor = preferences.edit()
            
            for ((key, value) in allPreferences) {
                for (oldPrefix in oldPrefixes) {
                    if (key.startsWith(oldPrefix)) {
                        val newKey = key.replace(oldPrefix, newPrefix)
                        
                        when (value) {
                            is String -> editor.putString(newKey, value)
                            is Int -> editor.putInt(newKey, value)
                            is Boolean -> editor.putBoolean(newKey, value)
                            is Float -> editor.putFloat(newKey, value)
                            is Long -> editor.putLong(newKey, value)
                        }
                        
                        editor.remove(key)
                        break // Only migrate once per key
                    }
                }
            }
            
            editor.apply()
        }
    }
}
