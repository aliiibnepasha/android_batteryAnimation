package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.AllColorsAdapter
import com.lowbyte.battery.animation.adapter.AllIconsAdapter
import com.lowbyte.battery.animation.databinding.ActivityStatusBarIconSettingsBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.model.IconItem
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.dp
import com.lowbyte.battery.animation.utils.AnimationUtils.solidColors
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.PermissionUtils.checkAccessibilityPermission
import com.lowbyte.battery.animation.utils.PermissionUtils.isAccessibilityServiceEnabled
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class StatusBarIconSettingsActivity : BaseActivity() {
    private lateinit var allColorsAdapter: AllColorsAdapter
    private lateinit var adapter: AllIconsAdapter
    private lateinit var glm: GridLayoutManager
    private var categoryTitle: String = ""
    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet
    private lateinit var binding: ActivityStatusBarIconSettingsBinding
    private lateinit var preferences: AppPreferences

    private var position: Int = -1
    private lateinit var label: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBarIconSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        sheet = AccessibilityPermissionBottomSheet(
            onAllowClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    this,
                    "allow_accessibility_click"
                )
                startActivity(Intent(this, AllowAccessibilityActivity::class.java))
            },
            onCancelClicked = {
                FirebaseAnalyticsUtils.logClickEvent(
                    this,
                    "cancel_accessibility_permission"
                )
                preferences.isStatusBarEnabled = false
                binding.switchEnableBatteryEmojiViewAll.isChecked = false
            }, onDismissListener = {
                if (!isAccessibilityServiceEnabled()) {
                    preferences.isStatusBarEnabled = false
                    binding.switchEnableBatteryEmojiViewAll.isChecked = false
                }

            }
        )
        binding.switchEnableBatteryEmojiViewAll.isChecked =
            preferences.isStatusBarEnabled && isAccessibilityServiceEnabled()
        binding.switchEnableBatteryEmojiViewAll.setOnCheckedChangeListener { _, isChecked ->
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isDestroyed && !isFinishing) {
                    preferences.isStatusBarEnabled = isChecked
                    // Log toggle
                    FirebaseAnalyticsUtils.logClickEvent(
                        this,
                        "toggle_statusbar_emoji_from_emoji_screen",
                        mapOf("enabled" to isChecked.toString())
                    )
                    if (preferences.isStatusBarEnabled && isChecked) {
                        checkAccessibilityPermission(false) {
                            when (it) {
                                "OpenBottomSheet" -> {
                                    sheet.show(supportFragmentManager, "AccessibilityPermission")
                                }

                                "Allowed" -> {
                                    binding.switchEnableBatteryEmojiViewAll.isChecked =
                                        preferences.isStatusBarEnabled
                                    sendBroadcast(Intent(BROADCAST_ACTION))
                                }

                                else -> {
                                    val existing =
                                        supportFragmentManager.findFragmentByTag("AccessibilityPermission")
                                    if (existing == null || !existing.isAdded) {
                                        sheet.show(
                                            supportFragmentManager,
                                            "AccessibilityPermission"
                                        )
                                    } else {
                                        Log.d(
                                            "Accessibility",
                                            "AccessibilityPermissionBottomSheet already shown"
                                        )
                                    }
                                }
                            }

                        }
                    } else {
                        sendBroadcast(Intent(BROADCAST_ACTION))
                    }

                }

            }, 500)
        }
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
        binding.openColorPalate.setOnClickListener {
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
        binding.seekBarIconSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                applyIconSize("size_$position", progress)
                binding.labelIconSize.text = getString(R.string.size_dp, label, progress)
//                FirebaseAnalyticsUtils.logClickEvent(this@StatusBarIconSettingsActivity, "change_icon_size", mapOf(
//                    "position" to position.toString(),
//                    "size" to progress.toString(),
//                    "label" to label
//                ))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!preferences.isStatusBarEnabled) {
                    Toast.makeText(this@StatusBarIconSettingsActivity,
                        getString(R.string.please_enable_battery_emoji_service),
                        Toast.LENGTH_LONG).show()
                }
            }
        })
        binding.colorsList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        allColorsAdapter = AllColorsAdapter({ position, fileItem, isRewarded ->

        })
        binding.colorsList.adapter = allColorsAdapter
        allColorsAdapter.submitList(solidColors)
        setupIconsRecyclerView()
    }

    private fun setupIconsRecyclerView() {
        val spaceHPx = 5.dp(this) // top & bottom spacer height
        val spacePPx = 65.dp(this) // top & bottom spacer height

        adapter = AllIconsAdapter(
            { position, fileItem, isRewarded ->
                FirebaseAnalyticsUtils.logClickEvent(
                    this,
                    "emoji_selected",
                    mapOf(
                        "tab_index" to "currentPos",
                        "emoji_label" to fileItem.iconName,
                        "emoji_position" to position.toString()
                    )
                )
//                val intent = Intent(this, EmojiEditApplyActivity::class.java).apply {
//                        putExtra(EXTRA_POSITION, position)
//                        putExtra(EXTRA_LABEL, fileItem.name)
//                        putExtra("categoryTitle", categoryTitle)
//                    }
//                    startActivity(intent)

            },
            headerHeightPx = spaceHPx,
            footerHeightPx = spacePPx,
        )

        glm = GridLayoutManager(this, 4).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0 || position == adapter.itemCount - 1) {
                        4
                    } else {
                        1
                    }
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = glm
            adapter = this@StatusBarIconSettingsActivity.adapter
        }

        val firstVisible = glm.findFirstVisibleItemPosition()
        val offset = binding.recyclerView.getChildAt(0)?.top ?: 0
        val listOfIcons = listOf(
            IconItem(
                iconName = "ic_airplan_mod",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_date",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_hotspot",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_mobile",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_wifi",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_wifi",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_airplan_mod",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_date",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_hotspot",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_mobile",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_wifi",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_wifi",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_airplan_mod",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_date",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_hotspot",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_mobile",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_wifi",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_wifi",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_airplan_mod",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_date",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_hotspot",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_mobile",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_wifi",
                title = "",
                position = 0,
                tag = ""
            ),
            IconItem(
                iconName = "ic_signal_wifi",
                title = "",
                position = 0,
                tag = ""
            ),


            )
        adapter.submitList(listOfIcons) {
            glm.scrollToPositionWithOffset(firstVisible, offset)
        }

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