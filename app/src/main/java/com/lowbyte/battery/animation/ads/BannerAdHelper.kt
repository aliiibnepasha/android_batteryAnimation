package com.lowbyte.battery.animation.ads

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.lowbyte.battery.animation.utils.AnimationUtils.getBannerId
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

object BannerAdHelper {

    private const val TAG = "BannerAdHelper"

    fun loadBannerAd(
        context: Context,
        container: ViewGroup,
        isProUser: Boolean,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {
        if (isProUser) {
            Log.d(TAG, "User is Pro — Banner ad will not be shown")
            container.visibility = ViewGroup.GONE
            return
        }

        Log.d(TAG, "Loading anchored adaptive banner ad...")

        container.visibility = ViewGroup.VISIBLE

        val adWidthPixels = Resources.getSystem().displayMetrics.widthPixels
        val adWidth = (adWidthPixels / Resources.getSystem().displayMetrics.density).toInt()

        Log.d(TAG, "Calculated ad width in dp: $adWidth")

        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

        val adView = AdManagerAdView(context).apply {
            adUnitId = getBannerId()
            setAdSize(adSize)
        }

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
                FirebaseAnalyticsUtils.logClickEvent(context, "ad_banner_clicked")
            }

            override fun onAdImpression() {
                Log.d(TAG, "Banner ad impression recorded")
                FirebaseAnalyticsUtils.logClickEvent(context, "ad_banner_impression")
            }
        }

        container.removeAllViews()
        container.addView(adView)

        val adRequest = AdRequest.Builder().build()
        Log.d(TAG, "Requesting banner ad with ad unit: ${adView.adUnitId}")
        adView.loadAd(adRequest)
    }
}