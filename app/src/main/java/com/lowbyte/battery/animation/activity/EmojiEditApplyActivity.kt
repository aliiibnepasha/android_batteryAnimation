package com.lowbyte.battery.animation.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.AllColorsAdapter
import com.lowbyte.battery.animation.adapter.AllEmojiBatteriesAdapter
import com.lowbyte.battery.animation.adapter.AllEmojiStickersAdapter
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivityEmojiEditApplayBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.server.EmojiViewModel
import com.lowbyte.battery.animation.server.EmojiViewModelFactory
import com.lowbyte.battery.animation.server.Resource
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AnimationUtils.dataUrl
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenId
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenApplyEmojiEnabled
import com.lowbyte.battery.animation.utils.AnimationUtils.solidColors
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.PermissionUtils.checkAccessibilityPermission
import com.lowbyte.battery.animation.utils.PermissionUtils.isAccessibilityServiceEnabled
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.launch

class EmojiEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityEmojiEditApplayBinding
    private lateinit var preferences: AppPreferences
    private lateinit var drawable: String
    private lateinit var allColorsAdapter: AllColorsAdapter
    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet
    private val vm: EmojiViewModel by viewModels { EmojiViewModelFactory(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmojiEditApplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        AdManager.loadInterstitialAd(this, getFullscreenId(),isFullscreenApplyEmojiEnabled)
        val categoryTitle = intent.getStringExtra("categoryTitle") ?: ""
        Log.d("APIData", "Categories List: $categoryTitle")
        binding.tvTitle.text = categoryTitle

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    vm.singleCategory.collect { state ->
                        if (state is Resource.Success) {
                            val singleCategory = state.data

                            singleCategory.folders.forEach {
                                when (it.name) {
                                    "emoji_battery" -> {
                                        Log.d("APIData", "files List: ${it.files}")
                                        val adapter = AllEmojiBatteriesAdapter(
                                            { position, fileItem, isRewarded ->

                                            },
                                            categoryName = categoryTitle,
                                            folderName = "emoji_battery"
                                        )
                                        binding.batteryListList.layoutManager = LinearLayoutManager(
                                            this@EmojiEditApplyActivity,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                        binding.batteryListList.adapter = adapter
                                        adapter.submitList(it.files)
                                    }

                                    "emoji_sticker" -> {
                                        val adapter = AllEmojiStickersAdapter(
                                            { position, fileItem, isRewarded ->

                                            },
                                            categoryName = categoryTitle,
                                            folderName = "emoji_sticker"
                                        )
                                        binding.cartoonList.layoutManager = LinearLayoutManager(
                                            this@EmojiEditApplyActivity,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                        binding.cartoonList.adapter = adapter
                                        adapter.submitList(it.files)
                                        Log.d("APIData", "files List: ${it.files}")
                                    }

                                    else -> {
                                        Log.d("APIData", "name List: ${it.name}")
                                        Log.d("APIData", "files List: ${it.files}")
                                    }
                                }

                            }


                        }
                    }
                }
            }
        }

        binding.emojiViewALl.setOnClickListener {
            val intent = Intent(this, ViewMoreEmojiActivity::class.java)
            emojiPickerLauncher.launch(intent)
        }

        vm.loadCategory(dataUrl, name = categoryTitle)

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
                if (!isDestroyed && !isFinishing){
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
                                    binding.switchEnableBatteryEmojiViewAll.isChecked = preferences.isStatusBarEnabled
                                    sendBroadcast(Intent(BROADCAST_ACTION))
                                }

                                else -> {
                                    val existing =
                                        supportFragmentManager.findFragmentByTag("AccessibilityPermission")
                                    if (existing == null || !existing.isAdded) {
                                        sheet.show(supportFragmentManager, "AccessibilityPermission")
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

        FirebaseAnalyticsUtils.logScreenView(this, "EmojiEditApplyScreen")
        FirebaseAnalyticsUtils.startScreenTimer("EmojiEditApplyScreen")

        val position = intent.getIntExtra(EXTRA_POSITION, -1)

        drawable = intent.getStringExtra(EXTRA_LABEL) ?: getString(R.string.wifi)

        val resId = resources.getIdentifier(drawable, "drawable", packageName)
        binding.previewEditEmoji.setImageResource(if (resId != 0) resId else R.drawable.emoji_battery_preview_1)

        FirebaseAnalyticsUtils.logClickEvent(this, "emoji_selected", mapOf("drawable" to drawable, "position" to position.toString()))

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", mapOf("screen" to "EmojiEditApplyScreen"))
            finish()
        }

        binding.ifvPro.setOnClickListener {
            startActivity(Intent(this, ProActivity::class.java))
        }

        binding.enableShowBatteryPercentage.isChecked = preferences.showBatteryPercent
        binding.batteryEmojiSeekbarSize.progress = preferences.getIconSize("batteryIcon", 25)
        binding.batteryEmojiPercentageSeekbarSize.progress = preferences.getIconSize("percentageSize", 25)

        binding.batteryEmojiSeekbarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.setIconSize("batteryIconSize", progress)
                sendBroadcast(Intent(BROADCAST_ACTION))
                binding.batteryEmojiLabel.text = getString(
                    R.string.size_dp,
                    getString(R.string.view_all_battery_emoji),
                    progress
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!preferences.isStatusBarEnabled) {
                    Toast.makeText(this@EmojiEditApplyActivity, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.batteryEmojiPercentageSeekbarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.setIconSize("percentageSize", progress)
                sendBroadcast(Intent(BROADCAST_ACTION))
                binding.batteryPercentageEmojiLabel.text = getString(
                    R.string.size_dp,
                    getString(R.string.percentage_size),
                    progress
                )
               // FirebaseAnalyticsUtils.logClickEvent(this@EmojiEditApplyActivity, "change_percentage_size", mapOf("size" to progress.toString()))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!binding.enableShowBatteryPercentage.isChecked) {
                    Toast.makeText(this@EmojiEditApplyActivity, getString(R.string.please_turn_on_battery_percentage), Toast.LENGTH_SHORT).show()
                }
                if (!preferences.isStatusBarEnabled) {
                    Toast.makeText(this@EmojiEditApplyActivity, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.openColorPalate.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.percentage_color))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.apply), ColorEnvelopeListener { envelope, _ ->
                    colorOfIcon(envelope)
                    FirebaseAnalyticsUtils.logClickEvent(this, "color_picker_applied", mapOf("color" to envelope.hexCode))
                })
                .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12)
                .show()
        }
        binding.colorsList.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        allColorsAdapter = AllColorsAdapter({ position, fileItem, isRewarded ->

        })
        binding.colorsList.adapter = allColorsAdapter
        allColorsAdapter.submitList(solidColors)
        binding.enableShowBatteryPercentage.setOnCheckedChangeListener { _, isChecked ->
            preferences.showBatteryPercent = isChecked
            sendBroadcast(Intent(BROADCAST_ACTION))
            FirebaseAnalyticsUtils.logClickEvent(this, "toggle_percentage_display", mapOf("enabled" to isChecked.toString()))
        }

        binding.btnNext.setOnClickListener {

            if (!preferences.isStatusBarEnabled) {
                Toast.makeText(
                    this,
                    getString(R.string.please_enable_battery_emoji_service),
                    Toast.LENGTH_LONG
                ).show()

            }else{
            FirebaseAnalyticsUtils.logClickEvent(this, "click_apply_emoji", mapOf("drawable" to drawable))
            preferences.batteryIconName = drawable
            sendBroadcast(Intent(BROADCAST_ACTION))
            startActivity(Intent(this, ApplySuccessfullyActivity::class.java))
            finish()


            }
        }
    }

    private val emojiPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    val emojiName = intent.getStringExtra("emojiName")
                    val batteryName = intent.getStringExtra("batteryName")
                    val positon = intent.getIntExtra("positon",0)
                    Log.d("logData", "emojiName: $emojiName batteryName: $batteryName positon: $positon")

                }
            }
        }

    private fun colorOfIcon(envelope: ColorEnvelope) {
        if (!binding.enableShowBatteryPercentage.isChecked) {
            Toast.makeText(this, getString(R.string.please_turn_on_battery_percentage), Toast.LENGTH_SHORT).show()
        }
        if (!preferences.isStatusBarEnabled) {
            Toast.makeText(this, getString(R.string.please_enable_battery_emoji_service), Toast.LENGTH_LONG).show()
        }
        preferences.setInt("percentageColor", envelope.color)
        sendBroadcast(Intent(BROADCAST_ACTION))
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "EmojiEditApplyScreen")
    }
}