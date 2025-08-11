package com.lowbyte.battery.animation.activity

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeWidgetHelper
import com.lowbyte.battery.animation.broadcastReciver.BatteryWidgetProvider
import com.lowbyte.battery.animation.databinding.ActivityBatteryWidgetEditApplyBinding
import com.lowbyte.battery.animation.service.BatteryWidgetForegroundService
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeInsideId
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyAnimEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyWidgetEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyWidgetEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class BatteryWidgetEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityBatteryWidgetEditApplyBinding
    private lateinit var preferences: AppPreferences
    private var position: Int = -1
    private lateinit var label: String
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var isNewWidget: Boolean = false
  //  private var isUserActionPerformedWidget: Boolean = false
    private var isScreenFirstOpen: Boolean = true
    private var nativeAdHelper: NativeWidgetHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatteryWidgetEditApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)



        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
               // if (isUserActionPerformedWidget) {
                    Log.d("onBackPredd","onBackPredd with Ad")
                    AdManager.showInterstitialAd(this@BatteryWidgetEditApplyActivity, isFullscreenApplyWidgetEnabled, true) {
                        finish()
                    }
//                } else {
//                    Log.d("onBackPredd","onBackPredd with  no Ad")
//                    finish()
//                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        FirebaseAnalyticsUtils.logScreenView(this, "BatteryWidgetEditApplyScreen")
        FirebaseAnalyticsUtils.startScreenTimer("BatteryWidgetEditApplyScreen")

        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        isNewWidget = appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID

      //  enableEdgeToEdge()


        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AdManager.loadInterstitialAd(this, getFullscreenId(),isFullscreenApplyWidgetEnabled)


        position = intent.getIntExtra(EXTRA_POSITION, -1)
        label = intent.getStringExtra(EXTRA_LABEL) ?: getString(R.string.wifi)

        Log.e("BatteryWidgetProvider", "Widget ID: $appWidgetId, Position: $position, Label: $label, IsNew: $isNewWidget")

        if (!isNewWidget) {
            val savedIcon = preferences.getWidgetIcon(appWidgetId)
            if (savedIcon.isNotEmpty()) {
                label = savedIcon
                Log.e("BatteryWidgetProvider", "Loaded saved icon for widget $appWidgetId: $savedIcon")
            }
        }

        loadAndDisplayImage()
        setupClickListeners()

        nativeAdHelper = NativeWidgetHelper(
            context = this,
            adId = getNativeInsideId(), // Replace with your ad unit ID
            showAdRemoteFlag = isNativeApplyWidgetEnabled,  // From remote config or your logic
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

    private fun loadAndDisplayImage() {
        val resId = resources.getIdentifier(label, "drawable", packageName)
        Log.e("BatteryWidgetEditApplyActivity", "Loading image for label: $label, resId: $resId")

        FirebaseAnalyticsUtils.logClickEvent(this, "widget_preview_loaded", mapOf("label" to label))

        if (resId == 0) {
            Log.e("BatteryWidgetEditApplyActivity", "Failed to find drawable resource for label: $label")
        }
        binding.previewWidgetView.setImageResource(if (resId != 0) resId else R.drawable.emoji_4)
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", mapOf("screen" to "BatteryWidgetEditApplyScreen"))
            if (isNewWidget) {
                setResult(RESULT_CANCELED)
            }
          //  if (isUserActionPerformedWidget) {
                AdManager.showInterstitialAd(this, isFullscreenApplyWidgetEnabled, true) {
                    finish()
                }
//            } else {
//                finish()
//            }
        }

        binding.buttonForApply.setOnClickListener {
          //  isUserActionPerformedWidget = true
            if (preferences.widgetCount>=10){
                Toast.makeText(this, getString(R.string.widget_limit_reached), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            preferences.setString("tempImageForWidget",label)
            FirebaseAnalyticsUtils.logClickEvent(
                this,
                "click_apply_widget",
                mapOf(
                    "label" to label,
                    "widget_id" to appWidgetId.toString(),
                    "is_new_widget" to isNewWidget.toString()
                )
            )

            val appWidgetManager = AppWidgetManager.getInstance(this)
            val widgetProvider = ComponentName(this, BatteryWidgetProvider::class.java)

            /*    if (preferences.shouldTriggerEveryThirdTime("interstitial_ad_count")) {
                    AdManager.showInterstitialAd(this, isFullscreenApplyWidgetEnabled,true,) {
                        Log.e("Ads", "FullScreenTobeShoe")
                        if (!(SDK_INT < VERSION_CODES.O || !appWidgetManager.isRequestPinAppWidgetSupported)) {

                            val options = Bundle().apply {
                                putString("WIDGET_ICON", label)
                            }

                            val successIntent = Intent(this, BatteryWidgetProvider::class.java).apply {
                                action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                putExtra("WIDGET_ICON", label)
                            }

                            val successCallback = PendingIntent.getBroadcast(this, 0, successIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                            appWidgetManager.requestPinAppWidget(widgetProvider, options, successCallback)
                            Log.e("BatteryWidgetProvider", "Activity ------------- Loading image for label 2: $label")
                        }
                        else {
                            Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show()
                        }
                        val serviceIntent = Intent(this, BatteryWidgetForegroundService::class.java)
                        ContextCompat.startForegroundService(this,serviceIntent)
                        Toast.makeText(this, getString(R.string.widget_applied_successfully), Toast.LENGTH_SHORT).show()

                    }
                } else{*/
                Log.e("Ads", "FullScreenTobeShoe")
                if (!(SDK_INT < VERSION_CODES.O || !appWidgetManager.isRequestPinAppWidgetSupported)) {

                    val options = Bundle().apply {
                        putString("WIDGET_ICON", label)
                    }

                    val successIntent = Intent(this, BatteryWidgetProvider::class.java).apply {
                        action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                        putExtra("WIDGET_ICON", label)
                    }

                    val successCallback = PendingIntent.getBroadcast(this, 0, successIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    appWidgetManager.requestPinAppWidget(widgetProvider, options, successCallback)
                    Log.e("BatteryWidgetProvider", "Activity ------------- Loading image for label 2: $label")
                    val serviceIntent = Intent(this, BatteryWidgetForegroundService::class.java).setAction(BatteryWidgetForegroundService.ACTION_START_SERVICE)
                    ContextCompat.startForegroundService(this, serviceIntent)
                    Toast.makeText(
                        this,
                        getString(R.string.widget_applied_successfully),
                        Toast.LENGTH_SHORT
                    ).show()

                }
                else {
                    Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show()
                }

            //   }


        }

        binding.buttonSetAsEmoji.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_set_as_emoji", mapOf("label" to label))
           // if (isUserActionPerformedWidget) {
                AdManager.showInterstitialAd(this, isFullscreenApplyWidgetEnabled, true) {
                    finish()
                }
//            } else {
//                finish()
//            }

        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "BatteryWidgetEditApplyScreen")
    }

    override fun onDestroy() {
        nativeAdHelper?.destroy()
        isScreenFirstOpen =  true
        super.onDestroy()
    }
}