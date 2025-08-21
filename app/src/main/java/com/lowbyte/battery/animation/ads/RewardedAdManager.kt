package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.lowbyte.battery.animation.utils.AdLoadingDialogManager
import com.lowbyte.battery.animation.utils.AnimationUtils.getRewardedId
import com.lowbyte.battery.animation.utils.AnimationUtils.isRewardedEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isValid
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.ServiceUtils.isEditing


object RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
     var rewardedAdLoaded: Boolean = false
     var isClickedPerform: Boolean = false
    private var isLoading = false
    private var pendingShowRequest = false
    private const val TAG = "RewardedAdManager"

    fun loadAd(context: Activity, onLoaded: (() -> Unit)? = null, onFailed: (() -> Unit)? = null) {
        if (rewardedAd != null || isLoading || !isRewardedEnabled || !context.isValid()) return
        if (AppPreferences.getInstance(context).isProUser) return
        if (!isInternetAvailable(context)) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, getRewardedId(), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAdLoaded = true
                if (context.isValid()) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded ad loaded")
                    onLoaded?.invoke()
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "Failed to load rewarded ad: ${loadAdError.message}")
                isLoading = false
                rewardedAd = null
                onFailed?.invoke()
                rewardedAdLoaded = false
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdShown: () -> Unit = {

        },
        onAdDismissed: () -> Unit = {

        }
    ) {
        val prefs = AppPreferences.getInstance(activity)

        if (!isRewardedEnabled || prefs.isProUser || !isInternetAvailable(activity)) return

        pendingShowRequest = true

        if (rewardedAd != null && activity.isValid()) {
            showAdInternal(activity, onRewardEarned, onAdShown, onAdDismissed)
        } else {
            // show dialog while loading
            AdLoadingDialogManager.show(activity, 8000L) {
                pendingShowRequest = false // timeout reached
                if (rewardedAd != null && activity.isValid()) {
                    showAdInternal(activity, onRewardEarned, onAdShown, onAdDismissed)
                } else {
                    AdStateController.isInterstitialShowing = false
                    activity.isEditing(isEditing = false, isAdShowing = false)
                }
            }

            // begin loading ad
            loadAd(
                context = activity,
                onLoaded = {
                    if (pendingShowRequest && activity.isValid()) {
                        AdLoadingDialogManager.dismiss()
                        showAdInternal(activity, onRewardEarned, onAdShown, onAdDismissed)
                    }
                },
                onFailed = {
                    if (pendingShowRequest && activity.isValid()) {
                        AdLoadingDialogManager.dismiss()
                        AdStateController.isInterstitialShowing = false
                        activity.isEditing(isEditing = false, isAdShowing = false)
                    }
                }
            )
        }
    }

    private fun showAdInternal(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdShown: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad shown")
                activity.isEditing(isEditing = true, isAdShowing = true)
                AdStateController.isInterstitialShowing = true
                onAdShown()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed")
                activity.isEditing(isEditing = false, isAdShowing = false)
                AdStateController.isInterstitialShowing = false
                rewardedAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Failed to show ad: ${adError.message}")
                if (adError.message == "The ad has already been shown."){
                    return
                }
                AdStateController.isInterstitialShowing = false
                rewardedAd = null
                loadAd(activity) // optional preload
             //   activity.isEditing(isEditing = false, isAdShowing = false)
            }

            override fun onAdImpression() {
                activity.isEditing(true, isAdShowing = true)
                super.onAdImpression()
            }
        }

        activity.isEditing(true, isAdShowing = true)
        rewardedAd?.show(activity) { rewardItem: RewardItem ->
            activity.isEditing(isEditing = true, isAdShowing = true)
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned()
        }
    }

     fun isInternetAvailable(context: Activity): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
