package com.lowbyte.battery.animation

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lowbyte.battery.animation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAccessibilityPermission()
        setupSeekBars()
    }


    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "Please enable accessibility service", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        // else, do nothing or show UI as normal
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = "$packageName/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':').any { it.equals(expectedComponentName, ignoreCase = true) }
    }



    private fun setupSeekBars() {
        binding.seekBarX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update notch X position
                updateNotchPosition(progress, binding.seekBarY.progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update notch Y position
                updateNotchPosition(binding.seekBarX.progress, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateNotchPosition(x: Int, y: Int) {
        // Send broadcast to update notch position
        val intent = Intent("com.lowbyte.battery.animation.UPDATE_NOTCH_POSITION")
        intent.putExtra("x_position", x)
        intent.putExtra("y_position", y)
        sendBroadcast(intent)
    }
}