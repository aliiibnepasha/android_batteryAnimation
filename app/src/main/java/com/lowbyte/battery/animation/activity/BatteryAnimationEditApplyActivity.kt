package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeAnimationHelper
import com.lowbyte.battery.animation.ads.NativeWidgetHelper
import com.lowbyte.battery.animation.databinding.ActivityBatteryAnimationEditApplyBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeInsideId
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyAnimEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyAnimEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class BatteryAnimationEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityBatteryAnimationEditApplyBinding
    private lateinit var preferences: AppPreferences
    private var position: Int = -1
    private lateinit var label: String

    private var nativeAdHelper: NativeAnimationHelper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatteryAnimationEditApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        AdManager.loadInterstitialAd(this, getFullscreenId(),isFullscreenApplyAnimEnabled)

        // Track screen view & time
        FirebaseAnalyticsUtils.logScreenView(this, "BatteryAnimationEditApplyScreen")
        FirebaseAnalyticsUtils.startScreenTimer("BatteryAnimationEditApplyScreen")

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get intent extras
        position = intent.getIntExtra(EXTRA_POSITION, -1)
        label = intent.getStringExtra(EXTRA_LABEL) ?: getString(R.string.wifi)
        Log.i("ITEM_CLICK", "$position $label")

        // Log selected animation
        FirebaseAnalyticsUtils.logClickEvent(
            this,
            "animation_selected",
            mapOf("position" to position.toString(), "label" to label)
        )

        val resId = resources.getIdentifier(label, "raw", packageName)
        if (resId != 0) {
            binding.previewLottiAnimation.setAnimation(resId)
        } else {
            Log.e("AnimationAdapter", "Lottie resource not found for name: $label")
            binding.previewLottiAnimation.cancelAnimation()
        }

        setupClickListeners()

        nativeAdHelper = NativeAnimationHelper(
            context = this,
            adId = getNativeInsideId(), // Replace with your ad unit ID
            showAdRemoteFlag = isNativeApplyAnimEnabled,  // From remote config or your logic
            isProUser = preferences.isProUser,        // Check from your user settings
            onAdLoaded = {
                Log.d("Ad", "Native ad loaded successfully")
            },
            onAdFailed = {
                Log.d("Ad", "Failed to load native ad")
            },
            adContainer = binding.nativeAdContainer   // Optional: Pass only if you want to show immediately
        )
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", mapOf("screen" to "BatteryAnimationEditApplyScreen"))
            finish()
        }

        binding.buttonForAnimApply.setOnClickListener {
            if (preferences.statusLottieName==""){
                Toast.makeText(
                    this,
                    getString(R.string.please_enable_animation),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            FirebaseAnalyticsUtils.logClickEvent(this, "click_apply_animation", mapOf("label" to label))

            if (!preferences.isStatusBarEnabled) {
                Toast.makeText(
                    this,
                    getString(R.string.please_enable_battery_emoji_service),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            preferences.statusLottieName = label
            sendBroadcast(Intent(BROADCAST_ACTION))

            Toast.makeText(
                this,
                getString(R.string.animation_applied_successfully),
                Toast.LENGTH_SHORT
            ).show()

            if (preferences.shouldTriggerEveryThirdTime("interstitial_ad_count")) {
                AdManager.showInterstitialAd(this, true,true) {
                    Log.e("Ads", "FullScreenTobeShoe")
                }
            }


        }

        binding.buttonHome.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_home_button", mapOf("screen" to "BatteryAnimationEditApplyScreen"))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "BatteryAnimationEditApplyScreen")
    }

    override fun onDestroy() {
        nativeAdHelper?.destroy()
        super.onDestroy()
    }
}