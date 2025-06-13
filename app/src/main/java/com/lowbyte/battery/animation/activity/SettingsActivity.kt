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
import com.lowbyte.battery.animation.utils.AnimationUtils.openUrl
import com.lowbyte.battery.animation.utils.AppPreferences
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            finish() // Back button behavior
        }

        binding.proView.setOnClickListener {
            startActivity(Intent(this, ProActivity::class.java))
        }

        binding.ivNextDark.setOnCheckedChangeListener { _, isChecked ->

        }

        binding.viewTerms.setOnClickListener {
            val url = getString(R.string.privacy_policy_url)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
            }
            startActivity(intent)
        }

        binding.viewPrivacy.setOnClickListener {
            val url = getString(R.string.terms_of_service_url)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
            }
            startActivity(intent)
        }

        binding.viewLanguage.setOnClickListener {
            startActivity(Intent(this, LanguagesActivity::class.java))
            finish()
        }
        binding.viewRestoreSub.setOnClickListener {
            openUrl(this, getString(R.string.restore_sub_url))
        }

    }

    override fun onResume() {
        if (preferences.isProUser) {
            binding.proView.visibility = View.GONE
            binding.viewRestoreSub.visibility = View.VISIBLE
        }

        super.onResume()
    }
}