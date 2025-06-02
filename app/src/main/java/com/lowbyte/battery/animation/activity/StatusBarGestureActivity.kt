package com.lowbyte.battery.animation.activity

import GestureBottomSheetFragment
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.ActionScrollItem
import com.lowbyte.battery.animation.databinding.ActivityStatusBarGestureBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class StatusBarGestureActivity : AppCompatActivity() {

    private var _binding: ActivityStatusBarGestureBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: AppPreferences
    private lateinit var singleTapActionText: TextView
    private lateinit var longTapActionText: TextView
    private lateinit var swipeLeftToRightActionText: TextView
    private lateinit var swipeRightToLeftActionText: TextView
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
        preferences = AppPreferences.getInstance(this)

        singleTapActionText = binding.statusBarSingleTapAction
        longTapActionText = binding.statusBarLongAction
        swipeLeftToRightActionText = binding.swipeLeftToRightAction
        swipeRightToLeftActionText = binding.actionOnSwipeRightToLeft
        _binding?.ibBackButton?.setOnClickListener {
            finish()
        }

        singleTapActionText.text =
            preferences.getString("gestureAction", getString(R.string.action_do_nothing))
        longTapActionText.text =
            preferences.getString("longPressAction", getString(R.string.action_do_nothing))
        swipeLeftToRightActionText.text =
            preferences.getString("swipeLeftToRightAction", getString(R.string.action_do_nothing))
        swipeRightToLeftActionText.text =
            preferences.getString("swipeRightToLeftAction", getString(R.string.action_do_nothing))




        binding.switchVibrateFeedback.isChecked = preferences.isVibrateMode
        binding.switchVibrateFeedback.setOnCheckedChangeListener { _, isChecked ->
            preferences.isVibrateMode = isChecked
        }

        binding.gestureSwitchEnable.isChecked = preferences.isGestureMode
        binding.gestureSwitchEnable.setOnCheckedChangeListener { _, isChecked ->
            preferences.isGestureMode = isChecked

        }

        binding.viewSingleTap.setOnClickListener {
            showGestureBottomSheet(
                getString(R.string.single_tap),
                singleTapActionText.text.toString()
            ) { selected ->
                singleTapActionText.text = selected
                preferences.setString("gestureAction", selected)
            }
        }

        binding.viewLongTap.setOnClickListener {
            showGestureBottomSheet(
                getString(R.string.long_press),
                longTapActionText.text.toString()
            ) { selected ->
                longTapActionText.text = selected
                preferences.setString("longPressAction", selected)
            }
        }

        binding.swipeLeftToRightView.setOnClickListener {
            showGestureBottomSheet(
                getString(R.string.swipe_left_to_right),
                swipeLeftToRightActionText.text.toString()
            ) { selected ->
                swipeLeftToRightActionText.text = selected
                preferences.setString("swipeLeftToRightAction", selected)
            }
        }

        binding.swipeRightToLeftView.setOnClickListener {
            showGestureBottomSheet(
                getString(R.string.swipe_right_to_left),
                swipeRightToLeftActionText.text.toString()
            ) { selected ->
                swipeRightToLeftActionText.text = selected
                preferences.setString("swipeRightToLeftAction", selected)
            }
        }






    }
    // Add this helper function in your activity:
    private fun showGestureBottomSheet(
        title: String,
        currentAction: String,
        onActionSelected: (String) -> Unit
    ) {
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
        )

        val bottomSheet =
            GestureBottomSheetFragment(title, currentAction, items) { selectedAction ->
                onActionSelected(selectedAction)
            }

        bottomSheet.show(supportFragmentManager, "GestureBottomSheet")
    }
}