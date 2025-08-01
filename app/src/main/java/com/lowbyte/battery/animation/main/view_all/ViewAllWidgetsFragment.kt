package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.FragmentViewAllWidgetsBinding
import com.lowbyte.battery.animation.service.BatteryWidgetForegroundService
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.getTabTitlesWidget
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewAllWidgetsFragment : Fragment() {

    private lateinit var binding: FragmentViewAllWidgetsBinding
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        preferences = AppPreferences.getInstance(requireContext())
        binding = FragmentViewAllWidgetsBinding.inflate(inflater, container, false)

        // Log screen view
        FirebaseAnalyticsUtils.logScreenView(this, "ViewAllWidgetsFragment")

        binding.switchEnableWidgetService.setOnCheckedChangeListener { _, isChecked ->
            val context = requireContext()
            val preferences = AppPreferences.getInstance(context)

            if (isChecked) {
                if (!preferences.serviceRunningFlag) {
                    requireActivity().window.decorView.post {
                        val serviceIntent = Intent(requireContext(), BatteryWidgetForegroundService::class.java).apply {
                            action = BatteryWidgetForegroundService.ACTION_START_SERVICE
                        }
                        ContextCompat.startForegroundService(requireContext(), serviceIntent)
                    }
                }
            } else {
                val stopIntent = Intent(requireContext(), BatteryWidgetForegroundService::class.java).apply {
                    action = BatteryWidgetForegroundService.ACTION_STOP_SERVICE
                }
                ContextCompat.startForegroundService(requireContext(),stopIntent)

            }

            // Log event to Firebase
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "toggle_widget_enabled",
                mapOf("enabled" to isChecked.toString())
            )
        }


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

    override fun onResume() {
        super.onResume()
        binding.switchEnableWidgetService.isChecked = preferences.serviceRunningFlag

    }
}