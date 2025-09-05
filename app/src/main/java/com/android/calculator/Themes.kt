package com.android.calculator

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Themes(private val context: Context) {

    companion object {

        // Themes
        private const val DEFAULT_THEME_INDEX = 0
        private const val AMOLED_THEME_INDEX = 1
        private const val MATERIAL_YOU_THEME_INDEX = 2

        // used to go from Preference int value to actual theme
        private val themeMap = mapOf(
            DEFAULT_THEME_INDEX to R.style.AppTheme,
            AMOLED_THEME_INDEX to R.style.AmoledTheme,
            MATERIAL_YOU_THEME_INDEX to R.style.MaterialYouTheme
        )

        // Styles - Combinations of theme + day/night mode
        private const val SYSTEM_STYLE_INDEX = 0
        private const val LIGHT_STYLE_INDEX = 1
        private const val DARK_STYLE_INDEX = 2

        // used to go from Preference int value to actual day/night mode
        private val dayNightMap = mapOf(
            SYSTEM_STYLE_INDEX to AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            LIGHT_STYLE_INDEX to AppCompatDelegate.MODE_NIGHT_NO,
            DARK_STYLE_INDEX to AppCompatDelegate.MODE_NIGHT_YES
        )

        fun applyTheme(context: Context) {
            val preferences = MyPreferences(context)
            val themeId = themeMap[preferences.theme] ?: R.style.AppTheme
            (context as Activity).setTheme(themeId)

            // Apply Material You if selected
            if (preferences.theme == MATERIAL_YOU_THEME_INDEX) {
                DynamicColors.applyToActivityIfAvailable(context)
            }
        }

        fun applyDayNightMode(context: Context) {
            val preferences = MyPreferences(context)
            val dayNightMode = dayNightMap[preferences.forceDayNight] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            AppCompatDelegate.setDefaultNightMode(dayNightMode)
        }
    }

    fun openThemeSelector() {
        val preferences = MyPreferences(context)
        val currentTheme = preferences.theme

        val themeNames = arrayOf(
            context.getString(R.string.theme_default),
            context.getString(R.string.theme_amoled),
            context.getString(R.string.theme_material_you)
        )

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.theme_title)
            .setSingleChoiceItems(themeNames, currentTheme) { dialog, which ->
                preferences.theme = which
                (context as Activity).recreate()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun openStyleSelector() {
        val preferences = MyPreferences(context)
        val currentStyle = when (preferences.forceDayNight) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> SYSTEM_STYLE_INDEX
            AppCompatDelegate.MODE_NIGHT_NO -> LIGHT_STYLE_INDEX
            AppCompatDelegate.MODE_NIGHT_YES -> DARK_STYLE_INDEX
            else -> SYSTEM_STYLE_INDEX
        }

        val styleNames = arrayOf(
            context.getString(R.string.style_system),
            context.getString(R.string.style_light),
            context.getString(R.string.style_dark)
        )

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.style_title)
            .setSingleChoiceItems(styleNames, currentStyle) { dialog, which ->
                val dayNightMode = dayNightMap[which] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                preferences.forceDayNight = dayNightMode
                AppCompatDelegate.setDefaultNightMode(dayNightMode)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
