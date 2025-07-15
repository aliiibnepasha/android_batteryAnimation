package com.lowbyte.battery.animation.ads

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

object BannerAdHelper {

    private const val TAG = "AdHelperBanner"

    private val adViewMap = mutableMapOf<ViewGroup, AdManagerAdView>()
    private val bannerStateMap = mutableMapOf<ViewGroup, BannerState>()

    private data class BannerState(
        var isLoading: Boolean = false,
        var isLoaded: Boolean = false
    )

    fun loadBannerAd(
        context: Context,
        container: ViewGroup,
        bannerAdId: String,
        isCollapsable: Boolean,
        isProUser: Boolean,
        remoteConfig: Boolean,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {
        val shimmer = container.findViewById<ViewGroup>(R.id.shimmerBanner)
        val adPlaceholder = container.findViewById<ViewGroup>(R.id.bannerAdContainer)

        // Skip ad load if not eligible
        if (isProUser || !isInternetAvailable(context) || !remoteConfig) {
            Log.d(TAG, "Ad skipped: Pro user / no internet / remoteConfig off")
            container.visibility = View.GONE
            shimmer?.visibility = View.GONE
            adPlaceholder?.removeAllViews()
            return
        }
        // Prevent multiple simultaneous loads
        val state = bannerStateMap[container] ?: BannerState()
        if (state.isLoading || state.isLoaded) {
            Log.d(TAG, "Banner already loading or loaded")
            return
        }

        state.isLoading = true
        bannerStateMap[container] = state
        shimmer?.visibility = View.VISIBLE
        container.visibility = View.VISIBLE

        val adWidthPixels = Resources.getSystem().displayMetrics.widthPixels
        val adWidth = (adWidthPixels / Resources.getSystem().displayMetrics.density).toInt()
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

        val adView = AdManagerAdView(context).apply {
            adUnitId = bannerAdId
            setAdSize(adSize)
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner loaded")
                shimmer?.visibility = View.GONE
                container.visibility = View.VISIBLE
                state.isLoading = false
                state.isLoaded = true
                bannerStateMap[container] = state
                adViewMap[container] = adView
                onAdLoaded?.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Banner failed: ${adError.message}")
                shimmer?.visibility = View.GONE
                container.visibility = View.GONE
                state.isLoading = false
                state.isLoaded = false
                bannerStateMap[container] = state
                adViewMap.remove(container)
                onAdFailed?.invoke()
            }

            override fun onAdClicked() {
                FirebaseAnalyticsUtils.logClickEvent(context, "ad_banner_clicked")
            }

            override fun onAdImpression() {
                FirebaseAnalyticsUtils.logClickEvent(context, "ad_banner_impression")
            }
        }

        adPlaceholder?.removeAllViews()
        adPlaceholder?.addView(adView)

        val adRequest = if (isCollapsable) {
            Log.d(TAG, "Loading collapsable banner")
            val extras = Bundle().apply { putString("collapsible", "bottom") }
            AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
        } else {
            Log.d(TAG, "Loading standard banner")
            AdRequest.Builder().build()
        }

        adView.loadAd(adRequest)
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun destroyBanner(container: ViewGroup) {
        adViewMap[container]?.destroy()
        adViewMap.remove(container)
        bannerStateMap.remove(container)
    }

    fun resetBanner(container: ViewGroup) {
        bannerStateMap[container]?.isLoaded = false
    }
}