package com.android.calculator

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.preference.PreferenceManager
import com.android.calculator.history.History
import com.android.calculator.util.MyPreferenceMigrator
import com.google.gson.Gson

class MyPreferences(context: Context) {

    var ctx = context

    // https://proandroiddev.com/dark-mode-on-android-app-with-kotlin-dc759fc5f0e1
    companion object {
        private const val THEME = "android_calculator.THEME"
        private const val FORCE_DAY_NIGHT = "android_calculator.FORCE_DAY_NIGHT"

        private const val KEY_VIBRATION_STATUS = "android_calculator.KEY_VIBRATION_STATUS"
        private const val KEY_HISTORY = "android_calculator.HISTORY_ELEMENTS"
        private const val KEY_PREVENT_PHONE_FROM_SLEEPING = "android_calculator.PREVENT_PHONE_FROM_SLEEPING"
        private const val KEY_HISTORY_SIZE = "android_calculator.HISTORY_SIZE"
        private const val KEY_SCIENTIFIC_MODE_ENABLED_BY_DEFAULT = "android_calculator.SCIENTIFIC_MODE_ENABLED_BY_DEFAULT"
        private const val KEY_RADIANS_INSTEAD_OF_DEGREES_BY_DEFAULT = "android_calculator.RADIANS_INSTEAD_OF_DEGREES_BY_DEFAULT"
        private const val KEY_NUMBER_PRECISION = "android_calculator.NUMBER_PRECISION"
        private const val KEY_WRITE_NUMBER_INTO_SCIENTIC_NOTATION = "android_calculator.WRITE_NUMBER_INTO_SCIENTIC_NOTATION"
        private const val KEY_LONG_CLICK_TO_COPY_VALUE = "android_calculator.LONG_CLICK_TO_COPY_VALUE"
        private const val KEY_ADD_MODULO_BUTTON = "android_calculator.ADD_MODULO_BUTTON"
        private const val KEY_SPLIT_PARENTHESIS_BUTTON = "android_calculator.SPLIT_PARENTHESIS_BUTTON"
        private const val KEY_DELETE_HISTORY_ON_SWIPE = "android_calculator.DELETE_HISTORY_ELEMENT_ON_SWIPE"
        private const val KEY_AUTO_SAVE_CALCULATION_WITHOUT_EQUAL_BUTTON = "android_calculator.AUTO_SAVE_CALCULATION_WITHOUT_EQUAL_BUTTON"
        private const val KEY_NUMBERING_SYSTEM = "android_calculator.NUMBERING_SYSTEM"
        private const val KEY_SHOW_ON_LOCK_SCREEN = "android_calculator.KEY_SHOW_ON_LOCK_SCREEN"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    var theme = preferences.getInt(THEME, -1)
        set(value) = preferences.edit().putInt(THEME, value).apply()
    var forceDayNight = preferences.getInt(FORCE_DAY_NIGHT, MODE_NIGHT_UNSPECIFIED)
        set(value) = preferences.edit().putInt(FORCE_DAY_NIGHT, value).apply()

    var vibrationMode = preferences.getBoolean(KEY_VIBRATION_STATUS, true)
        set(value) = preferences.edit().putBoolean(KEY_VIBRATION_STATUS, value).apply()
    private val currentScientificModeTypes= MyPreferenceMigrator.migrateScientificMode(preferences, KEY_SCIENTIFIC_MODE_ENABLED_BY_DEFAULT)
    var scientificMode = preferences.getInt(KEY_SCIENTIFIC_MODE_ENABLED_BY_DEFAULT, currentScientificModeTypes)
        set(value) = preferences.edit().putInt(KEY_SCIENTIFIC_MODE_ENABLED_BY_DEFAULT, value).apply()
    var useRadiansByDefault = preferences.getBoolean(KEY_RADIANS_INSTEAD_OF_DEGREES_BY_DEFAULT, false)
        set(value) = preferences.edit().putBoolean(KEY_RADIANS_INSTEAD_OF_DEGREES_BY_DEFAULT, value).apply()
    private var history = preferences.getString(KEY_HISTORY, null)
        set(value) = preferences.edit().putString(KEY_HISTORY, value).apply()
    var preventPhoneFromSleeping = preferences.getBoolean(KEY_PREVENT_PHONE_FROM_SLEEPING, false)
        set(value) = preferences.edit().putBoolean(KEY_PREVENT_PHONE_FROM_SLEEPING, value).apply()
    var historySize = preferences.getInt(KEY_HISTORY_SIZE, 100)
        set(value) = preferences.edit().putInt(KEY_HISTORY_SIZE, value).apply()
    var numberPrecision = preferences.getInt(KEY_NUMBER_PRECISION, 10)
        set(value) = preferences.edit().putInt(KEY_NUMBER_PRECISION, value).apply()
    var writeNumberIntoScientificNotation = preferences.getBoolean(KEY_WRITE_NUMBER_INTO_SCIENTIC_NOTATION, false)
        set(value) = preferences.edit().putBoolean(KEY_WRITE_NUMBER_INTO_SCIENTIC_NOTATION, value).apply()
    var longClickToCopyValue = preferences.getBoolean(KEY_LONG_CLICK_TO_COPY_VALUE, true)
        set(value) = preferences.edit().putBoolean(KEY_LONG_CLICK_TO_COPY_VALUE, value).apply()
    var addModuloButton = preferences.getBoolean(KEY_ADD_MODULO_BUTTON, false)
        set(value) = preferences.edit().putBoolean(KEY_ADD_MODULO_BUTTON, value).apply()
    var splitParenthesisButton = preferences.getBoolean(KEY_SPLIT_PARENTHESIS_BUTTON, false)
        set(value) = preferences.edit().putBoolean(KEY_SPLIT_PARENTHESIS_BUTTON, value).apply()
    var deleteHistoryOnSwipe = preferences.getBoolean(KEY_DELETE_HISTORY_ON_SWIPE, true)
        set(value) = preferences.edit().putBoolean(KEY_DELETE_HISTORY_ON_SWIPE, value).apply()
    var autoSaveCalculationWithoutEqualButton = preferences.getBoolean(KEY_AUTO_SAVE_CALCULATION_WITHOUT_EQUAL_BUTTON, false)
        set(value) = preferences.edit().putBoolean(KEY_AUTO_SAVE_CALCULATION_WITHOUT_EQUAL_BUTTON, value).apply()
    var numberingSystem = preferences.getInt(KEY_NUMBERING_SYSTEM, 0)
        set(value) = preferences.edit().putInt(KEY_NUMBERING_SYSTEM, value).apply()
    var showOnLockScreen = preferences.getBoolean(KEY_SHOW_ON_LOCK_SCREEN, false)
        set(value) = preferences.edit().putBoolean(KEY_SHOW_ON_LOCK_SCREEN, value).apply()

    fun getHistory(): MutableList<History> {
        return if (history == null) {
            mutableListOf()
        } else {
            val gson = Gson()
            val historyArray = gson.fromJson(history, Array<History>::class.java)
            historyArray.toMutableList()
        }
    }

    fun saveHistory(history: MutableList<History>) {
        val gson = Gson()
        this.history = gson.toJson(history)
    }

    fun clearHistory() {
        this.history = null
    }
}
