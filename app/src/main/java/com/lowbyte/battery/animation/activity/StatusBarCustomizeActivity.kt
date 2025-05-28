package com.lowbyte.battery.animation.activity


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.CustomIconGridAdapter
import com.lowbyte.battery.animation.databinding.ActivityStatusBarCustommizeBinding
import com.lowbyte.battery.animation.model.CustomIconGridItem
import com.lowbyte.battery.animation.utils.AppPreferences
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class StatusBarCustomizeActivity : AppCompatActivity() {

    private var _binding: ActivityStatusBarCustommizeBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: AppPreferences

    // At the top of your Activity or Fragment:
    private val resizeHandler = Handler(Looper.getMainLooper())
    private var resizeRunnable: Runnable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityStatusBarCustommizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        _binding?.ibBackButton?.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.statusBarHeightSeekbar.progress = preferences.statusBarHeight
        binding.leftMarginSeekBar.progress = preferences.statusBarMarginLeft
        binding.rightMarginSeekBar.progress = preferences.statusBarMarginRight
        binding.statusBarHeightSeekbar.progress = preferences.statusBarHeight



        binding.switchEnableBatteryEmoji.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkAccessibilityPermission()
            } else {
                preferences.isStatusBarEnabled = false
            }


        }




        binding.statusBarHeight.text = getString(R.string.height_dp, preferences.statusBarHeight)
        binding.leftMarginLabel.text = getString(R.string.left_margin_dp, preferences.statusBarMarginLeft)
        binding.rightMarginLabel.text = getString(R.string.right_margin_dp, preferences.statusBarMarginRight)


        binding.statusBarHeightSeekbar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val safeProgress = progress
                preferences.statusBarHeight = safeProgress
                binding.statusBarHeight.text = getString(R.string.height_dp, safeProgress)
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
        /*Status bar height Code */
        binding.leftMarginSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                preferences.statusBarMarginLeft = progress
                binding.leftMarginLabel.text = getString(R.string.left_margin_dp, preferences.statusBarMarginLeft)
                Log.d("servicesdd", "Broadcast sent ff!")
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        binding.rightMarginSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                preferences.statusBarMarginRight = progress
                binding.rightMarginLabel.text = getString(R.string.right_margin_dp, preferences.statusBarMarginRight)
                Log.d("servicesdd", "Broadcast sent gg!")
                sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })


        binding.openColorPalate.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.status_bar))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(
                    "Conform",
                    ColorEnvelopeListener { envelope, fromUser -> setLayoutColor(envelope) })
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
        }

        // Create dummy data
        val items = ArrayList<CustomIconGridItem>().apply {
            add(CustomIconGridItem(R.drawable.ic_signal_wifi, getString(R.string.wifi)))
            add(CustomIconGridItem(R.drawable.ic_signal_date, getString(R.string.data)))
            add(CustomIconGridItem(R.drawable.ic_signal_mobile, getString(R.string.signals)))
            add(CustomIconGridItem(R.drawable.ic_airplan_mod, getString(R.string.airplane)))
            add(CustomIconGridItem(R.drawable.ic_signal_hotspot, getString(R.string.hotspot)))
            add(CustomIconGridItem(R.drawable.ic_time_date, getString(R.string.time)))
        }

        val adapter = CustomIconGridAdapter(items) { position, label ->
            val intent = Intent(this, StatusBarIconSettingsActivity::class.java)
            intent.putExtra("EXTRA_POSITION", position)
            intent.putExtra("EXTRA_LABEL", label)
            startActivity(intent)
        }
        binding.recyclerViewCustomIcon.adapter = adapter
        binding.recyclerViewCustomIcon.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerViewCustomIcon.adapter = adapter
    }

    private fun setLayoutColor(envelope: ColorEnvelope?) {
        preferences.statusBarBgColor = envelope?.color!!
        sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))

    }
    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(
                this,
                "Please enable accessibility service",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }else{
            preferences.isStatusBarEnabled = true
            binding.switchEnableBatteryEmoji.isChecked = true

        }
        // else, do nothing or show UI as normal
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName =
            "${this.packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':')
            .any { it.equals(expectedComponentName, ignoreCase = true) }
    }


    override fun onResume() {
        binding.switchEnableBatteryEmoji.isChecked = isAccessibilityServiceEnabled() && preferences.isStatusBarEnabled
        super.onResume()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}