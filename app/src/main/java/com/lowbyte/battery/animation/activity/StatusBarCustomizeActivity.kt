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
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.BuildConfig
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.CustomIconGridAdapter
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeBannerSizeHelper
import com.lowbyte.battery.animation.databinding.ActivityStatusBarCustommizeBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.model.CustomIconGridItem
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION_DYNAMIC
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenHome2Id
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeCustomizeId
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenStatusEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeStatusEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class StatusBarCustomizeActivity : BaseActivity() {

    private var _binding: ActivityStatusBarCustommizeBinding? = null
    private val binding get() = _binding!!
    private var nativeHelper: NativeBannerSizeHelper? = null

    private lateinit var preferences: AppPreferences
    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    //    enableEdgeToEdge()
        _binding = ActivityStatusBarCustommizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        AdManager.loadInterstitialAd(this,getFullscreenHome2Id(),isFullscreenStatusEnabled)

        sheet = AccessibilityPermissionBottomSheet(
            onAllowClicked = {
                FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_granted", null)
                startActivity(Intent(this, AllowAccessibilityActivity::class.java))
                //  startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onCancelClicked = {
                FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_denied", null)
                preferences.isStatusBarEnabled = false
                binding.switchEnableBatteryEmojiCustom.isChecked = false
            }, onDismissListener = {
                if (!isAccessibilityServiceEnabled()) {
                    preferences.isStatusBarEnabled = false
                    binding.switchEnableBatteryEmojiCustom.isChecked = false
                }

            }
        )

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AdManager.showInterstitialAd(this@StatusBarCustomizeActivity, isFullscreenStatusEnabled,true) {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)


        nativeHelper = NativeBannerSizeHelper(
            context = this,
            adId = getNativeCustomizeId(), // Replace with your real AdMob ID
            showAdRemoteFlag = isNativeStatusEnabled, // Or get from remote config
            isProUser = preferences.isProUser,       // Or from preferences
            adContainer = binding.nativeAdContainer,
            onAdLoaded = { Log.d("AD", "Banner Ad loaded!") },
            onAdFailed = { Log.d("AD", "Banner Ad failed!") }
        )


        FirebaseAnalyticsUtils.logScreenView(this, "StatusBarCustomizeScreen")
        FirebaseAnalyticsUtils.startScreenTimer("StatusBarCustomizeScreen")

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", null)
            AdManager.showInterstitialAd(this@StatusBarCustomizeActivity, isFullscreenStatusEnabled,true) {
                finish()
            }
        }

        binding.restoreSetting.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_restore_defaults", null)
            preferences.setInt("tint_0", Color.BLACK)
            preferences.setInt("tint_1", Color.BLACK)
            preferences.setInt("tint_2", Color.BLACK)
            preferences.setInt("tint_3", Color.BLACK)
            preferences.setInt("tint_4", Color.BLACK)
            preferences.setInt("tint_5", Color.BLACK)
            preferences.setIconSize("size_0", 24)
            preferences.setIconSize("size_1", 24)
            preferences.setIconSize("size_2", 24)
            preferences.setIconSize("size_3", 24)
            preferences.setIconSize("size_4", 24)
            preferences.setIconSize("size_5", 12)
            preferences.statusBarBgColor = Color.LTGRAY
            binding.statusBarHeightSeekbar.progress = 35
            binding.leftMarginSeekBar.progress = 10
            binding.rightMarginSeekBar.progress = 10
            Toast.makeText(this, getString(R.string.restore_successfully), Toast.LENGTH_SHORT).show()
            sendBroadcast(Intent(BROADCAST_ACTION))
        }

        binding.switchEnableBatteryEmojiCustom.isChecked = preferences.isStatusBarEnabled && isAccessibilityServiceEnabled()
        binding.switchEnableBatteryEmojiCustom.setOnCheckedChangeListener { _, isChecked ->
            preferences.isStatusBarEnabled = isChecked
            FirebaseAnalyticsUtils.logClickEvent(this, "toggle_battery_emoji", mapOf("enabled" to isChecked.toString()))
            if (isChecked) {
                checkAccessibilityPermission()
            } else {
                sendBroadcast(Intent(BROADCAST_ACTION))
            }
            sendBroadcast(Intent(BROADCAST_ACTION_DYNAMIC))

        }


        binding.statusBarHeightSeekbar.max = 50
        binding.statusBarHeightSeekbar.progress = preferences.statusBarHeight
        binding.statusBarHeightSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val safeProgress = progress.coerceIn(0, 50)
                preferences.statusBarHeight = safeProgress
                binding.statusBarHeight.text = getString(R.string.height_dp, safeProgress)
               // FirebaseAnalyticsUtils.logClickEvent(this@StatusBarCustomizeActivity, "change_statusbar_height", mapOf("value" to safeProgress.toString()))
                sendBroadcast(Intent(BROADCAST_ACTION))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableBatteryEmojiCustom.isChecked) {
                    Toast.makeText(this@StatusBarCustomizeActivity, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.leftMarginSeekBar.progress = preferences.statusBarMarginLeft
        binding.leftMarginSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.statusBarMarginLeft = progress
                binding.leftMarginLabel.text = getString(R.string.left_margin_dp, progress)
             //   FirebaseAnalyticsUtils.logClickEvent(this@StatusBarCustomizeActivity, "change_left_margin", mapOf("value" to progress.toString()))
                sendBroadcast(Intent(BROADCAST_ACTION))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableBatteryEmojiCustom.isChecked) {
                    Toast.makeText(this@StatusBarCustomizeActivity, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.rightMarginSeekBar.progress = preferences.statusBarMarginRight
        binding.rightMarginSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.statusBarMarginRight = progress
                binding.rightMarginLabel.text = getString(R.string.right_margin_dp, progress)
              //  FirebaseAnalyticsUtils.logClickEvent(this@StatusBarCustomizeActivity, "change_right_margin", mapOf("value" to progress.toString()))
                sendBroadcast(Intent(BROADCAST_ACTION))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableBatteryEmojiCustom.isChecked) {
                    Toast.makeText(this@StatusBarCustomizeActivity, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.customizeStatusBarBgColor.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.status_bar))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.apply), ColorEnvelopeListener { envelope, _ ->
                    preferences.statusBarBgColor = envelope.color
                    FirebaseAnalyticsUtils.logClickEvent(this, "pick_statusbar_color", mapOf("color" to envelope.hexCode))
                    sendBroadcast(Intent(BROADCAST_ACTION))
                })
                .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show()
        }

        val items = arrayListOf(
            CustomIconGridItem(R.drawable.ic_signal_wifi, getString(R.string.wifi)),
            CustomIconGridItem(R.drawable.ic_signal_date, getString(R.string.data)),
            CustomIconGridItem(R.drawable.ic_signal_mobile, getString(R.string.signals)),
            CustomIconGridItem(R.drawable.ic_airplan_mod, getString(R.string.airplane)),
            CustomIconGridItem(R.drawable.ic_signal_hotspot, getString(R.string.hotspot)),
            CustomIconGridItem(R.drawable.ic_time_date, getString(R.string.time))
        )

        val adapter = CustomIconGridAdapter(items) { position, label ->
            FirebaseAnalyticsUtils.logClickEvent(this, "click_icon_item", mapOf("position" to position.toString(), "label" to label))
            FirebaseAnalyticsUtils.logClickEvent(this, "StatusBarIconSettingsActivity", mapOf("position" to position.toString(), "label" to label))
            val intent = Intent(this, StatusBarIconSettingsActivity::class.java)
            intent.putExtra(EXTRA_POSITION, position)
            intent.putExtra(EXTRA_LABEL, label)
            startActivity(intent)
        }

        binding.recyclerViewCustomIcon.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerViewCustomIcon.adapter = adapter
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_prompt_shown", null)
//            if (BuildConfig.DEBUG){
//                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
//            }else{
                val existing = supportFragmentManager.findFragmentByTag("AccessibilityPermission")
                if (existing == null || !existing.isAdded) {
                    sheet.show(supportFragmentManager, "AccessibilityPermission")
                } else {
                    Log.d("Accessibility", "AccessibilityPermissionBottomSheet already shown")
                }
         //   }
        } else {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_granted", null)
            sendBroadcast(Intent(BROADCAST_ACTION))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = "${packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabledServices.split(':').any { it.equals(expectedComponentName, ignoreCase = true) }
    }


    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "StatusBarCustomizeScreen")
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}