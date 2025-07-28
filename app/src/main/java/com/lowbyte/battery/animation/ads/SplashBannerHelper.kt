package com.lowbyte.battery.animation.ads

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils


//object SplashBannerHelper {
//
//    private const val TAG = "SplashBannerHelper"
//
//    fun loadInlineAdaptiveBanner(
//        context: Context,
//        container: ViewGroup,
//        isProUser: Boolean,
//        remoteConfig: Boolean,
//        maxHeightDp: Int? = 150,
//        onAdLoaded: (() -> Unit)? = null,
//        onAdFailed: (() -> Unit)? = null
//    ) {
//        val shimmer = container.findViewById<ShimmerFrameLayout?>(R.id.shimmerSplashBanner)
//        val adPlaceholder = container.findViewById<ViewGroup?>(R.id.inlineAdSplashContainer)
//
//        if (isProUser || !isInternetAvailable(context) || !remoteConfig) {
//            Log.d(TAG, "Pro user — hiding splash banner")
//            shimmer?.visibility = View.GONE
//            adPlaceholder?.removeAllViews()
//            container.visibility = View.GONE
//            return
//        }
//
//        shimmer?.visibility = View.VISIBLE
//        container.visibility = View.VISIBLE
//
//        val adWidthPixels = Resources.getSystem().displayMetrics.widthPixels
//        val adWidthDp = (adWidthPixels / Resources.getSystem().displayMetrics.density).toInt()
//        Log.d(TAG, "Calculated ad width dp: $adWidthDp, height: ${maxHeightDp ?: 50}")
//
//        val adSize = AdSize.getInlineAdaptiveBannerAdSize(adWidthDp, maxHeightDp ?: 50)
//
//        val adView = AdManagerAdView(context).apply {
//            adUnitId = getBannerSplashId()
//            setAdSize(adSize)
//        }
//
//        adView.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                Log.d(TAG, "Splash banner loaded")
//                shimmer?.visibility = View.GONE
//                container.visibility = View.VISIBLE
//                onAdLoaded?.invoke()
//            }
//
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                Log.e(TAG, "Splash banner failed to load: ${adError.message}")
//                shimmer?.visibility = View.GONE
//                container.visibility = View.GONE
//                onAdFailed?.invoke()
//            }
//
//            override fun onAdClicked() {
//                FirebaseAnalyticsUtils.logClickEvent(context, "inline_ad_banner_clicked")
//            }
//
//            override fun onAdImpression() {
//                FirebaseAnalyticsUtils.logClickEvent(context, "inline_ad_banner_impression")
//            }
//        }
//
//        adPlaceholder?.removeAllViews()
//        adPlaceholder?.addView(adView)
//
//        val adRequest = AdRequest.Builder().build()
//        Log.d(TAG, "Requesting splash ad...")
//        adView.loadAd(adRequest)
//    }
//
//    private fun isInternetAvailable(context: Context): Boolean {
//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = cm.activeNetwork ?: return false
//        val capabilities = cm.getNetworkCapabilities(network) ?: return false
//        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//    }
//}