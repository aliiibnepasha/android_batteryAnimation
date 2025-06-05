package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivityEmojiEditApplayBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class EmojiEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityEmojiEditApplayBinding
    private lateinit var preferences: AppPreferences
    private lateinit var drawable: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEmojiEditApplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        val position = intent.getIntExtra("EXTRA_POSITION", -1)
         drawable = intent.getStringExtra("EXTRA_LABEL") ?: getString(R.string.wifi)

        Log.i("ITEMCLICK", "$position $drawable")
        val resId = resources.getIdentifier(drawable, "drawable", packageName)
        if (resId != 0) {
            binding.previewEditEmoji.setImageResource(resId)
        } else {
            binding.previewEditEmoji.setImageResource(R.drawable.emoji_1) // fallback
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.ibBackButton.setOnClickListener {
            finish()
        }


        binding.enableShowBatteryPercentage.isChecked = preferences.showBatteryPercent
        binding.batteryEmojiSeekbarSize.progress = preferences.getIconSize("batteryIcon", 25)
        binding.batteryEmojiPercentageSeekbarSize.progress = preferences.getIconSize("percentageSize", 25)

        binding.batteryEmojiSeekbarSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update emoji size or preview
                preferences.setIconSize("batteryIconSize", progress)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
                binding.batteryEmojiLabel.text = getString(
                    R.string.size_dp,
                    getString(R.string.view_all_battery_emoji),
                    preferences.getIconSize("batteryIconSize", 25)
                )


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        // ✅ Percentage Size SeekBar Listener
        binding.batteryEmojiPercentageSeekbarSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.setIconSize("percentageSize", progress)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
                binding.batteryPercentageEmojiLabel.text = getString(
                    R.string.size_dp,
                    getString(R.string.percentage_size),
                    preferences.getIconSize("percentageSize", 25)
                )

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.enableShowBatteryPercentage.isChecked){
                    Toast.makeText(this@EmojiEditApplyActivity, getString(R.string.please_turn_on_battery_percentage), Toast.LENGTH_SHORT).show()
                }

            }
        })

        // ✅ Color Picker Click Listener
        binding.viewPercentageColor.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(
                    getString(R.string.apply),
                    ColorEnvelopeListener { envelope, fromUser -> colorOfIcon(envelope) })
                .setNegativeButton(
                    getString(R.string.cancel)
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()            // Open your custom color picker here
        }

        // ✅ Switch Toggle Listener
        binding.enableShowBatteryPercentage.setOnCheckedChangeListener { _, isChecked ->
            preferences.showBatteryPercent = isChecked
            sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            // Handle toggle logic
        }

        // ✅ Apply Button Click Listener
        binding.btnNext.setOnClickListener {
            if (preferences.shouldTriggerEveryThirdTime("interstitial_ad_count")) {
                AdManager.showInterstitialAd(this) {
                    Log.e("Ads","FullScreenTobeShoe")
                }
            }
            preferences.batteryIconName = drawable
            sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            startActivity(Intent(this, ApplySuccessfullyActivity::class.java))
            finish()

        }
    }

    private fun colorOfIcon(envelope: ColorEnvelope) {
        if (!binding.enableShowBatteryPercentage.isChecked){
            Toast.makeText(this, getString(R.string.please_turn_on_battery_percentage), Toast.LENGTH_SHORT).show()
        }
        preferences.setInt("percentageColor", envelope.color)
        sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
    }
}


