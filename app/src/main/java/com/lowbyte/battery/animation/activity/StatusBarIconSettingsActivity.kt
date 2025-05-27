package com.lowbyte.battery.animation.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivityStatusBarIconSettingsBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class StatusBarIconSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBarIconSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStatusBarIconSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = AppPreferences.getInstance(this)

        // Retrieve the extras
        val position = intent.getIntExtra("EXTRA_POSITION", -1)
        val label = intent.getStringExtra("EXTRA_LABEL") ?: getString(R.string.status_bar)
        binding.tvTitle.text = label

        // Build key for each icon by its label (spaces/lowercase for uniqueness)
        val prefKey = "icon_size_${label.trim().replace("\\s+".toRegex(), "_").lowercase()}"

        // Restore saved icon size for this label/icon
        val savedIconSize = prefs.getInt(prefKey, 50) // 50 is default
        binding.seekBarIconSize.progress = savedIconSize

        binding.labelIconSize.text = getString(R.string.size_dp, label, savedIconSize)

        binding.ibBackButton.setOnClickListener {
            finish()
        }

        // Save SeekBar value when changed
        binding.seekBarIconSize.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                prefs.setInt(prefKey, progress)
                binding.labelIconSize.text = getString(R.string.size_dp, label, progress)
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }



    /*
    // Save icon size
prefs.setIconSize("wifi", wifiSeekBar.progress)
prefs.setIconSize("signal", signalSeekBar.progress)

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