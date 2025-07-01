package com.lowbyte.battery.animation.activity

import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.gms.ads.MobileAds
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivitySplashBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.isBannerHomeEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isBannerSplashEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyAnimEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyWidgetEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenGestureEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenSplashEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenStatusEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyAnimEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeApplyWidgetEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeGestureEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeIntroEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeLangFirstEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeLangSecondEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeStatusEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var preferences: AppPreferences
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivityLog", "onCreate called")
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = AppPreferences.getInstance(this)
        Log.d("SplashActivityLog", "Preferences initialized: isProUser=${preferences.isProUser}")

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@SplashActivity) {
                Log.d("SplashActivityLog", "MobileAds initialized")
            }
        }



        val configDefaults = mapOf(
            "ads_config" to """
        {
            "BannerAdSplash_enabled": true,
            "BannerAdHome_enabled": true,
            "FullscreenSplash_enabled": true,
            "FullscreenGestureBack_enabled": true,
            "FullscreenStatusBack_enabled": true,
            "FullscreenApplyEmoji_enabled": true,
            "FullscreenApplyWidget_enabled": true,
            "FullscreenApplyAnim_enabled": true,
            "NativeLanguageFirst_enabled": true,
            "NativeLanguageSecond_enabled": true,
            "NativeApplyEmoji_enabled": true,
            "NativeApplyWidget_enabled": true,
            "NativeApplyAnimation_enabled": true,
            "NativeSmallStatusBar_enabled": true,
            "NativeSmallGesture_enabled": true
        }
    """.trimIndent()
        )

        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setDefaultsAsync(configDefaults)
        remoteConfig.fetchAndActivate()




        AdManager.initializeAds(this)
        Log.d("SplashActivityLog", "AdManager initialized")

        checkSubscriptionStatus()
        fetchAdSettingsAndLoad()
    }
    fun fetchAdSettingsAndLoad() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val adConfigJson = remoteConfig.getString("ads_config")
        try {
            val jsonObject = JSONObject(adConfigJson)
             isBannerSplashEnabled = jsonObject.optBoolean("BannerAdSplash_enabled", true)
             isBannerHomeEnabled = jsonObject.optBoolean("BannerAdHome_enabled", true)
             isFullscreenSplashEnabled = jsonObject.optBoolean("FullscreenSplash_enabled", true)
             isFullscreenStatusEnabled = jsonObject.optBoolean("FullscreenStatusBack_enabled", true)
             isFullscreenGestureEnabled = jsonObject.optBoolean("FullscreenGestureBack_enabled", true)
             isFullscreenApplyEmojiEnabled = jsonObject.optBoolean("FullscreenApplyEmoji_enabled", true)
             isFullscreenApplyWidgetEnabled = jsonObject.optBoolean("FullscreenApplyWidget_enabled", true)
             isFullscreenApplyAnimEnabled = jsonObject.optBoolean("FullscreenApplyAnim_enabled", true)
             isNativeIntroEnabled = jsonObject.optBoolean("NativeIntro_enabled", true)
             isNativeLangFirstEnabled = jsonObject.optBoolean("NativeLanguageFirst_enabled", true)
             isNativeLangSecondEnabled = jsonObject.optBoolean("NativeLanguageSecond_enabled", true)
             isNativeApplyEmojiEnabled = jsonObject.optBoolean("NativeApplyEmoji_enabled", true)
             isNativeApplyWidgetEnabled = jsonObject.optBoolean("NativeApplyWidget_enabled", true)
             isNativeApplyAnimEnabled = jsonObject.optBoolean("NativeApplyAnimation_enabled", true)
             isNativeStatusEnabled = jsonObject.optBoolean("NativeSmallStatusBar_enabled", true)
             isNativeGestureEnabled = jsonObject.optBoolean("NativeSmallGesture_enabled", true)
            Log.e("RemoteConfig", "Json $adConfigJson")

        } catch (e: JSONException) {
            Log.e("RemoteConfig", "Failed to parse ads_config JSON: ${e.message}")
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

    override fun onResume() {
        super.onResume()
        Log.d("SplashActivityLog", "onResume called")
    }
}