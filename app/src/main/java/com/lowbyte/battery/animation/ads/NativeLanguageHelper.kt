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

class NativeLanguageHelper(
    private val context: Activity?= null,
    private val adId: String,
    private val showAdRemoteFlag: Boolean,
    private val isProUser: Boolean,
    private val onAdLoaded: (() -> Unit)? = null,
    private val onAdFailed: (() -> Unit)? = null,
    private val adContainer: FrameLayout? = null // Optional: null = just preload
) {

    companion object {
        private const val TAG = "NativeLanguageHelper"
    }

    private var nativeAd: NativeAd? = null
    private var adLoader: AdLoader? = null
    private var isDestroyed = false

    init {
        if (context!=null){
            if (!isInternetAvailable(context)) {
                Log.d(TAG, "No internet — skipping native ad.")
                hideAdView()
                onAdFailed?.invoke()
            } else if (!showAdRemoteFlag || isProUser) {
                Log.d(TAG, "Pro user or remote flag disabled — skipping ad.")
                hideAdView()
                onAdFailed?.invoke()
            } else {
                showShimmer()
                loadAd()
            }
        }

    }

    private fun loadAd() {
        if (isDestroyed) return

        adLoader = AdLoader.Builder(context?:return, adId)
            .forNativeAd { ad ->
                if (isDestroyed) {
                    ad.destroy() // prevent leak if ad finishes after destroy
                    return@forNativeAd
                }
                if (!context.isFinishing || !context.isDestroyed){
                    if (nativeAd!=null){
                        nativeAd?.destroy()
                    }
                    nativeAd = ad
                    Log.d(TAG, "Native ad loaded")
                    onAdLoaded?.invoke()
                    adContainer?.let { showAdInView(it, ad) }
                }

            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Native ad failed: ${error.message}")
                    if (!context.isFinishing || !context.isDestroyed){
                        hideAdView()
                        onAdFailed?.invoke()
                    }


                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setRequestCustomMuteThisAd(false)
                    .build()
            )
            .build()

        adLoader?.loadAd(AdRequest.Builder().build())
    }

    fun showAdInView(container: FrameLayout, ad: NativeAd? = nativeAd) {
        if (ad == null) {
            Log.w(TAG, "No ad to show.")
            return
        }

        val inflater = LayoutInflater.from(context)
        val adView = inflater.inflate(R.layout.view_native_language_dark, container, false) as NativeAdView

        val shimmer = adView.findViewById<ShimmerFrameLayout>(R.id.shimmerNativeDark)
        shimmer?.visibility = View.GONE
        shimmer?.stopShimmer()

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        (adView.headlineView as? TextView)?.text = ad.headline
        (adView.bodyView as? TextView)?.text = ad.body ?: "Today"
        (adView.callToActionView as? Button)?.text = ad.callToAction
        (adView.advertiserView as? TextView)?.text = ad.advertiser ?: "Sponsored"

        ad.icon?.drawable?.let {
            (adView.iconView as? ImageView)?.setImageDrawable(it)
        } ?: run {
            adView.iconView?.visibility = View.GONE
        }

        adView.setNativeAd(ad)

        container.removeAllViews()
        container.addView(adView)
    }

    private fun showShimmer() {
        adContainer?.let {
            val shimmer = LayoutInflater.from(context)
                .inflate(R.layout.view_native_language_shimmer, it, false)
            it.removeAllViews()
            it.addView(shimmer)
        }
    }

    private fun hideAdView() {
        adContainer?.visibility = View.GONE
        adContainer?.removeAllViews()
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun destroy() {
        isDestroyed = true
        nativeAd?.destroy()
        nativeAd = null
        adLoader = null
        adContainer?.removeAllViews()
    }
}