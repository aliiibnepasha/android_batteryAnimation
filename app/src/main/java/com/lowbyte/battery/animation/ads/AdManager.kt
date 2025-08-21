package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lowbyte.battery.animation.utils.*
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenHome2Id
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.isCoolDownShowTime
import com.lowbyte.battery.animation.utils.AnimationUtils.isValid
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils.logPaidEvent
import com.lowbyte.battery.animation.utils.ServiceUtils.isEditing
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {

    private const val TAG = "AdManager"

     var interstitialAd: InterstitialAd? = null
    private var adIsLoading = false
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var preferences: AppPreferences

    private const val LAST_AD_TIME_KEY = "last_interstitial_ad_time"
    private const val RELOAD_DELAY_MS = 10_000L

    // Cooldown control flags
    private var isCooldownEnabledForLoad = false
    private var isCooldownEnabledForShow = false

    fun setCooldownEnabledForLoad(enabled: Boolean) {
        isCooldownEnabledForLoad = enabled
    }

    fun setCooldownEnabledForShow(enabled: Boolean) {
        isCooldownEnabledForShow = enabled
    }

    fun initializeAds(context: Context) {
        preferences = AppPreferences.getInstance(context)

        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            Log.d(TAG, "Ads SDK already initialized")
            return
        }

        if (preferences.isProUser) {
            Log.d(TAG, "Pro user — skipping ad initialization")
            return
        }

        Log.d(TAG, "Initializing Mobile Ads SDK")
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("ABCDEF012345"))
                .build()
        )
    }

    fun loadInterstitialAd(context: Activity, fullscreenAdId: String, remoteConfig: Boolean) {
        preferences = AppPreferences.getInstance(context)

        if (preferences.isProUser || !remoteConfig || adIsLoading || interstitialAd != null) {
            Log.d(
                TAG,
                "Skipping interstitial load: proUser=${preferences.isProUser}, remoteConfig=$remoteConfig, loading=$adIsLoading, alreadyLoaded=${interstitialAd != null}"
            )
            return
        }

        if (isCooldownEnabledForLoad && !hasCooldownPassed()) {
            Log.d(TAG, "Cooldown active — skipping ad load")
            return
        }

        adIsLoading = true
        Log.d(TAG, "Loading interstitial ad with ID: $fullscreenAdId")

        InterstitialAd.load(
            context,
            fullscreenAdId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    adIsLoading = false
                    if (context.isValid()) {
                        interstitialAd = ad
                        ad.setImmersiveMode(true)
                        Log.d(TAG, "Interstitial Ad successfully loaded")
                        ad.setOnPaidEventListener { adValue ->
                            logPaidEvent(context, adValue, "interstitialAd", ad.adUnitId)
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adIsLoading = false
                    interstitialAd = null
                    Log.e(TAG, "Interstitial Ad failed to load: ${adError.message}")
                }
            }
        )
    }

    fun showInterstitialAd(
        activity: Activity,
        remoteConfig: Boolean,
        isFromActivity: Boolean,
        onDismiss: () -> Unit
    ) {
        preferences = AppPreferences.getInstance(activity)

        if (preferences.isProUser || !remoteConfig || !activity.isValid()) {
            Log.d(TAG, "Skipping ad: Pro user or remote config off")
            onDismiss()
            activity.isEditing(false)
            return
        }

        if (!isInternetAvailable(activity)) {
            Log.d(TAG, "Skipping ad: No internet")
            onDismiss()
            activity.isEditing(false)
            return
        }

        if (isCooldownEnabledForShow && !hasCooldownPassed()) {
            Log.d(TAG, "Ad cooldown active — skipping ad")
            onDismiss()
            activity.isEditing(false)
            return
        }

        if (activity.isFinishing || activity.isDestroyed) return

        val delay = if (interstitialAd != null) 1000L else {
            loadInterstitialAd(activity, getFullscreenHome2Id(), remoteConfig)
            3000L
        }

        AdLoadingDialogManager.show(activity, delay) {
            if (activity.isValid()) {
                continueWithInterstitialAd(activity, remoteConfig, isFromActivity, onDismiss)
            }
        }
    }

    private fun continueWithInterstitialAd(
        activity: Activity,
        remoteConfig: Boolean,
        isFromActivity: Boolean,
        onDismiss: () -> Unit
    ) {
        if (AdStateController.isOpenAdShowing) {
            Log.d(TAG, "Open Ad is showing — skipping interstitial")
            onDismiss()
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity not valid — skip showing ad")
            interstitialAd = null
            AdStateController.isInterstitialShowing = false
            onDismiss()
            return
        }

        if (interstitialAd == null) {
            Log.d(TAG, "Ad not ready — fallback and reload")
            onDismiss()
            loadInterstitialAd(activity, getFullscreenId(), remoteConfig)
            activity.isEditing(isEditing = false, isAdShowing = false)
            return
        }

        AdStateController.isInterstitialShowing = true

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                activity.isEditing(isEditing = true, isAdShowing = true)
                Log.d(TAG, "Ad is now showing")


            }

            override fun onAdDismissedFullScreenContent() {
                Log.e(TAG, "Ad Dismissed")

                if (isFromActivity) onDismiss()
                adIsLoading = false
                interstitialAd = null
                AdStateController.isInterstitialShowing = false
                activity.isEditing(isEditing = false, isAdShowing = false)
                updateLastAdShownTime()
                activity.window.decorView.postDelayed({
                    try {
                        if (activity.isValid() && isAppInForeground(activity) && !preferences.isProUser) {
                            loadInterstitialAd(activity, getFullscreenHome2Id(), true)
                            Log.e(TAG, "auto-loading ad")

                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error auto-loading ad: ${e.localizedMessage}")
                    }
                }, RELOAD_DELAY_MS)

            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                adIsLoading = false
                interstitialAd = null
                AdStateController.isInterstitialShowing = false
                Log.e(TAG, "Failed to show interstitial: ${adError.message}")
                onDismiss()
                activity.isEditing(isEditing = false, isAdShowing = false)
            }

            override fun onAdImpression() {
                Log.d(TAG, "Interstitial impression recorded")
                if (!isFromActivity) onDismiss()
            }
        }

        try {
            interstitialAd?.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing interstitial: ${e.localizedMessage}")
            interstitialAd = null
            AdStateController.isInterstitialShowing = false
            onDismiss()
        }
    }

    // Cooldown helpers
    private fun updateLastAdShownTime() {
        preferences.setLong(LAST_AD_TIME_KEY, System.currentTimeMillis())
    }

    private fun hasCooldownPassed(): Boolean {
        val lastTime = preferences.getLong(LAST_AD_TIME_KEY, 0L)
        return System.currentTimeMillis() - lastTime >= isCoolDownShowTime
    }

    // Internet check
    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Foreground check
    private fun isAppInForeground(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val packageName = context.packageName
        return manager?.runningAppProcesses?.any {
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    it.processName == packageName
        } ?: false
    }
}