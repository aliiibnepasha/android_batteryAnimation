package com.lowbyte.battery.animation.ads

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.lowbyte.battery.animation.utils.AnimationUtils.getBannerSplashId
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

object SplashBannerHelper {

    private const val TAG = "SplashBannerHelper"

    fun loadInlineAdaptiveBanner(
        context: Context,
        container: ViewGroup,
        isProUser: Boolean,
        maxHeightDp: Int? = null,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {
        if (isProUser) {
            Log.d(TAG, "Pro user — skipping banner ad display")
            container.visibility = ViewGroup.GONE
            return
        }

        Log.d(TAG, "Loading inline adaptive banner ad...")

        container.visibility = ViewGroup.VISIBLE

        val adWidthPixels = Resources.getSystem().displayMetrics.widthPixels
        val adWidthDp = (adWidthPixels / Resources.getSystem().displayMetrics.density).toInt()
        Log.d(TAG, "Calculated ad width in dp: $adWidthDp")

        val adSize = if (maxHeightDp != null) {
            Log.d(TAG, "Using custom max height: ${maxHeightDp}dp")
            AdSize.getInlineAdaptiveBannerAdSize(adWidthDp, maxHeightDp)
        } else {
            Log.d(TAG, "Using default max height: 50dp")
            AdSize.getInlineAdaptiveBannerAdSize(adWidthDp, 50)
        }

        val adView = AdManagerAdView(context).apply {
            adUnitId = getBannerSplashId()
            setAdSize(adSize)
        }

        Log.d(TAG, "Ad unit ID: ${adView.adUnitId}")
        Log.d(TAG, "Ad size set: ${adSize.width}x${adSize.height}")

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded successfully")
                container.visibility = ViewGroup.VISIBLE
                onAdLoaded?.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Banner ad failed to load: ${adError.message}")
                container.visibility = ViewGroup.GONE
                onAdFailed?.invoke()
            }

            override fun onAdClicked() {
                Log.d(TAG, "Banner ad clicked")
                FirebaseAnalyticsUtils.logClickEvent(context, "inline_ad_banner_clicked")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Banner ad impression recorded")
                FirebaseAnalyticsUtils.logClickEvent(context, "inline_ad_banner_impression")
            }
        }

        container.removeAllViews()
        container.addView(adView)

        val adRequest = AdRequest.Builder().build()
        Log.d(TAG, "Requesting ad now...")
        adView.loadAd(adRequest)
    }
}