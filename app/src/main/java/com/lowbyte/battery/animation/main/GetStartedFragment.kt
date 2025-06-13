package com.lowbyte.battery.animation.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.FragmentGetStartedBinding
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class GetStartedFragment : Fragment(R.layout.fragment_get_started) {

    private lateinit var binding: FragmentGetStartedBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGetStartedBinding.bind(view)

        // Log screen view
        FirebaseAnalyticsUtils.logScreenView(this, "get_started_screen")

        binding.btnNext.setOnClickListener {
            // Log click event
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "get_started_next_click"
            )
             findNavController().navigate(R.id.action_getStarted_to_main)

        }
    }
}