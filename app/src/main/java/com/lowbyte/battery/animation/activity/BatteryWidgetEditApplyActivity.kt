package com.lowbyte.battery.animation.activity

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeLanguageHelper
import com.lowbyte.battery.animation.ads.NativeWidgetHelper
import com.lowbyte.battery.animation.broadcastReciver.BatteryLevelReceiver
import com.lowbyte.battery.animation.broadcastReciver.BatteryWidgetProvider
import com.lowbyte.battery.animation.databinding.ActivityBatteryWidgetEditApplyBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeInsideId
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class BatteryWidgetEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityBatteryWidgetEditApplyBinding
    private lateinit var preferences: AppPreferences
    private var position: Int = -1
    private lateinit var label: String
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var isNewWidget: Boolean = false
    private var isScreenFirstOpen: Boolean = true
    private var nativeAdHelper: NativeWidgetHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences.getInstance(this)

        FirebaseAnalyticsUtils.logScreenView(this, "BatteryWidgetEditApplyScreen")
        FirebaseAnalyticsUtils.startScreenTimer("BatteryWidgetEditApplyScreen")

        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        isNewWidget = appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID

        enableEdgeToEdge()
        binding = ActivityBatteryWidgetEditApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        AdManager.loadInterstitialAd(this, getFullscreenId())


        position = intent.getIntExtra(EXTRA_POSITION, -1)
        label = intent.getStringExtra(EXTRA_LABEL) ?: getString(R.string.wifi)

        Log.e("BatteryWidgetEditApplyActivity", "Widget ID: $appWidgetId, Position: $position, Label: $label, IsNew: $isNewWidget")

        if (!isNewWidget) {
            val savedIcon = preferences.getWidgetIcon(appWidgetId)
            if (savedIcon.isNotEmpty()) {
                label = savedIcon
                Log.e("BatteryWidgetEditApplyActivity", "Loaded saved icon for widget $appWidgetId: $savedIcon")
            }
        }

        loadAndDisplayImage()
        setupClickListeners()

        nativeAdHelper = NativeWidgetHelper(
            context = this,
            adId = getNativeInsideId(), // Replace with your ad unit ID
            showAdRemoteFlag = true,  // From remote config or your logic
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
            finish()
        }

        binding.buttonForApply.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(
                this,
                "click_apply_widget",
                mapOf(
                    "label" to label,
                    "widget_id" to appWidgetId.toString(),
                    "is_new_widget" to isNewWidget.toString()
                )
            )


            if (isScreenFirstOpen){
                isScreenFirstOpen = false
                if (preferences.shouldTriggerEveryThirdTime("interstitial_ad_count")) {
                    AdManager.showInterstitialAd(this,true) {
                        Log.e("Ads", "FullScreenTobeShoe")
                        FirebaseAnalyticsUtils.logClickEvent(this, "trigger_interstitial_ad", mapOf("screen" to "BatteryWidgetEditApplyScreen"))
                        try {
                            val appWidgetManager = AppWidgetManager.getInstance(this)
                            val widgetProvider = ComponentName(this, BatteryWidgetProvider::class.java)

                            if (isNewWidget) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {

                                    preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, label)

                                    val options = Bundle().apply {
                                        putString("WIDGET_ICON", label)
                                    }

                                    val successIntent = Intent(this, BatteryWidgetProvider::class.java).apply {
                                        action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                        putExtra("WIDGET_ICON", label)
                                    }

                                    val successCallback = PendingIntent.getBroadcast(
                                        this, 0, successIntent,
                                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                    )

                                    appWidgetManager.requestPinAppWidget(widgetProvider, options, successCallback)

                                    val updateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                                        action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                        putExtra("WIDGET_ICON", label)
                                    }
                                    sendBroadcast(updateIntent)

                                    for (delay in listOf(500, 1000, 2000)) {
                                        binding.root.postDelayed({
                                            val widgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)
                                            if (widgetIds.isNotEmpty()) {
                                                val newWidgetId = widgetIds.last()
                                                preferences.saveWidgetIcon(newWidgetId, label)

                                                val delayedUpdateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                                                    action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newWidgetId)
                                                    putExtra("WIDGET_ICON", label)
                                                }
                                                sendBroadcast(delayedUpdateIntent)
                                                appWidgetManager.notifyAppWidgetViewDataChanged(newWidgetId, R.id.battery_icon)
                                            }
                                        }, delay.toLong())
                                    }

                                } else {
                                    Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                preferences.saveWidgetIcon(appWidgetId, label)

                                val updateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                                    action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                    putExtra("WIDGET_ICON", label)
                                }
                                sendBroadcast(updateIntent)
                                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.battery_icon)
                            }

                            Toast.makeText(this, getString(R.string.widget_applied_successfully), Toast.LENGTH_SHORT).show()

                            if (!isNewWidget) {
                                val resultIntent = Intent().apply {
                                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                }
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }

                        } catch (e: Exception) {
                            Log.e("BatteryWidgetEditApplyActivity", "Error in buttonForApply click", e)
                        }

                    }
                }

            }else{
                FirebaseAnalyticsUtils.logClickEvent(this, "trigger_interstitial_ad", mapOf("screen" to "BatteryWidgetEditApplyScreen"))
                try {
                    val appWidgetManager = AppWidgetManager.getInstance(this)
                    val widgetProvider = ComponentName(this, BatteryWidgetProvider::class.java)

                    if (isNewWidget) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appWidgetManager.isRequestPinAppWidgetSupported) {

                            preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, label)

                            val options = Bundle().apply {
                                putString("WIDGET_ICON", label)
                            }

                            val successIntent = Intent(this, BatteryWidgetProvider::class.java).apply {
                                action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                putExtra("WIDGET_ICON", label)
                            }

                            val successCallback = PendingIntent.getBroadcast(
                                this, 0, successIntent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            appWidgetManager.requestPinAppWidget(widgetProvider, options, successCallback)

                            val updateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                                action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                putExtra("WIDGET_ICON", label)
                            }
                            sendBroadcast(updateIntent)

                            for (delay in listOf(500, 1000, 2000)) {
                                binding.root.postDelayed({
                                    val widgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)
                                    if (widgetIds.isNotEmpty()) {
                                        val newWidgetId = widgetIds.last()
                                        preferences.saveWidgetIcon(newWidgetId, label)

                                        val delayedUpdateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                                            action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newWidgetId)
                                            putExtra("WIDGET_ICON", label)
                                        }
                                        sendBroadcast(delayedUpdateIntent)
                                        appWidgetManager.notifyAppWidgetViewDataChanged(newWidgetId, R.id.battery_icon)
                                    }
                                }, delay.toLong())
                            }

                        } else {
                            Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        preferences.saveWidgetIcon(appWidgetId, label)

                        val updateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                            action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            putExtra("WIDGET_ICON", label)
                        }
                        sendBroadcast(updateIntent)
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.battery_icon)
                    }

                    Toast.makeText(this, getString(R.string.widget_applied_successfully), Toast.LENGTH_SHORT).show()

                    if (!isNewWidget) {
                        val resultIntent = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }

                } catch (e: Exception) {
                    Log.e("BatteryWidgetEditApplyActivity", "Error in buttonForApply click", e)
                }

            }





        }

        binding.buttonSetAsEmoji.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_set_as_emoji", mapOf("label" to label))
            finish()
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