package com.lowbyte.battery.animation.ads

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.utils.AnimationUtils.getBannerId
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

object BannerAdHelper {

    private const val TAG = "BannerAdHelper"

    fun loadBannerAd(
        context: Context,
        container: ViewGroup,
        isProUser: Boolean,
        remoteConfig: Boolean,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {
        val shimmer = container.findViewById<ViewGroup>(R.id.shimmerBanner)
        val adPlaceholder = container.findViewById<ViewGroup>(R.id.bannerAdContainer)

        if (isProUser || !isInternetAvailable(context) || !remoteConfig) {
            Log.d(TAG, "User is Pro — Banner ad will not be shown")
            container.visibility = View.GONE
            shimmer?.visibility = View.GONE
            adPlaceholder?.removeAllViews()
            return
        }

        shimmer?.visibility = View.VISIBLE
        container.visibility = View.VISIBLE

        val adWidthPixels = Resources.getSystem().displayMetrics.widthPixels
        val adWidth = (adWidthPixels / Resources.getSystem().displayMetrics.density).toInt()
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

        val adView = AdManagerAdView(context).apply {
            adUnitId = getBannerId()
            setAdSize(adSize)
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded successfully")
                shimmer?.visibility = View.GONE
                container.visibility = View.VISIBLE
                onAdLoaded?.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Banner ad failed to load: ${adError.message}")
                shimmer?.visibility = View.GONE
                container.visibility = View.GONE
                onAdFailed?.invoke()
            }

            override fun onAdClicked() {
                Log.d(TAG, "Banner ad clicked")
                FirebaseAnalyticsUtils.logClickEvent(context, "ad_banner_clicked")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Banner ad impression recorded")
                FirebaseAnalyticsUtils.logClickEvent(context, "ad_banner_impression")
            }
        }

        adPlaceholder?.removeAllViews()
        adPlaceholder?.addView(adView)

        val adRequest = AdRequest.Builder().build()
        Log.d(TAG, "Requesting banner ad with ad unit: ${adView.adUnitId}")
        adView.loadAd(adRequest)
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}