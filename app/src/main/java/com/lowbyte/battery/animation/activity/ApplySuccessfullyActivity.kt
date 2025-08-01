package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeEmojiHelper
import com.lowbyte.battery.animation.databinding.ActivityApplySuccessfullyBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeInsideId
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils // Make sure this exists

class ApplySuccessfullyActivity : BaseActivity() {

    private lateinit var binding: ActivityApplySuccessfullyBinding
    private var nativeAdHelper: NativeEmojiHelper? = null
    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  enableEdgeToEdge()

        binding = ActivityApplySuccessfullyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        FirebaseAnalyticsUtils.logScreenView(this, "ApplySuccessfullyScreen")
        FirebaseAnalyticsUtils.startScreenTimer("ApplySuccessfullyScreen")

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonEmojiHome.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_home_button", mapOf("source" to "ApplySuccessfullyScreen"))
            AdManager.showInterstitialAd(this,isFullscreenApplyEmojiEnabled,true) {
                finish()
                Log.e("Ads", "FullScreenTobeShoe")
            }
        }

        binding.buttonCustomizeAgain.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_customize_again", mapOf("source" to "ApplySuccessfullyScreen"))

            FirebaseAnalyticsUtils.logClickEvent(this, "trigger_interstitial_ad", mapOf("screen" to "EmojiEditApplyScreen"))
            AdManager.showInterstitialAd(this,isFullscreenApplyEmojiEnabled,true) {
                FirebaseAnalyticsUtils.logClickEvent(this, "StatusBarCustomizeActivity", null)

                startActivity(Intent(this, StatusBarCustomizeActivity::class.java))
                finish()
                Log.e("Ads", "FullScreenTobeShoe")
            }
        }

        binding.actionClose.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_close_button", mapOf("source" to "ApplySuccessfullyScreen"))

           // if (preferences.shouldTriggerEveryThirdTime("interstitial_ad_count")) {
                FirebaseAnalyticsUtils.logClickEvent(this, "trigger_interstitial_ad", mapOf("screen" to "EmojiEditApplyScreen"))
                AdManager.showInterstitialAd(this,isFullscreenApplyEmojiEnabled,true) {
                    finish()
                    Log.e("Ads", "FullScreenTobeShoe")
                }
         //   }
        }

        nativeAdHelper = NativeEmojiHelper(
            context = this,
            adId = getNativeInsideId(), // Replace with your ad unit ID
            showAdRemoteFlag = isNativeApplyEmojiEnabled,  // From remote config or your logic
            isProUser = preferences.isProUser,        // Check from your user settings
            onAdLoaded = {
                Log.d("Ad", "Native ad loaded successfully")
            },
            onAdFailed = {
                Log.d("Ad", "Failed to load native ad")
            },
            adContainer = binding.nativeAdContainer   // Optional: Pass only if you want to show immediately
        )
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AdManager.showInterstitialAd(this@ApplySuccessfullyActivity, isFullscreenApplyEmojiEnabled,true) {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "ApplySuccessfullyScreen")
    }
    override fun onDestroy() {
        nativeAdHelper?.destroy()
        super.onDestroy()
    }
}