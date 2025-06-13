package com.lowbyte.battery.animation.main.view_all

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lowbyte.battery.animation.databinding.FragmentViewAllWidgetsBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getTabTitlesWidget
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewAllWidgetsFragment : Fragment() {

    private lateinit var binding: FragmentViewAllWidgetsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllWidgetsBinding.inflate(inflater, container, false)

        // Log screen view
        FirebaseAnalyticsUtils.logScreenView(this, "ViewAllWidgetsFragment")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val tabTitles = getTabTitlesWidget(requireContext())

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabTitles.size
            override fun createFragment(position: Int): Fragment {
                return ViewPagerWidgetItemFragment.newInstance(position)
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]

            // Optional: log tab switch interaction
            tab.view.setOnClickListener {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "widget_tab_selected",
                    mapOf("tab_title" to tabTitles[position])
                )
            }
        }.attach()
    }
}