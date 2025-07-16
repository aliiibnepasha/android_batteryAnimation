package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.*
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.utils.AnimationUtils.isValid
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils.logPaidEvent

abstract class BaseNativeAdHelper(
    protected val context: Activity,
    protected val adId: String,
    protected val showAdRemoteFlag: Boolean,
    protected val isProUser: Boolean,
    protected val onAdLoaded: (() -> Unit)? = null,
    protected val onAdFailed: (() -> Unit)? = null,
    protected val adContainer: FrameLayout? = null
) {

     var nativeAd: NativeAd? = null

    protected abstract val logTag: String

    init {
        if (context.isValid()){
            if (!isInternetAvailable(context)) {
                Log.d(logTag, "No internet — skipping native ad.")
                hideAdView()
                onAdFailed?.invoke()
            } else if (!showAdRemoteFlag || isProUser) {
                Log.d(logTag, " Base Pro user or remote flag disabled — skipping ad.")
                hideAdView()
                onAdFailed?.invoke()
            } else {
                showShimmer()
                loadAd()
            }
        }
    }

    private fun loadAd() {
        Log.d(logTag, "Loading native ad: $adId")
        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                if (context.isValid()){
                    nativeAd = ad
                    Log.d(logTag, "Native ad loaded")
                    onAdLoaded?.invoke()
                    adContainer?.let { showAdInView(it, ad) }
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (context.isValid()){
                        Log.e(logTag, "Native ad failed: ${error.message}")
                        hideAdView()
                        onAdFailed?.invoke()
                    }
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().setRequestCustomMuteThisAd(false).build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun showAdInView(container: FrameLayout, ad: NativeAd? = nativeAd) {
        if (ad == null) {
            Log.w(logTag, "No ad to show.")
            return
        }

        val inflater = LayoutInflater.from(context)
        val adView = inflater.inflate(R.layout.view_native_widget_dark, container, false) as NativeAdView

        adView.findViewById<ShimmerFrameLayout?>(R.id.shimmerNativeDark)?.let {
            it.visibility = View.GONE
            it.stopShimmer()
        }

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        adView.mediaView = adView.findViewById(R.id.ad_media)

        (adView.headlineView as? TextView)?.text = ad.headline
        (adView.bodyView as? TextView)?.text = ad.body ?: "Today"
        (adView.callToActionView as? Button)?.text = ad.callToAction
        (adView.advertiserView as? TextView)?.text = ad.advertiser ?: "Sponsored"

        ad.icon?.drawable?.let {
            (adView.iconView as? ImageView)?.setImageDrawable(it)
        } ?: run {
            adView.iconView?.visibility = View.GONE
        }

        ad.mediaContent?.let {
            adView.mediaView?.mediaContent = it
        }

        adView.setNativeAd(ad)

        container.removeAllViews()
        container.addView(adView)
    }

    private fun showShimmer() {
        if (context.isValid()){
            adContainer?.let {
                val shimmer = LayoutInflater.from(context).inflate(R.layout.view_native_widget_shimmer, it, false)
                it.removeAllViews()
                it.addView(shimmer)
            }
        }
    }

    private fun hideAdView() {
        if (context.isValid()){
            adContainer?.visibility = View.GONE
            adContainer?.removeAllViews()
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun destroy() {
        if (context.isValid()){
            nativeAd?.destroy()
            nativeAd = null
        }
    }
}
