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
import com.lowbyte.battery.animation.utils.AppPreferences

object RewardedAdManager {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private const val TAG = "RewardedAdManager"

    fun loadAd(context: Activity) {
        if (rewardedAd != null || isLoading) return
          if (!isRewardedEnabled) return
        if (AppPreferences.getInstance(context).isProUser) return
        if (!isInternetAvailable(context)) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, getRewardedId(), adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                isLoading = false
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

        AdLoadingDialogManager.show(activity, dialogDuration) {
            if (rewardedAd != null) {
                rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad shown")
                        onAdShown()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Ad dismissed")
                        rewardedAd = null
                        onAdDismissed()
                        loadAd(activity) // preload next
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e(TAG, "Failed to show ad: ${adError.message}")
                        rewardedAd = null
                        loadAd(activity)
                    }
                }

                rewardedAd?.show(activity) { rewardItem: RewardItem ->
                    Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    onRewardEarned()
                }
            } else {
                Log.w(TAG, "Rewarded ad not ready")
                loadAd(activity)
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