package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.lowbyte.battery.animation.R

object NativeLanguageHelper {

    private const val TAG = "AdHelperNativeLanguage"

    private var nativeAdMap :NativeAd? = null

    fun loadAd(
        context: Activity,
        adId: String,
        showAdRemoteFlag: Boolean,
        isProUser: Boolean,
        adContainer: FrameLayout?,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {


        try {
            if (!isInternetAvailable(context)) {
                Log.d(TAG, "No internet — skipping native ad.")
                adContainer?.visibility = View.GONE
                onAdFailed?.invoke()
                return
            }
            if (!showAdRemoteFlag || isProUser) {
                Log.d(TAG, "Pro user or remote config off — skipping native ad.")
                adContainer?.visibility = View.GONE
                onAdFailed?.invoke()
                return
            }
            showShimmer(context, adContainer)

            val adLoader = AdLoader.Builder(context, adId)
                .forNativeAd { ad ->
                    if (context.isFinishing || context.isDestroyed) {
                        adContainer?.removeAllViews()
                        ad.destroy()
                        return@forNativeAd
                    }
                    nativeAdMap?.destroy()
                    nativeAdMap = ad
                    adContainer?.removeAllViews()
                    showAd(context, adContainer, ad)
                    onAdLoaded?.invoke()
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e(TAG, "Native ad failed: ${error.message}")
                        adContainer?.visibility = View.GONE
                        onAdFailed?.invoke()
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun showAd(activity: Activity, container: FrameLayout?, ad: NativeAd? = null) {
        val nativeAd = ad ?: return

        val inflater = LayoutInflater.from(activity)
        val adView = inflater.inflate(R.layout.view_native_language_dark, container, false) as NativeAdView

        val shimmer = adView.findViewById<ShimmerFrameLayout>(R.id.shimmerNativeDark)
        shimmer?.visibility = View.GONE
        shimmer?.stopShimmer()

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        (adView.headlineView as? TextView)?.text = nativeAd.headline
        (adView.bodyView as? TextView)?.text = nativeAd.body ?: "Today"
        (adView.callToActionView as? Button)?.text = nativeAd.callToAction
        (adView.advertiserView as? TextView)?.text = nativeAd.advertiser ?: "Sponsored"

        nativeAd.icon?.drawable?.let {
            (adView.iconView as? ImageView)?.setImageDrawable(it)
        } ?: run {
            adView.iconView?.visibility = View.GONE
        }

        adView.setNativeAd(nativeAd)
        container?.removeAllViews()
        container?.addView(adView)
    }

    private fun showShimmer(context: Context, container: FrameLayout?) {
        container?.let {
            val shimmer = LayoutInflater.from(context).inflate(R.layout.view_native_language_shimmer, it, false)
            it.removeAllViews()
            it.addView(shimmer)
        }
    }

    fun destroy(adId: String) {
        nativeAdMap?.destroy()
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}