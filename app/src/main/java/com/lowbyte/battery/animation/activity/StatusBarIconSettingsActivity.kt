package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityStatusBarIconSettingsBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class StatusBarIconSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityStatusBarIconSettingsBinding
    private lateinit var preferences: AppPreferences

    private var position: Int = -1
    private lateinit var label: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  enableEdgeToEdge()
        binding = ActivityStatusBarIconSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        FirebaseAnalyticsUtils.logScreenView(this, "StatusBarIconSettingsScreen")
        FirebaseAnalyticsUtils.startScreenTimer("StatusBarIconSettingsScreen")

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        position = intent.getIntExtra(EXTRA_POSITION, -1)
        label = intent.getStringExtra(EXTRA_LABEL) ?: getString(R.string.wifi)
        binding.tvTitle.text = label

        binding.seekBarIconSize.progress = preferences.getIconSize("size_$position", 25)
        binding.labelIconSize.text = getString(R.string.size_dp, label, preferences.getIconSize("size_$position", 25))

        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", null)
            finish()
        }

        binding.customizeStatusBarBgColor.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(label)
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(
                    getString(R.string.apply),
                    ColorEnvelopeListener { envelope, fromUser -> applyIconColor("tint_$position",envelope) })
                .setNegativeButton(
                    getString(R.string.cancel)
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
        }

        binding.seekBarIconSize.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                applyIconSize("size_$position", progress)
                binding.labelIconSize.text = getString(R.string.size_dp, label, progress)
                FirebaseAnalyticsUtils.logClickEvent(this@StatusBarIconSettingsActivity, "change_icon_size", mapOf(
                    "position" to position.toString(),
                    "size" to progress.toString(),
                    "label" to label
                ))
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                if (!preferences.isStatusBarEnabled) {
                    Toast.makeText(this@StatusBarIconSettingsActivity,
                        getString(R.string.please_enable_battery_emoji_service),
                        Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun applyIconColor(name: String, envelope: ColorEnvelope?) {
        if (!preferences.isStatusBarEnabled) {
            Toast.makeText(this, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
        }
        envelope?.color?.let { color ->
            preferences.setInt(name, color)
            sendBroadcast(Intent(BROADCAST_ACTION))
        }
    }

    private fun applyIconSize(name: String, iconSize: Int) {
        preferences.setIconSize(name, iconSize)
        sendBroadcast(Intent(BROADCAST_ACTION))
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "StatusBarIconSettingsScreen")
    }
}