package com.lowbyte.battery.animation.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.NativeFullscreenHelper
import com.lowbyte.battery.animation.databinding.FragmentFullscreenNativeAdBinding
import com.lowbyte.battery.animation.main.shared.SharedIntroViewModel
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeInsideId
import com.lowbyte.battery.animation.utils.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class NativeAdFragment : Fragment(R.layout.fragment_fullscreen_native_ad) {
    private var _binding: FragmentFullscreenNativeAdBinding? = null



    private val binding get() = _binding!!
    val sharedViewModel: SharedIntroViewModel by activityViewModels()

    private var nativeAdHelper: NativeFullscreenHelper? = null
    private lateinit var preferences: AppPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentFullscreenNativeAdBinding.bind(view)
        preferences = AppPreferences.getInstance(requireContext())

        binding.progressBar.max = 100
        binding.progressBar.progress = 0

// Animate progress over 3 seconds
        val duration = 3000L
        val interval = 30L
        val steps = (duration / interval).toInt()

        lifecycleScope.launch {
            repeat(steps) { i ->
                delay(interval)
                binding.progressBar.progress = ((i + 1) * 100) / steps
            }
            binding.closeIcon.visibility = View.VISIBLE
            binding.progressContainer.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }

        binding.closeIcon.setOnClickListener {
            Log.d("SharedVM", "Child event: ClickedClose")
            sharedViewModel.childEvent.value = "ClickedClose"
        }
        nativeAdHelper = NativeFullscreenHelper(
            context = requireContext(),
            adId = getNativeInsideId(), // Replace with your ad unit ID
            showAdRemoteFlag = true,  // From remote config or your logic
            isProUser = preferences.isProUser,        // Check from your user settings
            onAdLoaded = {
                Log.d("Ad", "Native ad loaded successfully")
            },
            onAdFailed = {
                Log.d("Ad", "Failed to load native ad")
            },
            adContainer = binding.nativeFullscreen   // Optional: Pass only if you want to show immediately
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}