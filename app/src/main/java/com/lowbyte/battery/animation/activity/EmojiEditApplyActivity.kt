package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.graphics.Color
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
import com.lowbyte.battery.animation.activity.StatusBarCustomizeActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivityEmojiEditApplayBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
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

        val position = intent.getIntExtra(EXTRA_POSITION, -1)
         drawable = intent.getStringExtra(EXTRA_LABEL) ?: getString(R.string.wifi)

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
        binding.ibNextButton.setOnClickListener {
//            preferences.setIconSize("batteryIconSize", 24)
//            preferences.setIconSize("percentageSize", 12)
            preferences.setInt("percentageColor", Color.BLACK)
            binding.batteryEmojiPercentageSeekbarSize.progress = 12
            binding.batteryEmojiSeekbarSize.progress = 24
        }


        binding.enableShowBatteryPercentage.isChecked = preferences.showBatteryPercent
        binding.batteryEmojiSeekbarSize.progress = preferences.getIconSize("batteryIcon", 25)
        binding.batteryEmojiPercentageSeekbarSize.progress = preferences.getIconSize("percentageSize", 25)

        binding.batteryEmojiSeekbarSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update emoji size or preview
                preferences.setIconSize("batteryIconSize", progress)
                sendBroadcast(Intent(BROADCAST_ACTION))
                binding.batteryEmojiLabel.text = getString(
                    R.string.size_dp,
                    getString(R.string.view_all_battery_emoji),
                    preferences.getIconSize("batteryIconSize", 25)
                )


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!preferences.isStatusBarEnabled){
                    Toast.makeText(this@EmojiEditApplyActivity,
                        getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })


        // ✅ Percentage Size SeekBar Listener
        binding.batteryEmojiPercentageSeekbarSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.setIconSize("percentageSize", progress)
                sendBroadcast(Intent(BROADCAST_ACTION))
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
                if (!preferences.isStatusBarEnabled){
                    Toast.makeText(this@EmojiEditApplyActivity,
                        getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
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
            sendBroadcast(Intent(BROADCAST_ACTION))

            // Handle toggle logic
        }

        // ✅ Apply Button Click Listener
        binding.btnNext.setOnClickListener {
            if (preferences.shouldTriggerEveryThirdTime("interstitial_ad_count")) {
                AdManager.showInterstitialAd(this) {
                    Log.e("Ads","FullScreenTobeShoe")
                }
            }

            preferences
            preferences.batteryIconName = drawable
            sendBroadcast(Intent(BROADCAST_ACTION))
            startActivity(Intent(this, ApplySuccessfullyActivity::class.java))
            finish()

        }
    }

    private fun colorOfIcon(envelope: ColorEnvelope) {
        if (!binding.enableShowBatteryPercentage.isChecked){
            Toast.makeText(this, getString(R.string.please_turn_on_battery_percentage), Toast.LENGTH_SHORT).show()
        }
        if (!preferences.isStatusBarEnabled){
            Toast.makeText(this@EmojiEditApplyActivity,
                getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
        }
        preferences.setInt("percentageColor", envelope.color)
        sendBroadcast(Intent(BROADCAST_ACTION))
    }
}


