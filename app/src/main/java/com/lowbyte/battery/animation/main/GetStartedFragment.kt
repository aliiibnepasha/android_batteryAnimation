package com.lowbyte.battery.animation.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.FragmentGetStartedBinding

class GetStartedFragment : Fragment(R.layout.fragment_get_started)  {
    private lateinit var binding: FragmentGetStartedBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentGetStartedBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)


        binding.btnNext.setOnClickListener {
            AdManager.showInterstitialAd(requireActivity()) {
                findNavController().navigate(R.id.action_getStarted_to_main)
            }
        }
    }


}