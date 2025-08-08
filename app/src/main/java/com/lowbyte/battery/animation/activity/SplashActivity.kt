package com.lowbyte.battery.animation.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeLanguageHelper
import com.lowbyte.battery.animation.databinding.ActivitySplashBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.isBannerHomeEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isBannerPermissionSettings
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyAnimEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyWidgetEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenDynamicDoneEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenGestureEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenSplashEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenStatusEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyAnimEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyWidgetEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeDynamicEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeGestureEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeHomeEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeIntroEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeLangFirstEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeLangSecondEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeSplashEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeStatusEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isRewardedEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.remoteJsonKey
import com.lowbyte.battery.animation.utils.AppPreferences
import org.json.JSONException
import org.json.JSONObject


class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var preferences: AppPreferences
    private lateinit var billingClient: BillingClient

    private var nativeHelper: NativeLanguageHelper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d("SplashActivityLog", "onCreate called")
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)


            preferences = AppPreferences.getInstance(this)
            Log.d("SplashActivityLog", "Preferences initialized: isProUser=${preferences.isProUser}")
            if (!preferences.isProUser){
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            }

            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(30)
                .build()

            remoteConfig.setConfigSettingsAsync(settings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("RemoteConfig", "Fetch and activate success")
                    } else {
                        Log.e("RemoteConfig", "Fetch failed: ${task.exception?.message}")
                    }
                    fetchAdSettingsAndLoad() // Always load config (from cache or new)
                }

            AdManager.initializeAds(this)
            checkSubscriptionStatus()
            Log.d("SplashActivityLog", "AdManager initialized")

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun fetchAdSettingsAndLoad() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val adConfigJson = remoteConfig.getString(remoteJsonKey)
        try {
            val jsonObject = JSONObject(adConfigJson)
            isFullscreenSplashEnabled = jsonObject.optBoolean("FullscreenSplash_enabled", true)
            isRewardedEnabled = jsonObject.optBoolean("Rewarded_enabled", true)
            isFullscreenStatusEnabled = jsonObject.optBoolean("FullscreenStatusBack_enabled", true)
            isFullscreenDynamicDoneEnabled = jsonObject.optBoolean("FullscreenDynamicDone_enabled", true)
            isFullscreenGestureEnabled = jsonObject.optBoolean("FullscreenGestureBack_enabled", true)
            isFullscreenApplyEmojiEnabled = jsonObject.optBoolean("FullscreenApplyEmoji_enabled", true)
            isFullscreenApplyWidgetEnabled = jsonObject.optBoolean("FullscreenApplyWidget_enabled", true)
            isFullscreenApplyAnimEnabled = jsonObject.optBoolean("FullscreenApplyAnim_enabled", true)
            isBannerPermissionSettings = jsonObject.optBoolean("BannerAdSettings_enabled", true)
            isBannerHomeEnabled = jsonObject.optBoolean("BannerAdHome_enabled", true)
            isNativeHomeEnabled = jsonObject.optBoolean("NativeAdHome_enabled", true)
            isNativeSplashEnabled = jsonObject.optBoolean("NativeAdSplash_enabled", true)
            isNativeIntroEnabled = jsonObject.optBoolean("NativeIntro_enabled", true)
            isNativeLangFirstEnabled = jsonObject.optBoolean("NativeLanguageFirst_enabled", false)
            isNativeLangSecondEnabled = jsonObject.optBoolean("NativeLanguageSecond_enabled", true)
            isNativeApplyEmojiEnabled = jsonObject.optBoolean("NativeApplyEmoji_enabled", true)
            isNativeApplyWidgetEnabled = jsonObject.optBoolean("NativeApplyWidget_enabled", true)
            isNativeApplyAnimEnabled = jsonObject.optBoolean("NativeApplyAnimation_enabled", true)
            isNativeStatusEnabled = jsonObject.optBoolean("NativeSmallStatusBar_enabled", true)
            isNativeDynamicEnabled = jsonObject.optBoolean("NativeDynamic_enabled", true)
            isNativeGestureEnabled = jsonObject.optBoolean("NativeSmallGesture_enabled", true)

            Log.d("RemoteConfig", "Remote config loaded: $adConfigJson")
        } catch (e: JSONException) {
            Log.e("RemoteConfig", "Failed to parse config JSON: ${e.message}")
        }
    }


    private fun checkSubscriptionStatus() {
        Log.d("SplashActivityLog", "Starting subscription status check")

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                Log.d("SplashActivityLog", "Billing listener called: ${billingResult.debugMessage}")
            }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d("SplashActivityLog", "Billing setup finished: ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryActiveSubscriptions()
                } else {
                    Log.w("SplashActivityLog", "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("SplashActivityLog", "Billing service disconnected")
            }
        })
    }

    private fun queryActiveSubscriptions() {
        Log.d("SplashActivityLog", "Querying active subscriptions...")

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            Log.d("SplashActivityLog", "Query result: ${billingResult.responseCode}, ${billingResult.debugMessage}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("SplashActivityLog", "Found ${purchases.size} purchase(s)")

                val isStillSubscribed = purchases.any { purchase ->
                    Log.d(
                        "SplashActivityLog",
                        "Purchase: state=${purchase.purchaseState}, acknowledged=${purchase.isAcknowledged}, products=${purchase.products}"
                    )
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged
                }

                preferences.isProUser = isStillSubscribed
                Log.d("SplashActivityLog", "isProUser set to $isStillSubscribed")

                if (!isStillSubscribed) {
                    preferences.setString("active_subscription", "")
                    Log.d("SplashActivityLog", "Cleared active_subscription preference")
                }
            } else {
                Log.e("SplashActivityLog", "Failed to query purchases: ${billingResult.debugMessage}")
            }
        }
    }
    fun changeLanguage(language: String) {
        setLanguage(language)
    }


    override fun onResume() {
        super.onResume()
        Log.d("SplashActivityLog", "onResume called")
    }

    override fun onDestroy() {
        nativeHelper = null

        AdManager.interstitialAd = null
        super.onDestroy()
    }
}