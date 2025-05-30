package com.lowbyte.battery.animation.main.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
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
import com.lowbyte.battery.animation.databinding.FragmentHomeBinding
import com.lowbyte.battery.animation.model.MultiViewItem
import com.lowbyte.battery.animation.utils.AnimationUtils.animationList
import com.lowbyte.battery.animation.utils.AnimationUtils.emojiCuteListFantasy
import com.lowbyte.battery.animation.utils.AnimationUtils.widgetListAction
import com.lowbyte.battery.animation.utils.AppPreferences

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var preferences: AppPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
        preferences = AppPreferences.getInstance(requireContext())

        binding.switchEnableBatteryEmoji.isChecked = preferences.isStatusBarEnabled

        binding.switchEnableBatteryEmoji.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && ::preferences.isInitialized) {
                checkAccessibilityPermission()
            } else {
                preferences.isStatusBarEnabled = false
                requireActivity().sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }
        }
        val data = listOf(
            MultiViewItem.TitleItem(getString(R.string.cat_emojis)),
            MultiViewItem.ListEmojiOrWidgetItem(emojiCuteListFantasy),
            MultiViewItem.TitleItem(getString(R.string.cat_widgets)),
            MultiViewItem.ListEmojiOrWidgetItem(widgetListAction),
            MultiViewItem.TitleItem(getString(R.string.cat_animations)),
            MultiViewItem.ListAnimationItem(animationList),
        )

        val adapter = MultiViewAdapter(
            data,
            onChildItemClick = { parentPosition, name, parentPos ->
                when (parentPos) {
                    1 -> {
                        val intent = Intent(requireActivity(), EmojiEditApplyActivity::class.java)
                        intent.putExtra("EXTRA_POSITION", parentPosition)
                        intent.putExtra("EXTRA_LABEL", name)
                        startActivity(intent)
                    }
                    3 -> {
                        val intent = Intent(requireActivity(), BatteryWidgetEditApplyActivity::class.java)
                        intent.putExtra("EXTRA_POSITION", parentPosition)
                        intent.putExtra("EXTRA_LABEL", name)
                        startActivity(intent)
                    }
                    5 -> {
                        val intent = Intent(requireActivity(), BatteryAnimationEditApplyActivity::class.java)
                        intent.putExtra("EXTRA_POSITION", parentPosition)
                        intent.putExtra("EXTRA_LABEL", name)
                        startActivity(intent)
                    }
                }

            },
            onChildViewAllClick = { titlePosition ->
                when (titlePosition) {
                    0 -> {
                        findNavController().navigate(R.id.action_home_to_viewAllEmoji)
                    }

                    2 -> {
                        findNavController().navigate(R.id.action_home_to_viewAllWidget)

                    }

                    4 -> {
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
            Toast.makeText(
                requireContext(),
                "Please enable accessibility service",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }else{
            preferences.isStatusBarEnabled = true
            binding.switchEnableBatteryEmoji.isChecked = true
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
        preferences = AppPreferences.getInstance(requireActivity())
        if (preferences.isStatusBarEnabled && ::preferences.isInitialized){
            binding.switchEnableBatteryEmoji.isChecked = isAccessibilityServiceEnabled()
        }else{
            binding.switchEnableBatteryEmoji.isChecked = false
            preferences.isStatusBarEnabled = false
        }
        super.onResume()
    }
}