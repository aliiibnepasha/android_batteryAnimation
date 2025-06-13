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
import com.lowbyte.battery.animation.databinding.ActivityBatteryAnimationEditApplyBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class BatteryAnimationEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityBatteryAnimationEditApplyBinding
    private lateinit var preferences: AppPreferences
    private var position: Int = -1
    private lateinit var label: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBatteryAnimationEditApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

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
        Log.i("ITEMCLICK", "$position $label")

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
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", mapOf("screen" to "BatteryAnimationEditApplyScreen"))
            finish()
        }

        binding.buttonForApply.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_apply_animation", mapOf("label" to label))

            if (!preferences.isStatusBarEnabled) {
                Toast.makeText(
                    this,
                    getString(R.string.please_enable_battery_emoji_service),
                    Toast.LENGTH_LONG
                ).show()
            }

            if (preferences.shouldTriggerEveryThirdTime("interstitial_ad_count")) {
                AdManager.showInterstitialAd(this, true) {
                    Log.e("Ads", "FullScreenTobeShoe")
                }
            }

            preferences.statusLottieName = label
            sendBroadcast(Intent(BROADCAST_ACTION))

            Toast.makeText(
                this,
                getString(R.string.animation_applied_successfully),
                Toast.LENGTH_SHORT
            ).show()
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
}