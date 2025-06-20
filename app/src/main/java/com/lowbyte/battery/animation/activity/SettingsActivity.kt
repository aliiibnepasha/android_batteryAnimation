package com.lowbyte.battery.animation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivitySettingsBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.initialLanguageCode
import com.lowbyte.battery.animation.utils.AnimationUtils.openUrl
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.LocaleHelper

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferences: AppPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}