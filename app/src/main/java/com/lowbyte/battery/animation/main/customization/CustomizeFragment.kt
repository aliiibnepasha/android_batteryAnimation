package com.lowbyte.battery.animation.main.customization

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.activity.StatusBarCustomizeActivity
import com.lowbyte.battery.animation.activity.StatusBarGestureActivity
import com.lowbyte.battery.animation.databinding.FragmentCustomizeBinding
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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}