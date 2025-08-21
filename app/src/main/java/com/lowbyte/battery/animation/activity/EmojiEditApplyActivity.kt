package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivityEmojiEditApplayBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.dialoge.RewardedDialogHandler
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isRewardedEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class EmojiEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityEmojiEditApplayBinding
    private lateinit var preferences: AppPreferences
    private lateinit var drawable: String

    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet
    private  var isRewarded: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmojiEditApplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        AdManager.loadInterstitialAd(this, getFullscreenId(),isFullscreenApplyEmojiEnabled)
        sheet = AccessibilityPermissionBottomSheet(
            onAllowClicked = {
                FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_granted", null)
               // AllowAccessibilityDialogFragment().show(supportFragmentManager, "AllowAccessibilityDialog")

                startActivity(Intent(this, AllowAccessibilityActivity::class.java))
                //  startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onCancelClicked = {
                FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_denied", null)
                preferences.isStatusBarEnabled = false
            }, onDismissListener = {
//                if (!isAccessibilityServiceEnabled()) {
//                    preferences.isStatusBarEnabled = false
//                }

            }
        )


        FirebaseAnalyticsUtils.logScreenView(this, "EmojiEditApplyScreen")
        FirebaseAnalyticsUtils.startScreenTimer("EmojiEditApplyScreen")

        val position = intent.getIntExtra(EXTRA_POSITION, -1)
        isRewarded = intent.getBooleanExtra("RewardEarned", false)
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
            AdManager.showInterstitialAd(this,isFullscreenApplyEmojiEnabled,true) {
                finish()
                Log.e("Ads", "FullScreenTobeShoe")
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AdManager.showInterstitialAd(this@EmojiEditApplyActivity, isFullscreenApplyEmojiEnabled,true) {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        binding.ibNextButton.setOnClickListener {
            preferences.setInt("percentageColor", Color.BLACK)
            binding.batteryEmojiPercentageSeekbarSize.progress = 12
            binding.batteryEmojiSeekbarSize.progress = 24
            FirebaseAnalyticsUtils.logClickEvent(this, "click_reset_sizes", null)
            Toast.makeText(this, getString(R.string.restore_successfully), Toast.LENGTH_SHORT).show()
            sendBroadcast(Intent(BROADCAST_ACTION))
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
              //  FirebaseAnalyticsUtils.logClickEvent(this@EmojiEditApplyActivity, "change_battery_icon_size", mapOf("size" to progress.toString()))
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
               // FirebaseAnalyticsUtils.logClickEvent(this@EmojiEditApplyActivity, "change_percentage_size", mapOf("size" to progress.toString()))
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
                .setTitle(getString(R.string.percentage_color))
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
            checkAccessibilityPermission()

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
    private fun checkAccessibilityPermission() {
        FirebaseAnalyticsUtils.logClickEvent(this, "click_apply_emoji", mapOf("drawable" to drawable))
        if (!isAccessibilityServiceEnabled()) {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_prompt_shown", null)

            val existing = supportFragmentManager.findFragmentByTag("AccessibilityPermission")
            if (existing == null || !existing.isAdded) {
                sheet.show(supportFragmentManager, "AccessibilityPermission")
            } else {
                Log.d("Accessibility", "AccessibilityPermissionBottomSheet already shown")
            }
        } else {

            if (isRewarded && !preferences.isProUser && preferences.getBoolean(
                    "RewardEarned",
                    false
                ) == false
            ) {
                RewardedDialogHandler.showRewardedDialog(
                    context = this,
                    preferences = preferences,
                    isSkipShow = false,
                    isRewardedEnabled = isRewardedEnabled,
                    onCompleted = {
                        preferences.isStatusBarEnabled = true
                        preferences.batteryIconName = drawable
                        startActivity(Intent(this, ApplySuccessfullyActivity::class.java))
                        sendBroadcast(Intent(BROADCAST_ACTION))
                        finish()

                    }
                )
            } else {
                preferences.isStatusBarEnabled = true
                preferences.batteryIconName = drawable
                FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_granted", null)
                startActivity(Intent(this, ApplySuccessfullyActivity::class.java))
                sendBroadcast(Intent(BROADCAST_ACTION))
                finish()
            }


        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = "${packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabledServices.split(':').any { it.equals(expectedComponentName, ignoreCase = true) }
    }


    override fun onResume() {

        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "EmojiEditApplyScreen")
    }
}