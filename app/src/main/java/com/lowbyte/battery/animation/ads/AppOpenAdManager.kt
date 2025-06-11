package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.utils.AnimationUtils.getOpenAppId

class AppOpenAdManager(private val app: Application) : Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isAdShowing = false
    private var dialog: Dialog? = null
    private var shouldShowAdWhenReady = false

    init {
        Log.d("AppOpenAdManager", "Initialized and registering lifecycle callbacks")
        app.registerActivityLifecycleCallbacks(this)
    }

    private fun loadAdIfNotAvailable() {
        if (appOpenAd == null && !isLoadingAd) {
            isLoadingAd = true
            val adRequest = AdRequest.Builder().build()

            AppOpenAd.load(
                app, getOpenAppId(), adRequest,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        Log.d("AppOpenAdManager", "Ad loaded")

                        if (shouldShowAdWhenReady && currentActivity != null) {
                            shouldShowAdWhenReady = false
                            showAdIfAvailable(currentActivity!!, false)
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d("AppOpenAdManager", "Ad failed to load: ${loadAdError.message}")
                        isLoadingAd = false
                    }
                })
        }
    }

    fun showAdIfAvailable(activity: Activity, autoRequest: Boolean) {
        if (isAdShowing || appOpenAd == null) return

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                appOpenAd = null
                if (autoRequest) {
                    loadAdIfNotAvailable()
                }
                Log.d("AppOpenAdManager", "Ad dismissed")
            }

            override fun onAdShowedFullScreenContent() {
                isAdShowing = true
                Log.d("AppOpenAdManager", "Ad shown")
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                isAdShowing = false
                appOpenAd = null
                Log.d("AppOpenAdManager", "Ad failed to show")
            }
        }

        appOpenAd?.show(activity)
    }

    fun loadAndShowAdWithDialog(activity: Activity) {
        if (appOpenAd != null) {
            showAdIfAvailable(activity, false)
            return
        }

        if (isAdShowing || isLoadingAd) return

        isLoadingAd = true
        showLoadingDialog(activity)

        val adRequest = AdRequest.Builder().build()
        val handler = Handler(Looper.getMainLooper())

        AppOpenAd.load(
            app, getOpenAppId(), adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    handler.post {
                        dismissDialog()
                        showAdIfAvailable(activity, false)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Log.d("AppOpenAdManager", "Ad failed: ${loadAdError.message}")
                    handler.post { dismissDialog() }
                }
            })

        handler.postDelayed({
            if (appOpenAd == null) {
                dismissDialog()
                Log.d("AppOpenAdManager", "Ad not loaded in 5 seconds")
            }
        }, 5000)
    }

    private fun showLoadingDialog(activity: Activity) {
        dismissDialog()
        dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            val layout = LayoutInflater.from(activity).inflate(R.layout.dialog_loading_ad, null)
            setContentView(layout)
            setCancelable(false)
            show()
        }
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    // -- Lifecycle tracking --

    override fun onActivityPaused(activity: Activity) {
        // Load on pause to have fresh ad ready for resume
        loadAdIfNotAvailable()
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("AppOpenAdManager", "onActivityResumed called for ${activity.localClassName}")
        currentActivity = activity

        if (appOpenAd != null) {
            showAdIfAvailable(activity, false)
        } else if (!isLoadingAd) {
            shouldShowAdWhenReady = true
            loadAdIfNotAvailable()
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity == currentActivity) {
            currentActivity = null
        }
        // Clear everything when app closes
        dismissDialog()
        appOpenAd = null
        isLoadingAd = false
        isAdShowing = false
    }

    // Unused lifecycle callbacks
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}