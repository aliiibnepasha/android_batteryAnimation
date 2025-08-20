package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeAnimationHelper
import com.lowbyte.battery.animation.databinding.ActivityBatteryAnimationEditApplyBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.utils.AllowAccessibilityDialogFragment
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
    private  var isRewarded: Boolean = false

    private lateinit var binding: ActivityBatteryAnimationEditApplyBinding
    private lateinit var preferences: AppPreferences
    private var position: Int = -1
    private lateinit var label: String
//    private var isUserActionPerformed: Boolean = false
    private lateinit var accessibilityPermissionBottomSheet: AccessibilityPermissionBottomSheet // Declare the sheet

    private var nativeAdHelper: NativeAnimationHelper? = null
   // val accessibilityDialogFragment = AllowAccessibilityDialogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatteryAnimationEditApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        AdManager.loadInterstitialAd(this, getFullscreenId(),isFullscreenApplyAnimEnabled)
        accessibilityPermissionBottomSheet = AccessibilityPermissionBottomSheet(onAllowClicked = {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_granted", null)

            startActivity(Intent(this, AllowAccessibilityActivity::class.java))
        }, onCancelClicked = {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_denied", null)
            preferences.isStatusBarEnabled = false
        }, onDismissListener = {

        })

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
        isRewarded = intent.getBooleanExtra("RewardEarned", false)

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

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    AdManager.showInterstitialAd(this@BatteryAnimationEditApplyActivity, isFullscreenApplyAnimEnabled, true) {
                        finish()
                    }

            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", mapOf("screen" to "BatteryAnimationEditApplyScreen"))
          //  if (isUserActionPerformed) {
                AdManager.showInterstitialAd(this, isFullscreenApplyAnimEnabled, true) {
                    finish()
                }
//            } else {
//                finish()
//            }
        }

        binding.buttonForAnimApply.setOnClickListener {
          //  isUserActionPerformed = true
            preferences.isStatusBarEnabled = true

            FirebaseAnalyticsUtils.logClickEvent(this, "click_apply_animation", mapOf("label" to label))
            preferences.statusLottieName = label
            checkAccessibilityPermission()
        }

        binding.buttonHome.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_home_button", mapOf("screen" to "BatteryAnimationEditApplyScreen"))
                AdManager.showInterstitialAd(this, isFullscreenApplyAnimEnabled, true) {
                    finish()
                }
        }
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_prompt_shown", null)
//            if (BuildConfig.DEBUG) {
//                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
//            } else {
                val existing = supportFragmentManager.findFragmentByTag("AccessibilityPermission")
                if (existing == null || !existing.isAdded) {
                    accessibilityPermissionBottomSheet.show(supportFragmentManager, "AccessibilityPermission")
                } else {
                    Log.d("Accessibility", "AccessibilityPermissionBottomSheet already shown")
          //      }
            }
        } else {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_permission_granted", null)
            sendBroadcast(Intent(BROADCAST_ACTION))
            Toast.makeText(
                this,
                getString(R.string.animation_applied_successfully),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName =
            "${packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(':')
            .any { it.equals(expectedComponentName, ignoreCase = true) }
    }



//    private val accessibilityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//
//       // dialog.
//    }


    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "BatteryAnimationEditApplyScreen")
    }

    override fun onDestroy() {
        nativeAdHelper?.destroy()
        super.onDestroy()
    }
}