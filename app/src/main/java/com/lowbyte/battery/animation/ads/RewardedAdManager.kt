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

object RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var isRewardEarned = false
    private const val TAG = "RewardedAdManager"

    fun loadAd(context: Activity) {
        if (rewardedAd != null || isLoading || !isRewardedEnabled || !context.isValid()) return

        if (AppPreferences.getInstance(context).isProUser) return
        if (!isInternetAvailable(context)) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, getRewardedId(), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                if (context.isValid()){
                    rewardedAd = ad
                    isLoading = false
                }
                Log.d(TAG, "Rewarded ad loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "Failed to load rewarded ad: ${loadAdError.message}")
                isLoading = false
                rewardedAd = null
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdShown: () -> Unit = {},
        onAdDismissed: () -> Unit = {}
    ) {
        val prefs = AppPreferences.getInstance(activity)

         if (!isRewardedEnabled) return
        if (prefs.isProUser) return
        if (!isInternetAvailable(activity)) return

        val isAdReady = rewardedAd != null
        val dialogDuration = if (isAdReady) 1000L else 3000L
        if (activity.isValid()){
            AdLoadingDialogManager.show(activity, dialogDuration) {
                if (rewardedAd != null && activity.isValid())  {
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Ad shown")
                            AdStateController.isInterstitialShowing = true

                            onAdShown()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Ad dismissed")
                            AdStateController.isInterstitialShowing = false

                            rewardedAd = null
                            onAdDismissed()
                            loadAd(activity) // preload next
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Failed to show ad: ${adError.message}")
                            AdStateController.isInterstitialShowing = false

                            rewardedAd = null
                            loadAd(activity)
                        }
                    }

                    if (activity.isValid()){
                        rewardedAd?.show(activity) { rewardItem: RewardItem ->
                            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                            onRewardEarned()
                        }
                    }
                } else {
                    Log.w(TAG, "Rewarded ad not ready")
                    if (activity.isValid()){
                        loadAd(activity)
                    }
                    AdStateController.isInterstitialShowing = false

                }
            }
        }


    }

    private fun isInternetAvailable(context: Activity): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}