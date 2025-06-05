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
import com.lowbyte.battery.animation.utils.AnimationUtils.getTabTitlesEmoji
import com.lowbyte.battery.animation.utils.AppPreferences


class ViewAllEmojiFragment : Fragment() {
    private lateinit var binding: FragmentViewAllEmojiBinding



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

        binding.switchEnableBatteryEmojiViewAll.isChecked = preferences.isStatusBarEnabled && isAccessibilityServiceEnabled()

        binding.switchEnableBatteryEmojiViewAll.setOnCheckedChangeListener { _, isChecked ->
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("TAG_Access", "onCreate check Call")
                preferences.isStatusBarEnabled = isChecked

                if (::preferences.isInitialized && preferences.isStatusBarEnabled && isChecked) {
                    Log.d(
                        "TAG_Access",
                        "onViewCreated: $isChecked /  ${preferences.isStatusBarEnabled}"
                    )
                    checkAccessibilityPermission()
                } else {
                    Log.d(
                        "TAG_Access",
                        "onViewCreated false: $isChecked / ${preferences.isStatusBarEnabled}"
                    )
                    requireActivity().sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
                }
            }, 500)
        }


    }

    private fun setupViewPager() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = getTabTitlesEmoji(requireContext()).size

            override fun createFragment(position: Int): Fragment {
                return ViewPagerEmojiItemFragment.newInstance(position)
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getTabTitlesEmoji(requireContext())[position]
        }.attach()


    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            val sheet = AccessibilityPermissionBottomSheet(onAllowClicked = {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_accessibility_service),
                    Toast.LENGTH_LONG
                ).show()

                Log.d("TAG_Access", "No permission but go for permission")
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

            }, onCancelClicked = {
                Log.d("TAG_Access", "No permission cancel")
                preferences.isStatusBarEnabled = false
                binding.switchEnableBatteryEmojiViewAll.isChecked = false

            })
            sheet.show(childFragmentManager, "AccessibilityPermission")

        } else {
            Log.d(
                "TAG_Access",
                "Allowed permission enabling checks ${preferences.isStatusBarEnabled}"
            )
            binding.switchEnableBatteryEmojiViewAll.isChecked = preferences.isStatusBarEnabled
            requireActivity().sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))


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
        super.onResume()
    }
}