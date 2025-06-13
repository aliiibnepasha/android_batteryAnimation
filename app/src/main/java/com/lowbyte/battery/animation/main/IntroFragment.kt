package com.lowbyte.battery.animation.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.IntroAdapter
import com.lowbyte.battery.animation.databinding.FragmentIntroBinding
import com.lowbyte.battery.animation.model.IntroItem
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class IntroFragment : Fragment(R.layout.fragment_intro) {

    private lateinit var binding: FragmentIntroBinding
    private lateinit var items: List<IntroItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentIntroBinding.bind(view)

        // Log initial screen view
        FirebaseAnalyticsUtils.logScreenView(this, "intro_screen")

        items = listOf(
            IntroItem(getString(R.string.intro_title_1), getString(R.string.intro_description_1), R.drawable.intro_1),
            IntroItem(getString(R.string.intro_title_2), getString(R.string.intro_description_2), R.drawable.intro_2),
            // Add more pages if needed
        )

        binding.viewPager.adapter = IntroAdapter(requireContext(), items)

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "intro_next_click", mapOf("page_index" to currentItem.toString()))

            if (currentItem < items.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "intro_complete_navigation")
                findNavController().navigate(R.id.action_intro_to_getStarted)
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Log each page view
                FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "intro_page_$position")
                binding.btnNext.text = getString(R.string.next)
            }
        })
    }
}