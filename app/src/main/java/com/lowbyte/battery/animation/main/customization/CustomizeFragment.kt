package com.lowbyte.battery.animation.main.customization

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.activity.StatusBarCustomizeActivity
import com.lowbyte.battery.animation.activity.StatusBarGestureActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.FragmentCustomizeBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenHome2Id
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenGestureEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenStatusEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.isNativeStatusEnabled
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class CustomizeFragment : Fragment() {

    private var _binding: FragmentCustomizeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomizeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        AdManager.loadInterstitialAd(requireActivity(),getFullscreenHome2Id(),isFullscreenStatusEnabled)

        // Log screen view event
        FirebaseAnalyticsUtils.logScreenView(this, "CustomizeFragment")

        // Status bar customization click
        binding.menuStatusBar.setOnClickListener {
            AdManager.setCooldownEnabledForLoad(false)
            AdManager.setCooldownEnabledForShow(false)
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_status_customize")
            AdManager.showInterstitialAd(requireActivity(), isNativeStatusEnabled,true) {
                startActivity(Intent(requireContext(), StatusBarCustomizeActivity::class.java))
                AdManager.setCooldownEnabledForLoad(true)
                AdManager.setCooldownEnabledForShow(true)
            }
        }

        // Gesture customization click
        binding.menuGesture.setOnClickListener {
            AdManager.setCooldownEnabledForLoad(false)
            AdManager.setCooldownEnabledForShow(false)
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_gesture_customize")
            AdManager.showInterstitialAd(requireActivity(), isFullscreenGestureEnabled,true) {
                startActivity(Intent(requireContext(), StatusBarGestureActivity::class.java))
                AdManager.setCooldownEnabledForLoad(true)
                AdManager.setCooldownEnabledForShow(true)
            }
        }

        return root
    }

    override fun onResume() {

        super.onResume()
    }
    override fun onDestroyView() {
        AdManager.setCooldownEnabledForLoad(true)
        AdManager.setCooldownEnabledForShow(true)
        super.onDestroyView()
        _binding = null
    }
}