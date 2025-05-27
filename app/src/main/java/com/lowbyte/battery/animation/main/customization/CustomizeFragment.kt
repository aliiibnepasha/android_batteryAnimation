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

class CustomizeFragment : Fragment() {

    private var _binding: FragmentCustomizeBinding? = null

    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCustomizeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _binding?.menuStatusBar?.setOnClickListener {
            startActivity(Intent(requireContext(), StatusBarCustomizeActivity::class.java))
        }

        _binding?.menuGesture?.setOnClickListener {
            startActivity(Intent(requireContext(), StatusBarGestureActivity::class.java))
        }

        return root
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