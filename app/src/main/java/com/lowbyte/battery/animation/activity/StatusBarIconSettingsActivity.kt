package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityStatusBarIconSettingsBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class StatusBarIconSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBarIconSettingsBinding
    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStatusBarIconSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val position = intent.getIntExtra("EXTRA_POSITION", -1)
        val label = intent.getStringExtra("EXTRA_LABEL") ?: getString(R.string.wifi)
        binding.tvTitle.text = label

        // Restore saved icon size for this label/icon
        binding.seekBarIconSize.progress = preferences.getIconSize("size_$position", 25)

        binding.labelIconSize.text = getString(R.string.size_dp, label, preferences.getIconSize("size_$position", 25))

        binding.ibBackButton.setOnClickListener {
            finish()
        }
        binding.openColorPalate.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.status_bar))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(
                    "Conform",
                    ColorEnvelopeListener { envelope, fromUser -> colorOfIcon("tint_$position",envelope) })
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
        }


        // Save SeekBar value when changed
        binding.seekBarIconSize.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                sizeOfIcon("size_$position",progress)
                binding.labelIconSize.text = getString(R.string.size_dp, label, preferences.getIconSize("size_$position", 25))

            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }



    fun colorOfIcon(name: String,envelope: ColorEnvelope?) {
        when (name) {
            "tint_0" -> {
                preferences.setInt("tint_0", envelope?.color!!)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            }

            "tint_1" -> {
                preferences.setInt("tint_1", envelope?.color!!)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            "tint_2" -> {
                preferences.setInt("tint_2", envelope?.color!!)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            "tint_3" -> {
                preferences.setInt("tint_3", envelope?.color!!)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            "tint_4" -> {
                preferences.setInt("tint_4", envelope?.color!!)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            "tint_5" -> {
                preferences.setInt("tint_5", envelope?.color!!)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            }


        }
    }

     fun sizeOfIcon(name: String,iconSize: Int) {
        when (name) {
            "size_0" -> {
                preferences.setIconSize("size_0", iconSize)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            }

            "size_1" -> {
                preferences.setIconSize("size_1", iconSize)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            "size_2" -> {
                preferences.setIconSize("size_2", iconSize)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            "size_3" -> {
                preferences.setIconSize("size_3", iconSize)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            "size_4" -> {
                preferences.setIconSize("size_4", iconSize)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            "size_5" -> {
                preferences.setIconSize("size_5", iconSize)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            }


        }
    }


    fun enableDisableIcon(name: String,isEnable: Boolean) {
        when (name) {
            getString(R.string.wifi) -> {
                preferences.showWifi = isEnable
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            getString(R.string.data) -> {
                preferences.showHotspot = isEnable
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            getString(R.string.signals) -> {
                preferences.showSignal = isEnable
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            getString(R.string.airplane) -> {
                preferences.showAirplane = isEnable
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            getString(R.string.hotspot) -> {
                preferences.showHotspot = isEnable
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            getString(R.string.time) -> {
                preferences.showTime = isEnable
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }


        }
    }








    /*
    // Save icon size


// Save show/hide
prefs.showWifi = wifiSwitch.isChecked
prefs.showHotspot = hotspotSwitch.isChecked
// ...

// Save background color (from a color picker, for example)
prefs.statusBarBgColor = pickedColor

// Save lottie and icon drawable name
prefs.statusLottieName = "animation_wifi"
prefs.statusIconName = "ic_custom_battery"

// In settings UI after a change
sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
// In service, listen for this broadcast and call updateStatusBarAppearance()
    *
    *
    *
    *
    * */
}