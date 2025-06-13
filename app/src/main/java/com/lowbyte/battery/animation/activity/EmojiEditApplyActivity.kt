package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivityEmojiEditApplayBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class EmojiEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityEmojiEditApplayBinding
    private lateinit var preferences: AppPreferences
    private lateinit var drawable: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEmojiEditApplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        AdManager.loadInterstitialAd(this, getFullscreenId())

        FirebaseAnalyticsUtils.logScreenView(this, "EmojiEditApplyScreen")
        FirebaseAnalyticsUtils.startScreenTimer("EmojiEditApplyScreen")

        val position = intent.getIntExtra(EXTRA_POSITION, -1)
        drawable = intent.getStringExtra(EXTRA_LABEL) ?: getString(R.string.wifi)

        Log.i("ITEMCLICK", "$position $drawable")
        val resId = resources.getIdentifier(drawable, "drawable", packageName)
        binding.previewEditEmoji.setImageResource(if (resId != 0) resId else R.drawable.emoji_1)

        FirebaseAnalyticsUtils.logClickEvent(this, "emoji_selected", mapOf("drawable" to drawable, "position" to position.toString()))

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", mapOf("screen" to "EmojiEditApplyScreen"))
            finish()
        }

        binding.ibNextButton.setOnClickListener {
            preferences.setInt("percentageColor", Color.BLACK)
            binding.batteryEmojiPercentageSeekbarSize.progress = 12
            binding.batteryEmojiSeekbarSize.progress = 24
            FirebaseAnalyticsUtils.logClickEvent(this, "click_reset_sizes", null)
        }

        binding.enableShowBatteryPercentage.isChecked = preferences.showBatteryPercent
        binding.batteryEmojiSeekbarSize.progress = preferences.getIconSize("batteryIcon", 25)
        binding.batteryEmojiPercentageSeekbarSize.progress = preferences.getIconSize("percentageSize", 25)

        binding.batteryEmojiSeekbarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.setIconSize("batteryIconSize", progress)
                sendBroadcast(Intent(BROADCAST_ACTION))
                binding.batteryEmojiLabel.text = getString(
                    R.string.size_dp,
                    getString(R.string.view_all_battery_emoji),
                    progress
                )
                FirebaseAnalyticsUtils.logClickEvent(this@EmojiEditApplyActivity, "change_battery_icon_size", mapOf("size" to progress.toString()))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!preferences.isStatusBarEnabled) {
                    Toast.makeText(this@EmojiEditApplyActivity, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.batteryEmojiPercentageSeekbarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.setIconSize("percentageSize", progress)
                sendBroadcast(Intent(BROADCAST_ACTION))
                binding.batteryPercentageEmojiLabel.text = getString(
                    R.string.size_dp,
                    getString(R.string.percentage_size),
                    progress
                )
                FirebaseAnalyticsUtils.logClickEvent(this@EmojiEditApplyActivity, "change_percentage_size", mapOf("size" to progress.toString()))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.enableShowBatteryPercentage.isChecked) {
                    Toast.makeText(this@EmojiEditApplyActivity, getString(R.string.please_turn_on_battery_percentage), Toast.LENGTH_SHORT).show()
                }
                if (!preferences.isStatusBarEnabled) {
                    Toast.makeText(this@EmojiEditApplyActivity, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.viewPercentageColor.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.apply), ColorEnvelopeListener { envelope, _ ->
                    colorOfIcon(envelope)
                    FirebaseAnalyticsUtils.logClickEvent(this, "color_picker_applied", mapOf("color" to envelope.hexCode))
                })
                .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show()
        }

        binding.enableShowBatteryPercentage.setOnCheckedChangeListener { _, isChecked ->
            preferences.showBatteryPercent = isChecked
            sendBroadcast(Intent(BROADCAST_ACTION))
            FirebaseAnalyticsUtils.logClickEvent(this, "toggle_percentage_display", mapOf("enabled" to isChecked.toString()))
        }

        binding.btnNext.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_apply_emoji", mapOf("drawable" to drawable))
            preferences.batteryIconName = drawable
            sendBroadcast(Intent(BROADCAST_ACTION))
            startActivity(Intent(this, ApplySuccessfullyActivity::class.java))
            finish()

            if (preferences.shouldTriggerEveryThirdTime("interstitial_ad_count")) {
                FirebaseAnalyticsUtils.logClickEvent(this, "trigger_interstitial_ad", mapOf("screen" to "EmojiEditApplyScreen"))
                AdManager.showInterstitialAd(this,true) {
                    Log.e("Ads", "FullScreenTobeShoe")
                }
            }


        }
    }

    private fun colorOfIcon(envelope: ColorEnvelope) {
        if (!binding.enableShowBatteryPercentage.isChecked) {
            Toast.makeText(this, getString(R.string.please_turn_on_battery_percentage), Toast.LENGTH_SHORT).show()
        }
        if (!preferences.isStatusBarEnabled) {
            Toast.makeText(this, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
        }
        preferences.setInt("percentageColor", envelope.color)
        sendBroadcast(Intent(BROADCAST_ACTION))
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "EmojiEditApplyScreen")
    }
}