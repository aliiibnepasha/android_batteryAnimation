package com.lowbyte.battery.animation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lowbyte.battery.animation.adapter.IntroAdapter
import com.lowbyte.battery.animation.databinding.FragmentIntroBinding
import com.lowbyte.battery.animation.model.IntroItem

class IntroFragment : Fragment(R.layout.fragment_intro) {

    private lateinit var binding: FragmentIntroBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentIntroBinding.bind(view)

        val items = listOf(
            IntroItem("Unique Status Bar", "Emojis, widgets and animation\n" + "to customize your screen", R.drawable.intro_1),
            IntroItem("Easy Dynamic Island", "Setup and display custom notch,\ngestures, styles and more", R.drawable.intro_2),
            IntroItem("Welcome!", "Personalize your screens with unique battery \nemojis, widgets and animations", R.drawable.intro_3)
        )

        binding.viewPager.adapter = IntroAdapter(requireContext(), items)

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < items.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                findNavController().navigate(R.id.action_intro_to_getStarted)
            }
        }
    }
}