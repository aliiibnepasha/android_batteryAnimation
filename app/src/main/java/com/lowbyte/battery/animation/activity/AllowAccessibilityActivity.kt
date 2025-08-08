package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.ads.BannerAdHelper
import com.lowbyte.battery.animation.databinding.ActivityAllowAccecibilityBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.getBannerPermissionId
import com.lowbyte.battery.animation.utils.AnimationUtils.isBannerPermissionSettings
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class AllowAccessibilityActivity :  BaseActivity() {

    private lateinit var binding: ActivityAllowAccecibilityBinding
    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAllowAccecibilityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        preferences = AppPreferences.getInstance(this)

        binding.buttonForSetting.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_to_setting", null)

            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            finish()
        }
        loadBannerAd()
    }

    private fun loadBannerAd() {
        if (preferences.isProUser || !isBannerPermissionSettings) {
            binding.bannerAdPermission.visibility = View.GONE
            return
        }

        BannerAdHelper.loadBannerAd(
            context = this,
            container = binding.bannerAdPermission,
            bannerAdId = getBannerPermissionId(false),
            isCollapsable = false,
            isProUser = preferences.isProUser,
            isBannerPermissionSettings
        )
    }
}