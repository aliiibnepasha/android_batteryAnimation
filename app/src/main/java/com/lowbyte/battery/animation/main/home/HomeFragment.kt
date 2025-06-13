package com.lowbyte.battery.animation.main.home

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.BatteryAnimationEditApplyActivity
import com.lowbyte.battery.animation.activity.BatteryWidgetEditApplyActivity
import com.lowbyte.battery.animation.activity.EmojiEditApplyActivity
import com.lowbyte.battery.animation.adapter.MultiViewAdapter
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.FragmentHomeBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.model.MultiViewItem
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.combinedAnimationList
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiCuteListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListAction
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preferences = AppPreferences.getInstance(requireContext())
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = AppPreferences.getInstance(requireContext())
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        // Log screen view
        FirebaseAnalyticsUtils.logScreenView(this, "HomeFragment")

        AdManager.loadInterstitialAd(requireContext())

        Handler(Looper.getMainLooper()).postDelayed({
            binding.switchEnableBatteryEmoji.isChecked = preferences.isStatusBarEnabled && isAccessibilityServiceEnabled()
            Log.d("TAG_Access", "Create ${preferences.isStatusBarEnabled}")

            binding.switchEnableBatteryEmoji.setOnCheckedChangeListener { _, isChecked ->
                preferences.isStatusBarEnabled = isChecked
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(), "toggle_battery_emoji_service",
                    mapOf("enabled" to isChecked.toString())
                )

                if (::preferences.isInitialized && preferences.isStatusBarEnabled && isChecked) {
                    checkAccessibilityPermission()
                } else {
                    requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))
                }
            }
        }, 500)

        val data = listOf(
            MultiViewItem.TitleItem(getString(R.string.cat_emojis)),
            MultiViewItem.ListEmojiOrWidgetItem(emojiCuteListFantasy),
            MultiViewItem.TitleItem(getString(R.string.cat_widgets)),
            MultiViewItem.ListEmojiOrWidgetItem(widgetListAction),
            MultiViewItem.TitleItem(getString(R.string.cat_animations)),
            MultiViewItem.ListAnimationItem(combinedAnimationList),
        )

        val adapter = MultiViewAdapter(
            data,
            onChildItemClick = { parentPosition, name, parentPos ->
                val label = name.ifBlank { "unknown" }

                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(), "click_list_item",
                    mapOf("category_index" to "$parentPos", "item_label" to label)
                )

                val intent = when (parentPos) {
                    1 -> Intent(requireActivity(), EmojiEditApplyActivity::class.java)
                    3 -> Intent(requireActivity(), BatteryWidgetEditApplyActivity::class.java)
                    5 -> Intent(requireActivity(), BatteryAnimationEditApplyActivity::class.java)
                    else -> null
                }
                intent?.apply {
                    putExtra(EXTRA_POSITION, parentPosition)
                    putExtra(EXTRA_LABEL, label)
                    startActivity(this)
                }
            },
            onChildViewAllClick = { titlePosition ->
                val eventMap = mapOf("section_index" to "$titlePosition")
                when (titlePosition) {
                    0 -> {
                        FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "view_all_emojis", eventMap)
                        findNavController().navigate(R.id.action_home_to_viewAllEmoji)
                    }
                    2 -> {
                        FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "view_all_widgets", eventMap)
                        findNavController().navigate(R.id.action_home_to_viewAllWidget)
                    }
                    4 -> {
                        FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "view_all_animations", eventMap)
                        findNavController().navigate(R.id.action_home_to_viewAllAnim)
                    }
                }
            }
        )

        binding.recyclerViewParent.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewParent.adapter = adapter
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "accessibility_prompt_shown")

            val sheet = AccessibilityPermissionBottomSheet(
                onAllowClicked = {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enable_accessibility_service),
                        Toast.LENGTH_LONG
                    ).show()
                    FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "accessibility_allow_clicked")
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                onCancelClicked = {
                    FirebaseAnalyticsUtils.logClickEvent(requireActivity(), "accessibility_cancel_clicked")
                    preferences.isStatusBarEnabled = false
                    binding.switchEnableBatteryEmoji.isChecked = false
                }
            )
            sheet.show(childFragmentManager, "AccessibilityPermission")
        } else {
            binding.switchEnableBatteryEmoji.isChecked = preferences.isStatusBarEnabled
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