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
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lowbyte.battery.animation.BuildConfig
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.activity.AllowAccessibilityActivity
import com.lowbyte.battery.animation.activity.StatusBarIconSettingsActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.FragmentViewAllEmojiBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.ui.InteractiveLottieActivity
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION_DYNAMIC
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenHome2Id
import com.lowbyte.battery.animation.utils.AnimationUtils.getTabTitlesEmoji
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenStatusEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewAllEmojiFragment : Fragment() {

    private lateinit var binding: FragmentViewAllEmojiBinding
    private lateinit var preferences: AppPreferences
    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllEmojiBinding.inflate(inflater, container, false)
        AdManager.loadInterstitialAd(requireActivity(),getFullscreenHome2Id(),isFullscreenStatusEnabled)

        preferences = AppPreferences.getInstance(requireContext())
        sheet = AccessibilityPermissionBottomSheet(
            onAllowClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "allow_accessibility_click"
                )
                startActivity(Intent(requireActivity(), AllowAccessibilityActivity::class.java))
                // startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onCancelClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "cancel_accessibility_permission"
                )
                preferences.isStatusBarEnabled = false
                binding.switchEnableBatteryEmojiViewAll.isChecked = false
            }, onDismissListener = {
                if (!isAccessibilityServiceEnabled()) {
                    preferences.isStatusBarEnabled = false
                    binding.switchEnableBatteryEmojiViewAll.isChecked = false
                }

            }
        )
        // Log screen view
        FirebaseAnalyticsUtils.logScreenView(this, "ViewAllEmojiFragment")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()

        binding.switchEnableBatteryEmojiViewAll.isChecked = preferences.isStatusBarEnabled && isAccessibilityServiceEnabled()

//
//        binding.tvCustomize.setOnClickListener {
//            FirebaseAnalyticsUtils.logClickEvent(
//                requireActivity(),
//                "InteractiveLottieAct"
//            )
//            AdManager.showInterstitialAd(
//                requireActivity(),
//                isFullscreenStatusEnabled,
//                true
//            ) {
//                startActivity(Intent(requireContext(), InteractiveLottieActivity::class.java))
//
//            }
//        }


        binding.switchEnableBatteryEmojiViewAll.setOnCheckedChangeListener { _, isChecked ->
            Handler(Looper.getMainLooper()).postDelayed({

                if (isAdded){
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
                    requireActivity().sendBroadcast(Intent(BROADCAST_ACTION_DYNAMIC))

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
//            if (BuildConfig.DEBUG){
//                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
//            }else{
                val existing = childFragmentManager.findFragmentByTag("AccessibilityPermission")
                if (existing == null || !existing.isAdded) {
                    sheet.show(childFragmentManager, "AccessibilityPermission")
                } else {
                    Log.d("Accessibility", "AccessibilityPermissionBottomSheet already shown")
                }
         //   }
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