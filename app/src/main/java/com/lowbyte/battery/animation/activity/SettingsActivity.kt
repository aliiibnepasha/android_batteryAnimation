package com.lowbyte.battery.animation.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivitySettingsBinding
import com.lowbyte.battery.animation.databinding.DialogPermissionAccessBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.initialLanguageCode
import com.lowbyte.battery.animation.utils.AnimationUtils.openUrl
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.LocaleHelper
import androidx.core.graphics.drawable.toDrawable

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferences: AppPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        FirebaseAnalyticsUtils.logScreenView(this, "SettingsScreen")
        FirebaseAnalyticsUtils.startScreenTimer("SettingsScreen")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", mapOf("screen" to "SettingsScreen"))
            finish()
        }

        binding.proView.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_pro_upgrade", null)
            startActivity(Intent(this, ProActivity::class.java))
        }

         binding.viewHowToUse.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_how_to_use", null)
            startActivity(Intent(this, HowToUseActivity::class.java))
        }

        binding.ivNextDark.setOnCheckedChangeListener { _, isChecked ->
            // You can log theme change if needed:
            // FirebaseAnalyticsUtils.logClickEvent(this, "toggle_dark_mode", mapOf("enabled" to isChecked.toString()))
        }

        binding.viewTerms.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_terms", null)
            val url = getString(R.string.privacy_policy_url)
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }

        binding.viewPrivacy.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_privacy", null)
            val url = getString(R.string.terms_of_service_url)
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }

        binding.viewLanguage.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_language", null)
            initialLanguageCode = LocaleHelper.getLanguage(this)
            startActivity(Intent(this, LanguagesActivity::class.java))
            finish()
        }

        binding.viewRestoreSub.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_restore_subscription", null)
            openUrl(this, getString(R.string.restore_sub_url))
        }
         binding.viewPermissionAccess.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_restore_subscription", null)
             showPermissionAccessDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        if (preferences.isProUser) {
            binding.proView.visibility = View.GONE
            binding.viewRestoreSub.visibility = View.VISIBLE
        }else{
            binding.viewRestoreSub.visibility = View.GONE
            binding.proView.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "SettingsScreen")
    }
    private fun showPermissionAccessDialog() {
        val dialog = Dialog(this)
        val binding = DialogPermissionAccessBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(true)
        val bullets = SpannableStringBuilder()

        // ✅ Function to add each bullet item
        fun addItem(title: String, why: String, turnOff: String) {
            val block = SpannableString("• $title\nWhy: $why\nTurn off: $turnOff\n\n")

            // 🔵 Make title Bold + Blue
            block.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue_app_color)),
                2, 2 + title.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            block.setSpan(
                StyleSpan(Typeface.BOLD),
                2, 2 + title.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            bullets.append(block)
        }

        // ✅ Add items
        addItem(
            title = "Accessibility",
            why = "Needed to display emoji battery overlays on the status bar.",
            turnOff = "Go to Accessibility settings → Installed Services → Emoji Battery → Off"
        )

        addItem(
            title = "Notifications",
            why = "Needed to show incoming alerts on Dynamic Bar.",
            turnOff = "Go to Settings → Notifications → Device & App Notifications → Emoji Battery → Off"
        )

        addItem(
            title = "Bluetooth",
            why = "Needed to display connected device and battery info in the status bar.",
            turnOff = "Go to Settings → Apps → Emoji Battery → Permissions → Bluetooth → Deny"
        )

        // ✅ Set Description label text with White color
        binding.tvDescriptionLabel.text = ""
        binding.tvDescriptionLabel.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        binding.tvBullets.text = bullets

        // ✅ Close handlers
        binding.btnClose.setOnClickListener { dialog.dismiss() }
        binding.btnDismiss.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}