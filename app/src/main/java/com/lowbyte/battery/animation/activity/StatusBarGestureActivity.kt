package com.lowbyte.battery.animation.activity

import GestureBottomSheetFragment
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.EmojiEditApplyActivity
import com.lowbyte.battery.animation.adapter.ActionScrollItem
import com.lowbyte.battery.animation.databinding.ActivityStatusBarGestureBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class StatusBarGestureActivity : BaseActivity() {

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

        singleTapActionText.text = getLocalizedStringFromPrefKey(this, "gestureAction", R.string.action_do_nothing)
        longTapActionText.text = getLocalizedStringFromPrefKey(this, "longPressAction", R.string.action_do_nothing)
        swipeLeftToRightActionText.text = getLocalizedStringFromPrefKey(this, "swipeLeftToRightAction", R.string.action_do_nothing)
        swipeRightToLeftActionText.text = getLocalizedStringFromPrefKey(this, "swipeRightToLeftAction", R.string.action_do_nothing)


        binding.switchVibrateFeedback.isChecked = preferences.isVibrateMode
        binding.switchVibrateFeedback.setOnCheckedChangeListener { _, isChecked ->
            preferences.isVibrateMode = isChecked

            if (!preferences.isStatusBarEnabled && isChecked){
                Toast.makeText(this@StatusBarGestureActivity,
                    getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
            }

        }

        binding.gestureSwitchEnable.isChecked = preferences.isGestureMode
        binding.gestureSwitchEnable.setOnCheckedChangeListener { _, isChecked ->
            preferences.isGestureMode = isChecked
            if (!preferences.isStatusBarEnabled && isChecked){
                Toast.makeText(this@StatusBarGestureActivity,
                    getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
            }

        }

        binding.viewSingleTap.setOnClickListener {
            showGestureBottomSheet(
                getString(R.string.single_tap),
                singleTapActionText.text.toString()
            ) { selected ->
                singleTapActionText.text = selected.label
                preferences.setString("gestureAction", selected.actionName)
            }
        }

        binding.viewLongTap.setOnClickListener {
            showGestureBottomSheet(
                getString(R.string.long_press),
                longTapActionText.text.toString()
            ) { selected ->
                longTapActionText.text = selected.label
                preferences.setString("longPressAction", selected.actionName)
            }
        }

        binding.swipeLeftToRightView.setOnClickListener {
            showGestureBottomSheet(
                getString(R.string.swipe_left_to_right),
                swipeLeftToRightActionText.text.toString()
            ) { selected ->
                swipeLeftToRightActionText.text = selected.label
                preferences.setString("swipeLeftToRightAction", selected.actionName)
            }
        }

        binding.swipeRightToLeftView.setOnClickListener {
            showGestureBottomSheet(
                getString(R.string.swipe_right_to_left),
                swipeRightToLeftActionText.text.toString()
            ) { selected ->
                swipeRightToLeftActionText.text = selected.label
                preferences.setString("swipeRightToLeftAction", selected.actionName)
            }
        }






    }
    // Add this helper function in your activity:
    private fun showGestureBottomSheet(
        title: String,
        currentAction: String,
        onActionSelected: (ActionScrollItem) -> Unit
    ) {
        val items = listOf(
            ActionScrollItem(getString(R.string.action_quick_scroll_to_up),"action_quick_scroll_to_up"),
            ActionScrollItem(getString(R.string.action_open_notifications),"action_open_notifications"),
           // ActionScrollItem(getString(R.string.action_open_control_centre),"action_open_control_centre"),
            ActionScrollItem(getString(R.string.action_power_options),"action_power_options"),
            ActionScrollItem(getString(R.string.action_do_nothing),"action_do_nothing"),
            ActionScrollItem(getString(R.string.action_back_action),"action_back_action"),
            ActionScrollItem(getString(R.string.action_home_action),"action_home_action"),
            ActionScrollItem(getString(R.string.action_recent_action),"action_recent_action"),
            ActionScrollItem(getString(R.string.action_take_screenshot),"action_take_screenshot"),
            ActionScrollItem(getString(R.string.action_lock_screen),"action_lock_screen"),
        )

        val bottomSheet =
            GestureBottomSheetFragment(title, currentAction, items) { selectedAction ->
                onActionSelected(selectedAction)
            }

        bottomSheet.show(supportFragmentManager, "GestureBottomSheet")
    }

    fun getLocalizedStringFromPrefKey(context: Context, key: String, defaultResId: Int): String {
        val savedKey = preferences.getString(key, "")

        savedKey?.let {
            val resId = context.resources.getIdentifier(it, "string", context.packageName)
            if (resId != 0) {
                return context.getString(resId)
            }
        }
        return context.getString(defaultResId)
    }
}