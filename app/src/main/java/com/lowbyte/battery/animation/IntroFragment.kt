package com.lowbyte.battery.animation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.lowbyte.battery.animation.adapter.IntroAdapter
import com.lowbyte.battery.animation.databinding.FragmentIntroBinding
import com.lowbyte.battery.animation.model.IntroItem

class IntroFragment : Fragment(R.layout.fragment_intro) {

    private lateinit var binding: FragmentIntroBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentIntroBinding.bind(view)

        val items = listOf(
            IntroItem(getString(R.string.intro_title_1), getString(R.string.intro_description_1), R.drawable.intro_1),
            IntroItem(getString(R.string.intro_title_2), getString(R.string.intro_description_2), R.drawable.intro_2),
           // IntroItem(getString(R.string.welcome), getString(R.string.intro_description_3), R.drawable.intro_3)
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
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.btnNext.text = if (position == 1) {
                    getString(R.string.next)
                } else {
                    getString(R.string.next)
                }
            }
        })
    }
}