package com.lowbyte.battery.animation.main.view_all

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.FragmentViewAllWidgetsBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.getTabTitlesWidget


class ViewAllWidgetsFragment : Fragment() {
    private lateinit var binding: FragmentViewAllWidgetsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentViewAllWidgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = getTabTitlesWidget(requireContext()).size

            override fun createFragment(position: Int): Fragment {
                return ViewPagerWidgetItemFragment.newInstance(position)
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getTabTitlesWidget(requireContext())[position]
        }.attach()
    }

}