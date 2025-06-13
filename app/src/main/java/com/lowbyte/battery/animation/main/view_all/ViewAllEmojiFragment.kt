package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.FragmentViewAllEmojiBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.getTabTitlesEmoji
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewAllEmojiFragment : Fragment() {

    private lateinit var binding: FragmentViewAllEmojiBinding
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllEmojiBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())

        // Log screen view
        FirebaseAnalyticsUtils.logScreenView(this, "ViewAllEmojiFragment")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()

        binding.switchEnableBatteryEmojiViewAll.isChecked =
            preferences.isStatusBarEnabled && isAccessibilityServiceEnabled()

        binding.switchEnableBatteryEmojiViewAll.setOnCheckedChangeListener { _, isChecked ->
            Handler(Looper.getMainLooper()).postDelayed({
                preferences.isStatusBarEnabled = isChecked

                // Log toggle
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "toggle_statusbar_emoji_from_emoji_screen",
                    mapOf("enabled" to isChecked.toString())
                )

                if (preferences.isStatusBarEnabled && isChecked) {
                    checkAccessibilityPermission()
                } else {
                    requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))
                }
            }, 500)
        }
    }

    private fun setupViewPager() {
        val tabTitles = getTabTitlesEmoji(requireContext())

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
            AccessibilityPermissionBottomSheet(
                onAllowClicked = {
                    FirebaseAnalyticsUtils.logClickEvent(
                        requireActivity(),
                        "allow_accessibility_click"
                    )
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enable_accessibility_service),
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onCancelClicked = {
                    FirebaseAnalyticsUtils.logClickEvent(
                        requireActivity(),
                        "cancel_accessibility_permission"
                    )
                    preferences.isStatusBarEnabled = false
                    binding.switchEnableBatteryEmojiViewAll.isChecked = false
                }
            ).show(childFragmentManager, "AccessibilityPermission")
        } else {
            binding.switchEnableBatteryEmojiViewAll.isChecked = preferences.isStatusBarEnabled
            requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName =
            "${requireContext().packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':').any {
            it.equals(expectedComponentName, ignoreCase = true)
        }
    }
}