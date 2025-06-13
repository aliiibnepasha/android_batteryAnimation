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

    private lateinit var preferences: AppPreferences
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private var bannerLoaded = false
    private var interstitialLoaded = false
    private var adsHandled = false
    private var hasResumed = false
    private val splashTimeout = 15000L
    private val progressDuration = 3000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())
        MyApplication.enableOpenAd(false) // To disable
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

        AdManager.loadInterstitialAd(requireContext(),getFullscreenSplashId())
        Handler(Looper.getMainLooper()).postDelayed({
            interstitialLoaded = true
            handleAdEvents()
        }, 3000)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        hasResumed = true
        handleAdEvents()

        Handler(Looper.getMainLooper()).postDelayed({
            if (!adsHandled && hasResumed) {
                adsHandled = true
                showProgressAndNavigate()
            }
        }, splashTimeout)
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

        Handler(Looper.getMainLooper()).postDelayed({
            if (preferences.isFirstRun) {
                preferences.isFirstRun = false
                findNavController().navigate(R.id.action_splash_to_language)
            } else {
                AdManager.showInterstitialAd(requireActivity(), false) {
                    findNavController().navigate(R.id.action_splash_to_language)
                }
            }
        }, progressDuration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MyApplication.enableOpenAd(true) // To disable
        _binding = null
    }
}
