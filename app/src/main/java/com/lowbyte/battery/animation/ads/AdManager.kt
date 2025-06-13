package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AppPreferences
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
        preferences = AppPreferences.getInstance(context)
        if (isMobileAdsInitializeCalled.getAndSet(true) || preferences.isProUser) return

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("ABCDEF012345"))
                .build()
        )

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(context)
        }
    }

    fun loadInterstitialAd(context: Context) {
        preferences = AppPreferences.getInstance(context)
        if (preferences.isProUser) {
            Log.d(TAG, "Skipping interstitial because user is a pro")
            return
        }
        if (adIsLoading || interstitialAd != null) return

        adIsLoading = true
        InterstitialAd.load(
            context,
            getFullscreenId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    adIsLoading = false
                    Log.d(TAG, "Interstitial Ad Loaded")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Ad Failed to Load: ${adError.message}")
                    interstitialAd = null
                    adIsLoading = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onDismiss: () -> Unit) {
        if (preferences.isProUser) {
            Log.d(TAG, "Skipping interstitial to show because user is a pro")
            onDismiss()
            return
        }

        if (AdStateController.isOpenAdShowing) {
            Log.d(TAG, "Skipping interstitial because Open Ad is showing")
            onDismiss()
            return
        }

        if (interstitialAd == null) {
            onDismiss()
            loadInterstitialAd(activity)
            return
        }

        AdStateController.isInterstitialShowing = true

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                AdStateController.isInterstitialShowing = false
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                AdStateController.isInterstitialShowing = false
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial Ad Shown")
            }
        }

        interstitialAd?.show(activity)
    }
}