package com.lowbyte.battery.animation.main.customization

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.StatusBarCustomizeActivity
import com.lowbyte.battery.animation.activity.StatusBarGestureActivity
import com.lowbyte.battery.animation.activity.StatusBarIconSettingsActivity
import com.lowbyte.battery.animation.adapter.CustomIconGridAdapter
import com.lowbyte.battery.animation.databinding.FragmentCustomizeBinding
import com.lowbyte.battery.animation.model.CustomIconGridItem
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class CustomizeFragment : Fragment() {

    private var _binding: FragmentCustomizeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomizeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Log screen view event
        FirebaseAnalyticsUtils.logScreenView(this, "CustomizeFragment")

        // Status bar customization click
        binding.menuStatusBar.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_status_customize")
            startActivity(Intent(requireContext(), StatusBarCustomizeActivity::class.java))
        }

        // Gesture customization click
        binding.menuGesture.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(requireContext(), "click_gesture_customize")
            startActivity(Intent(requireContext(), StatusBarGestureActivity::class.java))
        }

        val items = arrayListOf(
            CustomIconGridItem(R.drawable.action_single_tap, getString(R.string.wifi)),
            CustomIconGridItem(R.drawable.action_single_tap, getString(R.string.data)),
            CustomIconGridItem(R.drawable.action_single_tap, getString(R.string.signals)),
            CustomIconGridItem(R.drawable.action_single_tap, getString(R.string.airplane)),
            CustomIconGridItem(R.drawable.action_single_tap, getString(R.string.hotspot)),
            CustomIconGridItem(R.drawable.action_single_tap, getString(R.string.time))
        )

        val adapter = CustomIconGridAdapter(items) { position, label ->
            FirebaseAnalyticsUtils.logClickEvent(this, "click_icon_item", mapOf("position" to position.toString(), "label" to label))
            FirebaseAnalyticsUtils.logClickEvent(this, "StatusBarIconSettingsActivity", mapOf("position" to position.toString(), "label" to label))
            val intent = Intent(requireContext(), StatusBarIconSettingsActivity::class.java)
            intent.putExtra(EXTRA_POSITION, position)
            intent.putExtra(EXTRA_LABEL, label)
            startActivity(intent)
        }

        binding.recyclerViewCustomIcon.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewCustomIcon.adapter = adapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}