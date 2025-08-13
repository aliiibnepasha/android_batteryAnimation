package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lowbyte.battery.animation.activity.AllowAccessibilityActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.FragmentViewAllEmojiBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.server.EmojiViewModel
import com.lowbyte.battery.animation.server.EmojiViewModelFactory
import com.lowbyte.battery.animation.server.Resource
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.applyTabMargins
import com.lowbyte.battery.animation.utils.AnimationUtils.dataUrl
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenHome2Id
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenStatusEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.PermissionUtils.checkAccessibilityPermission
import com.lowbyte.battery.animation.utils.PermissionUtils.isAccessibilityServiceEnabled
import kotlinx.coroutines.launch

class ViewAllEmojiFragment : Fragment() {

    private lateinit var binding: FragmentViewAllEmojiBinding
    private lateinit var preferences: AppPreferences
    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet
    private val vm: EmojiViewModel by viewModels { EmojiViewModelFactory(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllEmojiBinding.inflate(inflater, container, false)
        AdManager.loadInterstitialAd(requireActivity(),getFullscreenHome2Id(),isFullscreenStatusEnabled)

        preferences = AppPreferences.getInstance(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    vm.categories.collect { state ->
                        if (state is Resource.Success) {
                            val categoriesList = state.data
                            Log.d("APIData", "Categories List: $categoriesList")
                            setupViewPager(categoriesList)
                        }
                    }
                }
            }
        }
        vm.loadCategoryNames(dataUrl)

        sheet = AccessibilityPermissionBottomSheet(
            onAllowClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "allow_accessibility_click"
                )
                startActivity(Intent(requireActivity(), AllowAccessibilityActivity::class.java))
            },
            onCancelClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "cancel_accessibility_permission"
                )
                preferences.isStatusBarEnabled = false
                binding.switchEnableBatteryEmojiViewAll.isChecked = false
            }, onDismissListener = {
                if (!requireActivity().isAccessibilityServiceEnabled()) {
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

        binding.switchEnableBatteryEmojiViewAll.isChecked =
            preferences.isStatusBarEnabled && requireActivity().isAccessibilityServiceEnabled()

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
                        requireActivity().checkAccessibilityPermission(false) {
                            when (it) {
                                "OpenBottomSheet" -> {
                                    sheet.show(childFragmentManager, "AccessibilityPermission")
                                }

                                "Allowed" -> {
                                    binding.switchEnableBatteryEmojiViewAll.isChecked = preferences.isStatusBarEnabled
                                    requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))
                                }

                                else -> {
                                    val existing =
                                        childFragmentManager.findFragmentByTag("AccessibilityPermission")
                                    if (existing == null || !existing.isAdded) {
                                        sheet.show(childFragmentManager, "AccessibilityPermission")
                                    } else {
                                        Log.d(
                                            "Accessibility",
                                            "AccessibilityPermissionBottomSheet already shown"
                                        )
                                    }
                                }
                            }

                        }
                    } else {
                        requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))
                    }

                }

            }, 500)
        }
    }

    private fun setupViewPager(tabsList: List<String>) {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = tabsList.size
            override fun createFragment(position: Int): Fragment {
                return ViewPagerEmojiItemFragment.newInstance(position, tabsList[position])
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabsList[position]
        }.attach()

        applyTabMargins(binding.tabLayout, 8, 8)

    }


}