package com.lowbyte.battery.animation.activity

import GestureBottomSheetFragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.ActionScrollItem
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeBannerSizeHelper
import com.lowbyte.battery.animation.databinding.ActivityStatusBarGestureBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeCustomizeId
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class StatusBarGestureActivity : BaseActivity() {

    private var _binding: ActivityStatusBarGestureBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: AppPreferences

    private lateinit var singleTapActionText: TextView
    private lateinit var longTapActionText: TextView
    private lateinit var swipeLeftToRightActionText: TextView
    private lateinit var swipeRightToLeftActionText: TextView
    private var nativeHelper: NativeBannerSizeHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  enableEdgeToEdge()
        _binding = ActivityStatusBarGestureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        AdManager.loadInterstitialAd(this, getFullscreenId())

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AdManager.showInterstitialAd(this@StatusBarGestureActivity, true) {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        FirebaseAnalyticsUtils.logScreenView(this, "StatusBarGestureScreen")
        FirebaseAnalyticsUtils.startScreenTimer("StatusBarGestureScreen")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        nativeHelper = NativeBannerSizeHelper(
            context = this,
            adId = getNativeCustomizeId(), // Replace with your real AdMob ID
            showAdRemoteFlag = true, // Or get from remote config
            isProUser = preferences.isProUser,       // Or from preferences
            adContainer = binding.nativeAdContainer,
            onAdLoaded = { Log.d("AD", "Banner Ad loaded!") },
            onAdFailed = { Log.d("AD", "Banner Ad failed!") }
        )



        singleTapActionText = binding.statusBarSingleTapAction
        longTapActionText = binding.statusBarLongAction
        swipeLeftToRightActionText = binding.swipeLeftToRightAction
        swipeRightToLeftActionText = binding.actionOnSwipeRightToLeft

        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", null)
            finish()
        }

        // Load saved actions
        singleTapActionText.text = getLocalizedStringFromPrefKey(this, "gestureAction", R.string.action_do_nothing)
        longTapActionText.text = getLocalizedStringFromPrefKey(this, "longPressAction", R.string.action_do_nothing)
        swipeLeftToRightActionText.text = getLocalizedStringFromPrefKey(this, "swipeLeftToRightAction", R.string.action_do_nothing)
        swipeRightToLeftActionText.text = getLocalizedStringFromPrefKey(this, "swipeRightToLeftAction", R.string.action_do_nothing)

        binding.switchVibrateFeedback.isChecked = preferences.isVibrateMode
        binding.switchVibrateFeedback.setOnCheckedChangeListener { _, isChecked ->
            preferences.isVibrateMode = isChecked
            FirebaseAnalyticsUtils.logClickEvent(this, "toggle_vibration_feedback", mapOf("enabled" to isChecked.toString()))
            if (!preferences.isStatusBarEnabled && isChecked) {
                Toast.makeText(this, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
            }
        }

        binding.gestureSwitchEnable.isChecked = preferences.isGestureMode
        binding.gestureSwitchEnable.setOnCheckedChangeListener { _, isChecked ->
            preferences.isGestureMode = isChecked
            FirebaseAnalyticsUtils.logClickEvent(this, "toggle_gesture_mode", mapOf("enabled" to isChecked.toString()))
            if (!preferences.isStatusBarEnabled && isChecked) {
                Toast.makeText(this, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
            }
        }

        binding.viewSingleTap.setOnClickListener {
            if (!preferences.isGestureMode){
                Toast.makeText(this, getString(R.string.please_enable_gesture_mode), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            openGestureSheet("single_tap", singleTapActionText, "gestureAction")
        }

        binding.viewLongTap.setOnClickListener {
            if (!preferences.isGestureMode){
                Toast.makeText(this, getString(R.string.please_enable_gesture_mode), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            openGestureSheet("long_press", longTapActionText, "longPressAction")
        }

        binding.swipeLeftToRightView.setOnClickListener {
            if (!preferences.isGestureMode){
                Toast.makeText(this, getString(R.string.please_enable_gesture_mode), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            openGestureSheet("swipe_left_to_right", swipeLeftToRightActionText, "swipeLeftToRightAction")
        }

        binding.swipeRightToLeftView.setOnClickListener {
            if (!preferences.isGestureMode){
                Toast.makeText(this, getString(R.string.please_enable_gesture_mode), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            openGestureSheet("swipe_right_to_left", swipeRightToLeftActionText, "swipeRightToLeftAction")
        }
    }

    private fun openGestureSheet(gestureKey: String, targetTextView: TextView, prefKey: String) {
        FirebaseAnalyticsUtils.logClickEvent(this, "gesture_bottomsheet_opened", mapOf("gesture" to gestureKey))

        val items = listOf(
            ActionScrollItem(getString(R.string.action_open_control_centre), "action_open_control_centre"),
            ActionScrollItem(getString(R.string.action_open_notifications), "action_open_notifications"),
            ActionScrollItem(getString(R.string.action_power_options), "action_power_options"),
            ActionScrollItem(getString(R.string.action_do_nothing), "action_do_nothing"),
            ActionScrollItem(getString(R.string.action_back_action), "action_back_action"),
            ActionScrollItem(getString(R.string.action_home_action), "action_home_action"),
            ActionScrollItem(getString(R.string.action_recent_action), "action_recent_action"),
            ActionScrollItem(getString(R.string.action_take_screenshot), "action_take_screenshot"),
            ActionScrollItem(getString(R.string.action_lock_screen), "action_lock_screen"),
        )

        GestureBottomSheetFragment(getStringResourceTitle(gestureKey), targetTextView.text.toString(), items) { selected ->
            targetTextView.text = selected.label
            preferences.setString(prefKey, selected.actionName)
            FirebaseAnalyticsUtils.logClickEvent(
                this,
                "gesture_action_selected",
                mapOf("gesture" to gestureKey, "action" to selected.actionName)
            )
        }.show(supportFragmentManager, "GestureBottomSheet")
    }

    private fun getStringResourceTitle(gestureKey: String): String {
        return when (gestureKey) {
            "single_tap" -> getString(R.string.single_tap)
            "long_press" -> getString(R.string.long_press)
            "swipe_left_to_right" -> getString(R.string.swipe_left_to_right)
            "swipe_right_to_left" -> getString(R.string.swipe_right_to_left)
            else -> gestureKey
        }
    }

    private fun getLocalizedStringFromPrefKey(context: Context, key: String, defaultResId: Int): String {
        val savedKey = preferences.getString(key, "")
        savedKey?.let {
            val resId = context.resources.getIdentifier(it, "string", context.packageName)
            if (resId != 0) return context.getString(resId)
        }
        return context.getString(defaultResId)
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "StatusBarGestureScreen")
    }

    override fun onDestroy() {
        nativeHelper?.destroy()
        nativeHelper = null
        super.onDestroy()
    }
}