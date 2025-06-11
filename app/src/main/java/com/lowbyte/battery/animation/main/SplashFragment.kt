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


        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(requireContext())
        googleMobileAdsConsentManager.gatherConsent(requireActivity()) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                Log.w("TAG", "${consentError.errorCode}: ${consentError.message}")
            }

        }


        val progressBar = binding.progressBar
        progressBar.max = 100

        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
        animator.duration = if (preferences.isFirstRun) 5000 else 8000 // Adjust duration as needed
        animator.start()
        AdManager.loadInterstitialAd(requireContext())

        Handler(Looper.getMainLooper()).postDelayed({
            if (preferences.isFirstRun) {
                preferences.isFirstRun = false
                findNavController().navigate(R.id.action_splash_to_language)
            }else{
                AdManager.showInterstitialAd(requireActivity()) {
                    findNavController().navigate(R.id.action_splash_to_main)

                }

            }
        }, animator.duration)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}