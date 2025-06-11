package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private const val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading = false
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)

    fun initializeAds(context: Context) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) return

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
        if (interstitialAd == null) {
            onDismiss()
            loadInterstitialAd(activity)
            return
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad Dismissed")
                interstitialAd = null
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad Failed to Show")
                interstitialAd = null
                onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad Showed")
            }
        }
        interstitialAd?.show(activity)
    }
}