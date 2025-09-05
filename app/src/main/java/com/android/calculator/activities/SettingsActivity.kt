package com.android.calculator.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.android.calculator.MyPreferences
import com.android.calculator.R
import com.android.calculator.Themes
import com.android.calculator.calculator.parser.NumberingSystem
import com.android.calculator.util.ScientificMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme
        Themes.applyTheme(this)
        setContentView(R.layout.settings_activity)

        // Set up toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            setupPreferences()
        }

        private fun setupPreferences() {
            val context = requireContext()
            val preferences = MyPreferences(context)

            // Theme preference
            findPreference<Preference>("theme")?.setOnPreferenceClickListener {
                Themes(context).openThemeSelector()
                true
            }

            // Style preference
            findPreference<Preference>("style")?.setOnPreferenceClickListener {
                Themes(context).openStyleSelector()
                true
            }

            // Scientific mode preference
            findPreference<Preference>("scientific_mode")?.setOnPreferenceClickListener {
                showScientificModeDialog(context, preferences)
                true
            }

            // Numbering system preference
            findPreference<Preference>("numbering_system")?.setOnPreferenceClickListener {
                showNumberingSystemDialog(context, preferences)
                true
            }

            // History size preference
            findPreference<Preference>("history_size")?.setOnPreferenceClickListener {
                showHistorySizeDialog(context, preferences)
                true
            }

            // Number precision preference
            findPreference<Preference>("number_precision")?.setOnPreferenceClickListener {
                showNumberPrecisionDialog(context, preferences)
                true
            }

            // System alert window permission (for overlay)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                findPreference<Preference>("system_alert_window")?.setOnPreferenceClickListener {
                    requestSystemAlertWindowPermission()
                    true
                }
            }
        }

        private fun showScientificModeDialog(context: Context, preferences: MyPreferences) {
            val modes = ScientificMode.values()
            val modeNames = modes.map { 
                when (it) {
                    ScientificMode.OFF -> getString(R.string.scientific_mode_off)
                    ScientificMode.ON -> getString(R.string.scientific_mode_on)
                    ScientificMode.ALWAYS -> getString(R.string.scientific_mode_always)
                }
            }.toTypedArray()

            val currentMode = ScientificMode.fromInt(preferences.scientificMode)
            val currentIndex = modes.indexOf(currentMode)

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.scientific_mode)
                .setSingleChoiceItems(modeNames, currentIndex) { dialog, which ->
                    preferences.scientificMode = modes[which].ordinal
                    (activity as? Activity)?.recreate()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun showNumberingSystemDialog(context: Context, preferences: MyPreferences) {
            val systems = NumberingSystem.values()
            val systemNames = systems.map { 
                when (it) {
                    NumberingSystem.NONE -> getString(R.string.numbering_system_none)
                    NumberingSystem.EUROPEAN -> getString(R.string.numbering_system_european)
                    NumberingSystem.INDIAN -> getString(R.string.numbering_system_indian)
                }
            }.toTypedArray()

            val currentSystem = preferences.numberingSystem
            
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.numbering_system)
                .setSingleChoiceItems(systemNames, currentSystem) { dialog, which ->
                    preferences.numberingSystem = which
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun showHistorySizeDialog(context: Context, preferences: MyPreferences) {
            val sizes = arrayOf("50", "100", "200", "500", "1000")
            val currentSize = preferences.historySize.toString()
            val currentIndex = sizes.indexOf(currentSize).takeIf { it >= 0 } ?: 1

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.history_size)
                .setSingleChoiceItems(sizes, currentIndex) { dialog, which ->
                    preferences.historySize = sizes[which].toInt()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun showNumberPrecisionDialog(context: Context, preferences: MyPreferences) {
            val precisions = arrayOf("5", "10", "15", "20")
            val currentPrecision = preferences.numberPrecision.toString()
            val currentIndex = precisions.indexOf(currentPrecision).takeIf { it >= 0 } ?: 1

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.number_precision)
                .setSingleChoiceItems(precisions, currentIndex) { dialog, which ->
                    preferences.numberPrecision = precisions[which].toInt()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun requestSystemAlertWindowPermission() {
            if (!Settings.canDrawOverlays(requireContext())) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
            }
        }
    }
}
