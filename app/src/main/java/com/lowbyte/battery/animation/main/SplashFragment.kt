package com.lowbyte.battery.animation.main

import android.animation.ObjectAnimator
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lowbyte.battery.animation.BuildConfig
import com.lowbyte.battery.animation.MyApplication
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.GoogleMobileAdsConsentManager
import com.lowbyte.battery.animation.ads.SplashBannerHelper
import com.lowbyte.battery.animation.databinding.FragmentSplashBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenSplashId
import com.lowbyte.battery.animation.utils.AnimationUtils.isBannerSplashEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenSplashEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private var shouldNavigateAfterResume = false
    private lateinit var preferences: AppPreferences
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private var bannerLoaded = false
    private var interstitialLoaded = false
    private var adsHandled = false
    private var hasResumed = false
    private val splashTimeout = 8000L
    private val progressDuration = 3000L

    private val handler = Handler(Looper.getMainLooper())
    private var resumeTimeoutRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())
        MyApplication.enableOpenAd(false)
        FirebaseAnalyticsUtils.logScreenView(this, "splash_screen")

        if (!preferences.isProUser) {
            googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(requireContext())
            googleMobileAdsConsentManager.gatherConsent(requireActivity()) { consentError ->
                if (!isAdded) return@gatherConsent
                if (consentError != null) {
                    Log.w("Consent", "${consentError.errorCode}: ${consentError.message}")
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "ads_consent_fail")
                } else {
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "ads_consent_success")
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("backPress","closedonSplash")
        }


        hasResumed = false

        SplashBannerHelper.loadInlineAdaptiveBanner(
            context = requireContext(),
            container = binding.bannerAdSplash,
            isProUser = preferences.isProUser,
            maxHeightDp = 150,
            onAdLoaded = {
                if (isAdded) {
                    bannerLoaded = true
                    handleAdEvents()
                }

            },
            onAdFailed = {
                if (isAdded) {
                    bannerLoaded = true
                    handleAdEvents()
                }

            },
           remoteConfig = isBannerSplashEnabled
        )

        AdManager.loadInterstitialAd(requireContext(), getFullscreenSplashId(),isFullscreenSplashEnabled)

        // Simulate loading time for interstitial
        handler.postDelayed({
            if (isAdded){
                interstitialLoaded = true
                handleAdEvents()
            }

        }, 3000)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        hasResumed = true

        if (shouldNavigateAfterResume && !adsHandled) {
            adsHandled = true
            showProgressAndNavigate()
            return
        }

        handleAdEvents()

        resumeTimeoutRunnable = Runnable {
            if (!adsHandled) {
                if (hasResumed) {
                    adsHandled = true
                    showProgressAndNavigate()
                } else {
                    shouldNavigateAfterResume = true // wait for resume
                }
            }
        }
        if (!isInternetAvailable(requireContext()) || preferences.isProUser){
            handler.postDelayed(resumeTimeoutRunnable!!, 3000)
            showProgressAndNavigate()
        }else{
            handler.postDelayed(resumeTimeoutRunnable!!, splashTimeout)
        }

    }
    override fun onPause() {
        super.onPause()
        hasResumed = false
        resumeTimeoutRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    private fun handleAdEvents() {
        if ((bannerLoaded || preferences.isProUser) && (interstitialLoaded || preferences.isProUser)) {
            if (!adsHandled && hasResumed) {
                adsHandled = true
                showProgressAndNavigate()
            }
        }
    }

    private fun showProgressAndNavigate() {
        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, 100)
        animator.duration = progressDuration
        animator.start()

        handler.postDelayed({

            if (isAdded && findNavController().currentDestination?.id == R.id.splashFragment) {
                preferences = AppPreferences.getInstance(requireContext())
                val destination = if (preferences.isFirstRun) {
                    R.id.action_splash_to_language
                } else {
                    R.id.action_splash_to_main
                }
                if (isAdded && findNavController().currentDestination?.id == R.id.splashFragment) {
                    if (preferences.isProUser){
                        preferences.serviceRunningFlag = false
                        preferences.isFirstRun = false
                        findNavController().navigate(destination)
                    }else{
                        findNavController().navigate(R.id.action_splash_to_pro)
                    }
                } else {
                    Log.w("Navigation", "Attempted to navigate from incorrect fragment")
                }



            }
        }, progressDuration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        MyApplication.enableOpenAd(true)
        _binding = null
    }
}