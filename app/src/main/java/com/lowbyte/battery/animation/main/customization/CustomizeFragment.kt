package com.lowbyte.battery.animation.main.customization

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.databinding.FragmentCustomizeBinding

class CustomizeFragment : Fragment() {

    private var _binding: FragmentCustomizeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCustomizeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        checkAccessibilityPermission()
        setupSeekBars()

        return root
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(
                requireContext(),
                "Please enable accessibility service",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
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


    private fun setupSeekBars() {
        binding.seekBarX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update notch X position
             //   updateNotchPosition(progress, binding.seekBarY.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update notch Y position
              //  updateNotchPosition(binding.seekBarX.progress, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

//    private fun updateNotchPosition(x: Int, y: Int) {
//        // Send broadcast to update notch position
//        val intent = Intent("com.lowbyte.battery.animation.UPDATE_NOTCH_POSITION")
//        intent.putExtra("x_position", x)
//        intent.putExtra("y_position", y)
//        requireContext().sendBroadcast(intent)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}