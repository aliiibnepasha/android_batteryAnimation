package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.databinding.FragmentViewAllEmojiBinding
import com.lowbyte.battery.animation.utils.AppPreferences


class ViewAllEmojiFragment : Fragment() {
    private lateinit var binding: FragmentViewAllEmojiBinding
    private val tabTitles = listOf("All", "Popular", "Cute", "Comic", "Hot")
    private lateinit var preferences: AppPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllEmojiBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        binding.switchEnableBatteryEmoji.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkAccessibilityPermission()
            } else {
                preferences.isStatusBarEnabled = false
            }
        }
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabTitles.size

            override fun createFragment(position: Int): Fragment {
                return ViewPagerEmojiItemFragment.newInstance(position)
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()


    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(
                requireContext(),
                "Please enable accessibility service",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }else{
            preferences.isStatusBarEnabled = true
            binding.switchEnableBatteryEmoji.isChecked = true

        }
        // else, do nothing or show UI as normal
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName =
            "${requireContext().packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':')
            .any { it.equals(expectedComponentName, ignoreCase = true) }
    }


    override fun onResume() {
        binding.switchEnableBatteryEmoji.isChecked = isAccessibilityServiceEnabled() && preferences.isStatusBarEnabled
        super.onResume()
    }
}