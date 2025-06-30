package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils.logPaidEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private const val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading = false
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var preferences: AppPreferences

    fun initializeAds(context: Context) {
        Log.d(TAG, "Initializing Mobile Ads SDK")
        preferences = AppPreferences.getInstance(context)

        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            Log.d(TAG, "Ads SDK already initialized")
            return
        }

        if (preferences.isProUser) {
            Log.d(TAG, "User is pro — skipping ads initialization")
            return
        }

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("ABCDEF012345"))
                .build()
        )

        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Calling MobileAds.initialize()")
            MobileAds.initialize(context)
        }
    }

    fun loadInterstitialAd(context: Context, fullscreenAdId: String, remoteConfig: Boolean) {
        preferences = AppPreferences.getInstance(context)
        if (preferences.isProUser || remoteConfig) {
            Log.d(TAG, "Pro user — skipping interstitial ad load")
            return
        }

        if (adIsLoading) {
            Log.d(TAG, "Interstitial ad is already loading")
            return
        }

        if (interstitialAd != null) {
            Log.d(TAG, "Interstitial ad already loaded")
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
                    interstitialAd = ad
                    interstitialAd?.setImmersiveMode(true)
                    adIsLoading = false
                    Log.d(TAG, "Interstitial Ad successfully loaded")
                    interstitialAd?.setOnPaidEventListener { adValue ->
                        logPaidEvent(context,adValue, "interstitialAd", ad.adUnitId)


                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    adIsLoading = false
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
        Log.d(TAG, "Attempting to show interstitial ad")

        if (preferences.isProUser || remoteConfig) {
            Log.d(TAG, "Pro user — skipping interstitial ad show")
            onDismiss()
            return
        }

        if (AdStateController.isOpenAdShowing) {
            Log.d(TAG, "Open Ad is currently showing — skipping interstitial")
            onDismiss()
            return
        }

        if (interstitialAd == null) {
            Log.d(TAG, "Interstitial ad not ready — fallback and reload")
            onDismiss()
            loadInterstitialAd(activity, getFullscreenId(),remoteConfig)
            return
        }

        AdStateController.isInterstitialShowing = true
        Log.d(TAG, "Showing interstitial ad now")
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                AdStateController.isInterstitialShowing = false

                if (isFromActivity) {
                    Log.d(TAG, "Callback: isFromActivity=true — executing onDismiss()")
                    onDismiss()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                interstitialAd = null
                AdStateController.isInterstitialShowing = false
                onDismiss()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Interstitial ad impression recorded")
                if (!isFromActivity) {
                    Log.d(TAG, "Callback: isFromActivity=false — executing onDismiss() after impression")
                    onDismiss()
                }
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad is now visible")
            }
        }

        interstitialAd?.show(activity)
    }
}