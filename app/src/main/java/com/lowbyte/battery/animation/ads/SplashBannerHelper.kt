package com.lowbyte.battery.animation.ads

import android.content.Context
import android.content.res.Resources
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.lowbyte.battery.animation.utils.AnimationUtils.getBannerSplashId
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

object SplashBannerHelper {

    fun loadInlineAdaptiveBanner(
        context: Context,
        container: ViewGroup,
        isProUser: Boolean,
        maxHeightDp: Int? = null,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {
        if (isProUser) {
            container.visibility = ViewGroup.GONE
            return
        }

        container.visibility = ViewGroup.VISIBLE

        val adWidthPixels = Resources.getSystem().displayMetrics.widthPixels
        val adWidthDp = (adWidthPixels / Resources.getSystem().displayMetrics.density).toInt()

        val adSize = if (maxHeightDp != null) {
            AdSize.getInlineAdaptiveBannerAdSize(adWidthDp, maxHeightDp)
        } else {
            AdSize.getInlineAdaptiveBannerAdSize(adWidthDp, 50)
        }

        val adView = AdManagerAdView(context).apply {
            adUnitId = getBannerSplashId()
            setAdSize(adSize)
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                container.visibility = ViewGroup.VISIBLE
                onAdLoaded?.invoke()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                container.visibility = ViewGroup.GONE
                onAdFailed?.invoke()
            }

            override fun onAdClicked() {
                FirebaseAnalyticsUtils.logClickEvent(context, "inline_ad_banner_clicked")
            }

            override fun onAdImpression() {
                FirebaseAnalyticsUtils.logClickEvent(context, "inline_ad_banner_impression")
            }
        }

        container.removeAllViews()
        container.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}