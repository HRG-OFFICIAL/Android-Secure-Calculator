package com.android.calculator.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.calculator.BuildConfig
import com.android.calculator.R
import com.android.calculator.Themes
import com.android.calculator.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply theme
        Themes.applyTheme(this)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up back button
        binding.aboutBackButtonHitbox.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set version info
        binding.aboutAppVersion.text = getString(R.string.about_other_version) + " " + BuildConfig.VERSION_NAME

        // Set up click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // GitHub repository
        binding.aboutGithub.setOnClickListener {
            openUrl("https://github.com/yourname/Android_Calculator")
        }

        // License
        binding.aboutLicense.setOnClickListener {
            showLicenseDialog()
        }

        // Buy Me a Coffee
        binding.aboutBuyMeCoffee.setOnClickListener {
            openUrl("https://buymeacoffee.com/yourusername")
        }

        // Rate app
        binding.aboutRate.setOnClickListener {
            rateApp()
        }

        // Privacy policy
        binding.aboutPrivacyPolicy.setOnClickListener {
            openUrl("https://github.com/yourname/Android_Calculator/blob/main/PRIVACY.md")
        }

        // Discord
        binding.aboutDiscord.setOnClickListener {
            openUrl("https://discord.gg/yourserver")
        }

        // Translate
        binding.aboutTranslate.setOnClickListener {
            openUrl("https://github.com/yourname/Android_Calculator/blob/main/CONTRIBUTING.md#translation")
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.error_opening_url), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLicenseDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.license)
            .setMessage(R.string.license_text)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun rateApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
            startActivity(intent)
        } catch (e: Exception) {
            openUrl("https://play.google.com/store/apps/details?id=${packageName}")
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " https://play.google.com/store/apps/details?id=${packageName}")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
    }

}
