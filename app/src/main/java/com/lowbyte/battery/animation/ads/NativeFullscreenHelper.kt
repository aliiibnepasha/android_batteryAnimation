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

class NativeFullscreenHelper(
    private val context: Activity,
    private val adId: String,
    private val showAdRemoteFlag: Boolean,
    private val isProUser: Boolean,
    private val onAdLoaded: (() -> Unit)? = null,
    private val onAdFailed: (() -> Unit)? = null,
    private val onAdClicked: (() -> Unit)? = null,
    private val adContainer: LinearLayout? = null // Optional: null = just preload
)
{

    companion object {
        private const val TAG = "AdHelperNativeWidget"
    }

    private var nativeAd: NativeAd? = null

    init {
        if (context.isValid()){
            if (!isInternetAvailable(context)) {
                Log.d(TAG, "No internet — skipping native ad.")
                hideAdView()
                onAdFailed?.invoke()
            } else if (!showAdRemoteFlag || isProUser) {
                Log.d(TAG, " Native full Pro user or remote flag disabled — skipping ad.")
                hideAdView()
                onAdFailed?.invoke()
            } else {
                showShimmer()
                loadAd()
            }
        }
    }

    private fun loadAd() {
        Log.d(TAG, "Loading native ad: $adId")
        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                if (context.isValid()){
                    nativeAd = ad
                    Log.d(TAG, "Native ad loaded")
                    onAdLoaded?.invoke()
                    adContainer?.let { showAdInView(it, ad) }
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (context.isValid()){
                        Log.e(TAG, "Native ad failed: ${error.message}")
                        hideAdView()
                        onAdFailed?.invoke()
                    }
                }

                override fun onAdClicked() {
                    onAdClicked?.invoke()
                    super.onAdClicked()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder()
                .setRequestCustomMuteThisAd(false)
                .build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun showAdInView(container: LinearLayout, ad: NativeAd? = nativeAd) {
        if (ad == null) {
            Log.w(TAG, "No ad to show.")
            return
        }

        val inflater = LayoutInflater.from(context)
        val adView = inflater.inflate(R.layout.view_native_fullscreen_dark, container, false) as NativeAdView

        adView.findViewById<ShimmerFrameLayout?>(R.id.shimmerNativeDark)?.let {
            it.visibility = View.GONE
            it.stopShimmer()
        }

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.mediaView = adView.findViewById(R.id.ad_media)

        (adView.headlineView as? TextView)?.text = ad.headline
        (adView.bodyView as? TextView)?.text = ad.body ?: "Today"
        (adView.callToActionView as? Button)?.text = ad.callToAction

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
                val shimmer = LayoutInflater.from(context)
                    .inflate(R.layout.view_native_fullscreen_shimmer, it, false)
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