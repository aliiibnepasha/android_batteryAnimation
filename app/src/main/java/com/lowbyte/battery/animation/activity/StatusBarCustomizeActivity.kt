package com.lowbyte.battery.animation.activity


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.CustomIconGridAdapter
import com.lowbyte.battery.animation.databinding.ActivityStatusBarCustommizeBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.model.CustomIconGridItem
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AppPreferences
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


class StatusBarCustomizeActivity : BaseActivity() {

    private var _binding: ActivityStatusBarCustommizeBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityStatusBarCustommizeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        _binding?.ibBackButton?.setOnClickListener {
            finish()
        }

        _binding?.restoreSetting?.setOnClickListener {
            preferences.setInt("tint_0", Color.BLACK)
            preferences.setInt("tint_1", Color.BLACK)
            preferences.setInt("tint_2", Color.BLACK)
            preferences.setInt("tint_3", Color.BLACK)
            preferences.setInt("tint_4", Color.BLACK)
            preferences.setInt("tint_5", Color.BLACK)
            preferences.setIconSize("size_0", 24)
            preferences.setIconSize("size_1", 24)
            preferences.setIconSize("size_2", 24)
            preferences.setIconSize("size_3", 24)
            preferences.setIconSize("size_4", 24)
            preferences.setIconSize("size_5", 12)
            preferences.statusBarBgColor = Color.LTGRAY
            binding.statusBarHeightSeekbar.progress = 35
            binding.leftMarginSeekBar.progress = 10
            binding.rightMarginSeekBar.progress = 10

        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.statusBarHeightSeekbar.progress = preferences.statusBarHeight
        binding.leftMarginSeekBar.progress = preferences.statusBarMarginLeft
        binding.rightMarginSeekBar.progress = preferences.statusBarMarginRight


        binding.switchEnableBatteryEmojiCustom.isChecked = preferences.isStatusBarEnabled && isAccessibilityServiceEnabled()
        Log.d("TAG_Access", "Create ${preferences.isStatusBarEnabled}")

        binding.switchEnableBatteryEmojiCustom.setOnCheckedChangeListener { _, isChecked ->

            Log.d("TAG_Access", "onCreate check Call $isChecked")
            preferences.isStatusBarEnabled = isChecked

            if (::preferences.isInitialized && preferences.isStatusBarEnabled && isChecked) {
                Log.d(
                    "TAG_Access",
                    "onViewCreated: $isChecked /  ${preferences.isStatusBarEnabled}"
                )
                checkAccessibilityPermission()
            } else {
                Log.d(
                    "TAG_Access",
                    "onViewCreated false: $isChecked / ${preferences.isStatusBarEnabled}"
                )
               sendBroadcast(Intent(BROADCAST_ACTION))
            }
        }


        binding.statusBarHeight.text = getString(R.string.height_dp, preferences.statusBarHeight)
        binding.leftMarginLabel.text = getString(R.string.left_margin_dp, preferences.statusBarMarginLeft)
        binding.rightMarginLabel.text = getString(R.string.right_margin_dp, preferences.statusBarMarginRight)


        binding.statusBarHeightSeekbar.max = 50
        binding.statusBarHeightSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val safeProgress = progress.coerceIn(0, 50)
                preferences.statusBarHeight = safeProgress
                binding.statusBarHeight.text = getString(R.string.height_dp, safeProgress)
                sendBroadcast(Intent(BROADCAST_ACTION))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableBatteryEmojiCustom.isChecked) {
                    Toast.makeText(
                        this@StatusBarCustomizeActivity,
                        getString(R.string.please_enable_battery_emoji_service),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })        /*Status bar height Code */
        binding.leftMarginSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                preferences.statusBarMarginLeft = progress
                binding.leftMarginLabel.text = getString(R.string.left_margin_dp, preferences.statusBarMarginLeft)
                Log.d("servicesdd", "Broadcast sent ff!")
                sendBroadcast(Intent(BROADCAST_ACTION))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableBatteryEmojiCustom.isChecked){
                    Toast.makeText(this@StatusBarCustomizeActivity,
                        getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.rightMarginSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                preferences.statusBarMarginRight = progress
                binding.rightMarginLabel.text = getString(R.string.right_margin_dp, preferences.statusBarMarginRight)
                Log.d("service_sdd", "Broadcast sent gg!")
                sendBroadcast(Intent(BROADCAST_ACTION))

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.switchEnableBatteryEmojiCustom.isChecked){
                    Toast.makeText(this@StatusBarCustomizeActivity,
                        getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })


        binding.customizeStatusBarBgColor.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.status_bar))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(
                    getString(R.string.apply),
                    ColorEnvelopeListener { envelope, fromUser -> setLayoutColor(envelope) })
                .setNegativeButton(
                    getString(R.string.cancel)
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
            intent.putExtra(EXTRA_POSITION, position)
            intent.putExtra(EXTRA_LABEL, label)
            startActivity(intent)
        }
        binding.recyclerViewCustomIcon.adapter = adapter
        binding.recyclerViewCustomIcon.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerViewCustomIcon.adapter = adapter
    }

    private fun setLayoutColor(envelope: ColorEnvelope?) {
        if (!binding.switchEnableBatteryEmojiCustom.isChecked){
            Toast.makeText(this@StatusBarCustomizeActivity,
                getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
        }
        preferences.statusBarBgColor = envelope?.color!!
        sendBroadcast(Intent(BROADCAST_ACTION))

    }


    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            val sheet = AccessibilityPermissionBottomSheet(onAllowClicked = {
                Toast.makeText(
                   this,
                    getString(R.string.please_enable_accessibility_service),
                    Toast.LENGTH_LONG
                ).show()
                Log.d("TAG_Access", "No permission but go for permission")
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }, onCancelClicked = {
                Log.d("TAG_Access", "No permission cancel")
                preferences.isStatusBarEnabled = false
                binding.switchEnableBatteryEmojiCustom.isChecked = false
            })
            sheet.show(supportFragmentManager, "AccessibilityPermission")

        } else {
            Log.d(
                "TAG_Access",
                "Allowed permission enabling checks ${preferences.isStatusBarEnabled}"
            )
            binding.switchEnableBatteryEmojiCustom.isChecked = preferences.isStatusBarEnabled
            sendBroadcast(Intent(BROADCAST_ACTION))


        }
        // else, do nothing or show UI as normal
    }
    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName =
            "${packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':')
            .any { it.equals(expectedComponentName, ignoreCase = true) }
    }


    override fun onResume() {
        super.onResume()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}