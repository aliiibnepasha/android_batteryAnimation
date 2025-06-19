package com.lowbyte.battery.animation.main

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lowbyte.battery.animation.MyApplication
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.GoogleMobileAdsConsentManager
import com.lowbyte.battery.animation.ads.SplashBannerHelper
import com.lowbyte.battery.animation.databinding.FragmentSplashBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenSplashId
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
    private val splashTimeout = 15000L
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
                if (consentError != null) {
                    Log.w("Consent", "${consentError.errorCode}: ${consentError.message}")
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "ads_consent_fail")
                } else {
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "ads_consent_success")
                }
            }
        }

        hasResumed = false

        SplashBannerHelper.loadInlineAdaptiveBanner(
            context = requireContext(),
            container = binding.bannerAdSplash,
            isProUser = preferences.isProUser,
            maxHeightDp = 150,
            onAdLoaded = {
                bannerLoaded = true
                handleAdEvents()
            },
            onAdFailed = {
                bannerLoaded = true
                handleAdEvents()
            }
        )

        AdManager.loadInterstitialAd(requireContext(), getFullscreenSplashId())

        // Simulate loading time for interstitial
        handler.postDelayed({
            interstitialLoaded = true
            handleAdEvents()
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
        handler.postDelayed(resumeTimeoutRunnable!!, splashTimeout)
    }
    override fun onPause() {
        super.onPause()
        hasResumed = false
        resumeTimeoutRunnable?.let { handler.removeCallbacks(it) }
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
            val destination = if (preferences.isFirstRun) {
                preferences.serviceRunningFlag = false
                preferences.isFirstRun = true
                R.id.action_splash_to_language
            } else {
                R.id.action_splash_to_main
            }

            AdManager.showInterstitialAd(requireActivity(), false) {
                if (isAdded) findNavController().navigate(destination)
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