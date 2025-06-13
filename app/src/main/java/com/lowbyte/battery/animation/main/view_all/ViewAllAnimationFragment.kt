package com.lowbyte.battery.animation.main.view_all

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.BatteryAnimationEditApplyActivity
import com.lowbyte.battery.animation.adapter.AnimationAdapter
import com.lowbyte.battery.animation.databinding.FragmentViewAllAnimationBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.combinedAnimationList
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class ViewAllAnimationFragment : Fragment() {

    private lateinit var binding: FragmentViewAllAnimationBinding
    private lateinit var adapter: AnimationAdapter
    private lateinit var preferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllAnimationBinding.inflate(inflater, container, false)
        preferences = AppPreferences.getInstance(requireContext())

        // Log screen view event
        FirebaseAnalyticsUtils.logScreenView(this, "ViewAllAnimationFragment")

        binding.switchVibrateFeedback.isChecked = preferences.statusLottieName.isNotBlank()

        binding.switchVibrateFeedback.setOnCheckedChangeListener { _, isChecked ->
            if (!preferences.isStatusBarEnabled && isChecked) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enable_battery_emoji_service),
                    Toast.LENGTH_LONG
                ).show()
            }
            preferences.statusLottieName = if (isChecked) "a_1" else ""
            requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))

            // Log toggle event
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "toggle_lottie_enabled",
                mapOf("enabled" to isChecked.toString())
            )
        }

        binding.enableSizeAnim.text = getString(R.string.height_dp, preferences.getIconSize("lottieView", 24))
        binding.seekBarAnimSize.progress = preferences.getIconSize("lottieView", 24)

        binding.seekBarAnimSize.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.setIconSize("lottieView", progress)
                binding.enableSizeAnim.text = getString(R.string.height_dp, progress)
                requireActivity().sendBroadcast(Intent(BROADCAST_ACTION))

                // Log size change
                FirebaseAnalyticsUtils.logClickEvent(
                    requireActivity(),
                    "lottie_icon_size_changed",
                    mapOf("new_size" to progress.toString())
                )
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                if (!preferences.isStatusBarEnabled) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enable_battery_emoji_service),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = AnimationAdapter { position, name ->
            Log.d("Click", "Clicked position: $position, animation: $name")

            // Log animation click
            FirebaseAnalyticsUtils.logClickEvent(
                requireActivity(),
                "click_animation_item",
                mapOf("animation_name" to name, "position" to position.toString())
            )

            val intent = Intent(requireActivity(), BatteryAnimationEditApplyActivity::class.java).apply {
                putExtra(EXTRA_POSITION, position)
                putExtra(EXTRA_LABEL, name)
            }
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@ViewAllAnimationFragment.adapter
        }

        adapter.submitList(combinedAnimationList)
    }
}