package com.lowbyte.battery.animation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.lowbyte.battery.animation.ads.AdStateController
import com.lowbyte.battery.animation.ads.GoogleMobileAdsConsentManager
import com.lowbyte.battery.animation.utils.AnimationUtils.getOpenAppId
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.LocaleHelper
import java.util.Date

class MyApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    companion object {
        var isOpenAdEnabled: Boolean = true

        fun enableOpenAd(enable: Boolean) {
            isOpenAdEnabled = enable
            Log.d("MyApplication", "Open Ad enabled: $enable")
        }
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        AppPreferences.getInstance(this)

        val lang = LocaleHelper.getLanguage(this)
        LocaleHelper.setLocale(this, if (lang.isBlank()) "en" else lang)

        MobileAds.initialize(this)
        registerActivityLifecycleCallbacks(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }

        appOpenAdManager = AppOpenAdManager()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            appOpenAdManager.showAdIfAvailable(it)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener)
    }

    fun loadAd(activity: Activity) {
        appOpenAdManager.loadAd(activity)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    private inner class AppOpenAdManager {

        private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(applicationContext)
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false
        private var loadTime: Long = 0

        fun loadAd(context: Context) {
            if (isLoadingAd || isAdAvailable()) return

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                getOpenAppId(),
                request,
                object : AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        Log.d("LOG_TAG", "onAdLoaded.")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        appOpenAd = null
                        isLoadingAd = false
                        Log.d("LOG_TAG", "onAdFailedToLoad: ${loadAdError.message}")
                    }
                }
            )
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference = Date().time - loadTime
            val numMilliSecondsPerHour = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        fun showAdIfAvailable(activity: Activity) {
            showAdIfAvailable(activity, object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {}
            })
        }

        fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
            if (!isOpenAdEnabled) {
                Log.d("LOG_TAG", "Open ad is disabled.")
                onShowAdCompleteListener.onShowAdComplete()
                return
            }

            if (AdStateController.isInterstitialShowing) {
                Log.d("LOG_TAG", "Fullscreen ad is showing. Skipping open ad.")
                onShowAdCompleteListener.onShowAdComplete()
                return
            }

            if (isShowingAd) {
                Log.d("LOG_TAG", "Open ad is already showing.")
                return
            }

            if (!isAdAvailable()) {
                Log.d("LOG_TAG", "Open ad not ready. Triggering load.")
                onShowAdCompleteListener.onShowAdComplete()
                if (googleMobileAdsConsentManager.canRequestAds) loadAd(activity)
                return
            }

            Log.d("LOG_TAG", "Showing open ad.")
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    AdStateController.isOpenAdShowing = false
                    onShowAdCompleteListener.onShowAdComplete()
                    if (googleMobileAdsConsentManager.canRequestAds) loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    AdStateController.isOpenAdShowing = false
                    onShowAdCompleteListener.onShowAdComplete()
                    if (googleMobileAdsConsentManager.canRequestAds) loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("LOG_TAG", "Open ad is showing.")
                    AdStateController.isOpenAdShowing = true
                }
            }

            isShowingAd = true
            AdStateController.isOpenAdShowing = true
            appOpenAd?.show(activity)
        }    }
}