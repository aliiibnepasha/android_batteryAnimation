package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lowbyte.battery.animation.utils.AdLoadingDialogManager
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenHome2Id
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.isValid
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils.logPaidEvent
import com.lowbyte.battery.animation.utils.ServiceUtils.isEditing
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private const val TAG = "AdManager"

     var interstitialAd: InterstitialAd? = null
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


    }

    fun loadInterstitialAd(context: Activity, fullscreenAdId: String, remoteConfig: Boolean) {
        preferences = AppPreferences.getInstance(context)
        if (preferences.isProUser || !remoteConfig) {
            Log.d(TAG, "Pro user — skipping interstitial ad load ${preferences.isProUser} or $remoteConfig")
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
                    adIsLoading = false
                    if (context.isValid()){
                        interstitialAd = ad
                        interstitialAd?.setImmersiveMode(true)
                        Log.d(TAG, "Interstitial Ad successfully loaded")
                        interstitialAd?.setOnPaidEventListener { adValue ->
                            logPaidEvent(context,adValue, "interstitialAd", ad.adUnitId)


                        }
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
        AppPreferences.getInstance(activity)
        if (preferences.isProUser || !remoteConfig || !activity.isValid()) {
            Log.d(TAG, "Pro user or remote config disabled — skipping ad and dialog")
            onDismiss()
            return
        }

        if (!isInternetAvailable(activity)) {
            Log.d(TAG, "No internet connection — skipping ad and dialog")
            onDismiss()
            return
        }

        if (activity.isFinishing || activity.isDestroyed){
            return
        }
        val dialogDuration = if (interstitialAd != null) {
            1000L
        } else {
            loadInterstitialAd(
                activity,
                getFullscreenHome2Id(),
                remoteConfig
            )
            4500L
        }

        if (activity.isValid()){
            AdLoadingDialogManager.show(activity, dialogDuration) {
                if (activity.isValid()){
                    continueWithInterstitialAd(activity, remoteConfig, isFromActivity, onDismiss)
                }
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
            Log.w(TAG, "Activity is not in a valid state — skip showing ad")
            interstitialAd = null
            AdStateController.isInterstitialShowing = false
            onDismiss()
            return
        }

        if (interstitialAd == null && activity.isValid()) {
            Log.d(TAG, "Interstitial ad not ready — fallback and reload")
            onDismiss()
            loadInterstitialAd(activity, getFullscreenId(), remoteConfig)
            activity.isEditing(isEditing = false, isAdShowing = false,)

            return
        }

        AdStateController.isInterstitialShowing = true
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                adIsLoading = false
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                AdStateController.isInterstitialShowing = false
                if (isFromActivity) onDismiss()
                activity.isEditing(isEditing = false, isAdShowing = false)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                adIsLoading = false
                Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                interstitialAd = null
                AdStateController.isInterstitialShowing = false
                onDismiss()
                activity.isEditing(false,false)

            }

            override fun onAdImpression() {
                activity.isEditing(isEditing = true, isAdShowing = true)
                Log.d(TAG, "Interstitial ad impression recorded")
                if (!isFromActivity) onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                activity.isEditing(isEditing = true, isAdShowing = true)
                Log.d(TAG, "Interstitial ad is now visible")
            }
        }

        try {
            Log.d(TAG, "Showing interstitial ad")
            if (activity.isValid()){
                activity.isEditing(isEditing = true, isAdShowing = true)
                interstitialAd?.show(activity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while showing interstitial ad: ${e.localizedMessage}")
            interstitialAd = null
            AdStateController.isInterstitialShowing = false

            onDismiss()
        }
    }



    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }





}