package com.lowbyte.battery.animation.activity

import GestureBottomSheetFragment
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.ActionScrollItem
import com.lowbyte.battery.animation.databinding.ActivityStatusBarCustommizeBinding
import com.lowbyte.battery.animation.databinding.ActivityStatusBarGestureBinding

class StatusBarGestureActivity : AppCompatActivity() {

    private var _binding: ActivityStatusBarGestureBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityStatusBarGestureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        _binding?.ibBackButton?.setOnClickListener {
            finish()
        }

        _binding?.viewSingleTap?.setOnClickListener {
            showGestureBottomSheet(getString(R.string.single_tap), "Goes Back")
        }
        _binding?.viewLongTap?.setOnClickListener {
            showGestureBottomSheet(getString(R.string.long_press), "Shows Menu")
        }
        _binding?.swipeLeftToRightView?.setOnClickListener {
            showGestureBottomSheet(getString(R.string.swipe_left_to_right), "Open Notifications")
        }
        _binding?.swipeRightToLeftView?.setOnClickListener {
            showGestureBottomSheet(getString(R.string.swipe_right_to_left), "Open Quick Settings")
        }




    }
    // Add this helper function in your activity:
    private fun showGestureBottomSheet(title: String, action: String) {
        val items = listOf(
            ActionScrollItem(getString(R.string.action_quick_scroll_to_up)),
            ActionScrollItem(getString(R.string.action_open_notifications)),
            ActionScrollItem(getString(R.string.action_open_control_centre)),
            ActionScrollItem(getString(R.string.action_power_options)),
            ActionScrollItem(getString(R.string.action_do_nothing)),
            ActionScrollItem(getString(R.string.action_back_action)),
            ActionScrollItem(getString(R.string.action_home_action)),
            ActionScrollItem(getString(R.string.action_recent_action)),
            ActionScrollItem(getString(R.string.action_take_screenshot)),
            ActionScrollItem(getString(R.string.action_lock_screen)),
            // Add more actions as needed
        )
        GestureBottomSheetFragment(title, action, items).show(supportFragmentManager, "GestureBottomSheet")
    }
}