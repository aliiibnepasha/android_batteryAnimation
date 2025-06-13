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
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.GoogleMobileAdsConsentManager
import com.lowbyte.battery.animation.databinding.FragmentSplashBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: AppPreferences
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())

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
        } else {
            Log.d("Splash", "Pro user, skipping ad consent flow.")
        }

        animateProgressAndProceed()

        return binding.root
    }

    private fun animateProgressAndProceed() {
        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, 100)
        animator.duration = if (preferences.isFirstRun) 5000 else 8000
        animator.start()

        AdManager.loadInterstitialAd(requireContext())
        FirebaseAnalyticsUtils.logClickEvent(requireContext(), "splash_progress_started", mapOf(
            "first_time" to preferences.isFirstRun.toString(),
            "duration_ms" to animator.duration.toString()
        ))

        Handler(Looper.getMainLooper()).postDelayed({
            if (preferences.isFirstRun) {
                preferences.isFirstRun = false
                FirebaseAnalyticsUtils.logClickEvent(requireContext(), "navigate_language_screen")
                findNavController().navigate(R.id.action_splash_to_language)
            } else {
                FirebaseAnalyticsUtils.logClickEvent(requireContext(), "navigate_main_screen_attempt")
                AdManager.showInterstitialAd(requireActivity()) {
                    FirebaseAnalyticsUtils.logClickEvent(requireContext(), "interstitial_ad_shown_on_splash")
                    findNavController().navigate(R.id.action_splash_to_main)
                }
            }
        }, animator.duration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}