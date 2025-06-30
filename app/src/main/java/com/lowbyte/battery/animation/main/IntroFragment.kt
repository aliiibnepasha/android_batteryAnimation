package com.lowbyte.battery.animation.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.IntroAdapter
import com.lowbyte.battery.animation.databinding.FragmentIntroBinding
import com.lowbyte.battery.animation.model.IntroItem
import com.lowbyte.battery.animation.model.SlideType
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils


class IntroFragment : Fragment(R.layout.fragment_intro) {

    private lateinit var binding: FragmentIntroBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d("backPress", "closeBackOnIntro")
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentIntroBinding.bind(view)
        FirebaseAnalyticsUtils.logScreenView(this, "intro_screen")

        val items = listOf(
            IntroItem(getString(R.string.intro_title_1), getString(R.string.intro_description_1), R.drawable.intro_1, SlideType.INTRO),
          //  IntroItem(type = SlideType.NATIVE_AD),
            IntroItem(getString(R.string.intro_title_2), getString(R.string.intro_description_2), R.drawable.intro_2, SlideType.INTRO),
            IntroItem(getString(R.string.welcome), getString(R.string.intro_description_3), R.drawable.intro_3, SlideType.INTRO)
        )

        binding.viewPager.adapter = IntroAdapter(requireActivity(), items)

        binding.btnNext.setOnClickListener {

            val currentItem = binding.viewPager.currentItem
            FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "intro_next_click", mapOf("page_index" to currentItem.toString()))
            if (currentItem < items.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                if (isAdded && findNavController().currentDestination?.id == R.id.introFragment) {
                    findNavController().navigate(R.id.action_intro_to_main)
                }
            }
        }
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "intro_page_$position")

                super.onPageSelected(position)
//                if (position == 2) {
//                    binding.btnNext.text = getString(R.string.getStarted)
//                } else {
//                    binding.btnNext.text = getString(R.string.next)
//                }
                val isLast = position == items.lastIndex
                val isAd = items[position].type == SlideType.NATIVE_AD

                binding.btnNext.visibility = if (isAd) View.GONE else View.VISIBLE
                binding.btnNext.text = if (isLast) getString(R.string.getStarted) else getString(R.string.next)


            }
        })
    }
}
