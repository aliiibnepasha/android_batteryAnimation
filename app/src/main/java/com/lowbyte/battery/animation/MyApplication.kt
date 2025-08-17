package com.lowbyte.battery.animation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.DeadSystemException
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
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.AdStateController
import com.lowbyte.battery.animation.ads.GoogleMobileAdsConsentManager
import com.lowbyte.battery.animation.utils.AdLoadingDialogManager
import com.lowbyte.battery.animation.utils.AnimationUtils.getOpenAppId
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils.logPaidEvent
import com.lowbyte.battery.animation.utils.LocaleHelper
import com.lowbyte.battery.animation.utils.ServiceUtils.isEditing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class MyApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null
    private lateinit var preferences: AppPreferences

    companion object {
        var isOpenAdEnabled: Boolean = true

        fun enableOpenAd(enable: Boolean) {
            isOpenAdEnabled = enable
            Log.d("MyApplication", "Open Ad enabled: $enable")
        }
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        preferences =  AppPreferences.getInstance(this)

        AdManager.initializeAds(this)

        val lang = LocaleHelper.getLanguage(this)
        LocaleHelper.setLocale(this, lang.ifBlank { "" })
        CoroutineScope(Dispatchers.Default).launch {
            try {
                MobileAds.initialize(applicationContext)
            } catch (e: DeadSystemException) {
                Log.e("AdInit", "DeadSystemException — system is shutting down. Skipping ad init.")
            } catch (e: Exception) {
                Log.e("AdInit", "AdMob init error: ${e.message}")
            }
        }
        registerActivityLifecycleCallbacks(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }

        appOpenAdManager = AppOpenAdManager()
    }


    override fun attachBaseContext(newBase: Context) {
        val langUpdatedContext = LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase))
        super.attachBaseContext(langUpdatedContext)
    }


    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        currentActivity?.let {
            if (!it.isDestroyed && !it.isFinishing){
                if (appOpenAdManager.appOpenAd == null && !appOpenAdManager.isLoadingAd) {
                    appOpenAdManager.loadAd(it)
                } else {
                    appOpenAdManager.showAdIfAvailable(it, object : OnShowAdCompleteListener {
                        override fun onShowAdComplete() {

                        }

                    })
                }
            }


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


    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    private inner class AppOpenAdManager {

        private val googleMobileAdsConsentManager: GoogleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
        var appOpenAd: AppOpenAd? = null
        var isLoadingAd = false
        var isShowingAd = false
        private var loadTime: Long = 0

        fun loadAd(context: Context) {
            if (preferences.isProUser) return
            if (isLoadingAd || isAdAvailable() || preferences.isProUser) return

//            isLoadingAd = true
//            val request = AdRequest.Builder().build()
//            AppOpenAd.load(
//                context,
//                getOpenAppId(),
//                request,
//                object : AppOpenAdLoadCallback() {
//                    override fun onAdLoaded(ad: AppOpenAd) {
//                        appOpenAd = ad
//                        appOpenAd?.setImmersiveMode(true)
//
//                        isLoadingAd = false
//
//                        appOpenAd?.setOnPaidEventListener { adValue ->
//                            logPaidEvent(context,adValue, "appOpenAd", ad.adUnitId)
//
//                        }
//                        loadTime = Date().time
//                        Log.d("LOG_TAG", "onAdLoaded.")
//                    }
//                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                        appOpenAd = null
//                        isLoadingAd = false
//                        Log.d("LOG_TAG", "onAdFailedToLoad: ${loadAdError.message}")
//                    }
//                }
//            )
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference = Date().time - loadTime
            val numMilliSecondsPerHour = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {

            try {
                if (preferences.isProUser) {
                    Log.d("LOG_TAG", "Pro user — skipping open ad.")
                    onShowAdCompleteListener.onShowAdComplete()
                    return
                }

                if (!isInternetAvailable(activity)) {
                    Log.d("LOG_TAG", "No internet — skipping open ad.")
                    onShowAdCompleteListener.onShowAdComplete()
                    return
                }

                if (!isOpenAdEnabled) {
                    Log.d("LOG_TAG", "Open ad is disabled.")
                    onShowAdCompleteListener.onShowAdComplete()
                    return
                }

                if (AdStateController.isInterstitialShowing) {
                    Log.d("LOG_TAG", "Interstitial showing — skipping open ad.")
                    onShowAdCompleteListener.onShowAdComplete()
                    return
                }

                if (isShowingAd) {
                    Log.d("LOG_TAG", "Already showing open ad.")
                    return
                }

                val duration = if (isAdAvailable()) 1000L else 3000L
                if (!isOpenAdEnabled) {
                    return
                }else{
                    AdLoadingDialogManager.show(activity, duration) {
                        try {
                            if (!isAdAvailable()) {
                                Log.d("LOG_TAG", "Ad not available after delay — loading.")
                                onShowAdCompleteListener.onShowAdComplete()
                                if (googleMobileAdsConsentManager.canRequestAds) loadAd(activity)
                               return@show
                            }

                            if (activity.isFinishing || activity.isDestroyed) {
                                Log.w("LOG_TAG", "Activity is not valid — cannot show ad.")
                                onShowAdCompleteListener.onShowAdComplete()
                                return@show
                            }

                            isShowingAd = true
                            AdStateController.isOpenAdShowing = true

                            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    activity.isEditing(false)
                                    appOpenAd = null
                                    isShowingAd = false
                                    AdStateController.isOpenAdShowing = false
                                    onShowAdCompleteListener.onShowAdComplete()
                                    if (googleMobileAdsConsentManager.canRequestAds) loadAd(activity)
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    activity.isEditing(false)

                                    Log.e("LOG_TAG", "Open ad failed: ${adError.message}")
                                    appOpenAd = null
                                    isShowingAd = false
                                    AdStateController.isOpenAdShowing = false
                                    onShowAdCompleteListener.onShowAdComplete()
                                    if (googleMobileAdsConsentManager.canRequestAds) loadAd(activity)
                                }

                                override fun onAdShowedFullScreenContent() {
                                    activity.isEditing(true)
                                    Log.d("LOG_TAG", "Open ad is showing.")
                                    AdStateController.isOpenAdShowing = true
                                }
                            }
                            activity.isEditing(true)
                            appOpenAd?.show(activity)
                        } catch (e: Exception) {
                            activity.isEditing(false)
                            Log.e("LOG_TAG", "Error showing ad: ${e.localizedMessage}")
                            isShowingAd = false
                            AdStateController.isOpenAdShowing = false
                            onShowAdCompleteListener.onShowAdComplete()
                        }
                    }
                }
            } catch (e: Exception) {
               e.printStackTrace()
            }

        }

        private fun isInternetAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}