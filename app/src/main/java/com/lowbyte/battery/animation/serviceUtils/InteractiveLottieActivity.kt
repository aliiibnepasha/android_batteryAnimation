package com.lowbyte.battery.animation.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.NotchAccessibilityService
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.AllowAccessibilityActivity
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.custom.InteractiveLottieView
import com.lowbyte.battery.animation.databinding.ActivityInteractiveLottieBinding
import com.lowbyte.battery.animation.dialoge.AccessibilityPermissionBottomSheet
import com.lowbyte.battery.animation.serviceUtils.AllLottieAdapter
import com.lowbyte.battery.animation.serviceUtils.LottieItem
import com.lowbyte.battery.animation.serviceUtils.OnItemInteractionListener
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.getFullscreenHome2Id
import com.lowbyte.battery.animation.utils.AnimationUtils.isFullscreenStatusEnabled
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.AppPreferences.Companion.KEY_SHOW_LOTTIE_TOP_VIEW
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils

class InteractiveLottieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInteractiveLottieBinding
    private lateinit var preferences: AppPreferences
    private lateinit var lotteSelectedAdapter: LottieItemAdapter
    private lateinit var interactiveLottieView: InteractiveLottieView
    private val lottieItems = ArrayList<LottieItem>() // For adapter display only
    private val availableLottieFiles = mutableListOf<Int>() // Declare empty list
    private lateinit var sheet: AccessibilityPermissionBottomSheet // Declare the sheet



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInteractiveLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

                )
        AdManager.loadInterstitialAd(this, getFullscreenHome2Id(), isFullscreenStatusEnabled)

        sheet = AccessibilityPermissionBottomSheet(
            onAllowClicked = {
                FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_lotti", null)
                startActivity(Intent(this, AllowAccessibilityActivity::class.java))
            },
            onCancelClicked = {
                preferences.setBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, false)
                binding.btnActivateSelected.text = getString( R.string.turn_off)
                FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_lotti", null)

            }, onDismissListener = {
                if (!isAccessibilityServiceEnabled()) {
                    preferences.setBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, false)
                    binding.btnActivateSelected.text = getString( R.string.turn_off)

                }

            }
        )

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AdManager.showInterstitialAd(
                    this@InteractiveLottieActivity,
                    isFullscreenStatusEnabled,
                    true
                ) {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)



        availableLottieFiles.addAll((1..56).map {
            resources.getIdentifier("lottie_$it", "raw", packageName)
        })
        // Get the InteractiveLottieView from the layout instead of creating a new one
        interactiveLottieView = binding.customView

        setupToggleButton()
        setupRecyclerViews()
        setupSeekBars()
        setupMovementControls()
        setupAllLotteries()
        setupInteractionListener()
        loadItemsFromPreferences()
        
        // Initialize controls as disabled
        enableControls(false)
    }

    override fun onPause() {
        super.onPause()
        saveItemsToPreferences()
        isEditing(false) // activity not visible
    }

    override fun onResume() {
        isEditing(true) // activity visible
        super.onResume()
    }

    private fun setupToggleButton() {
        val isVisible = preferences.getBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, false)?:false
        binding.btnActivateSelected.text = getString(if (isVisible) R.string.turn_off else R.string.turn_on)
        binding.btnActivateDisable.setOnClickListener {
            finish()
        }

        binding.btnActivateSelected.setOnClickListener {
            checkAccessibilityPermission()
        }
    }

    private fun setupRecyclerViews() {
        lotteSelectedAdapter = LottieItemAdapter(
            lottieItems,
            onRemove = { lottieItem ->
                interactiveLottieView.removeItemByResId(lottieItem.resId)
            },
            onSelect = { lottieItem ->
                interactiveLottieView.selectItem(lottieItem, "clickFromActivity")
            }
        )
        binding.recyclerLotties.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerLotties.adapter = lotteSelectedAdapter
    }

    private fun setupSeekBars() {
        binding.seekbarSize.max = 200
        binding.seekbarSize.progress = 100
        binding.seekbarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && interactiveLottieView.getSelectedItem() != null) {
                    val scale = progress / 100f
                    interactiveLottieView.scaleSelectedItem(scale)
                    Log.d("InteractiveLottieActivity", "Scaling selected item to: $scale")
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekbarRotation.max = 360
        binding.seekbarRotation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && interactiveLottieView.getSelectedItem() != null) {
                    val rotation = progress.toFloat()
                    interactiveLottieView.rotateSelectedItem(rotation)
                    Log.d("InteractiveLottieActivity", "Rotating selected item to: $rotation")
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupMovementControls() {
        binding.btnMoveTop.setOnClickListener {
            if (interactiveLottieView.getSelectedItem() != null) {
                interactiveLottieView.moveSelectedItem(0, -10)
                Log.d("InteractiveLottieActivity", "Moving selected item up")
            }
        }
        binding.btnMoveBottom.setOnClickListener {
            if (interactiveLottieView.getSelectedItem() != null) {
                interactiveLottieView.moveSelectedItem(0, 10)
                Log.d("InteractiveLottieActivity", "Moving selected item down")
            }
        }
        binding.btnMoveLeft.setOnClickListener {
            if (interactiveLottieView.getSelectedItem() != null) {
                interactiveLottieView.moveSelectedItem(-10, 0)
                Log.d("InteractiveLottieActivity", "Moving selected item left")
            }
        }
        binding.btnMoveRight.setOnClickListener {
            if (interactiveLottieView.getSelectedItem() != null) {
                interactiveLottieView.moveSelectedItem(10, 0)
                Log.d("InteractiveLottieActivity", "Moving selected item right")
            }
        }
    }

    private fun setupAllLotteries() {
        binding.recyclerAllLotties.layoutManager = GridLayoutManager(this, 4)
        val allLottieAdapter = AllLottieAdapter(availableLottieFiles) { resId ->
            if (interactiveLottieView.containsItem(resId)) {
                Toast.makeText(this, getString(R.string.already_added), Toast.LENGTH_SHORT).show()
                return@AllLottieAdapter
            }

            val success = interactiveLottieView.addLottieItem(resId)
            if (!success) {
                Toast.makeText(this, getString(R.string.limit_reached), Toast.LENGTH_SHORT).show()
            }
        }
        binding.recyclerAllLotties.adapter = allLottieAdapter
    }

    private fun setupInteractionListener() {
        interactiveLottieView.itemInteractionListener = object : OnItemInteractionListener {
            override fun onItemSelected(resId: Int?) {
                // Update seekbar values when item is selected
                if (resId != null) {
                    val selectedItem = interactiveLottieView.getSelectedItem()
                    selectedItem?.let { item ->
                        // Update size seekbar
                        val scalePercent = (item.view.scaleX * 100).toInt()
                        binding.seekbarSize.progress = scalePercent.coerceIn(0, 200)
                        
                        // Update rotation seekbar
                        val rotationDegrees = item.view.rotation.toInt()
                        binding.seekbarRotation.progress = rotationDegrees.coerceIn(0, 360)
                    }
                    enableControls(true)
                } else {
                    // Clear seekbar values when no item is selected
                    binding.seekbarSize.progress = 100
                    binding.seekbarRotation.progress = 0
                    enableControls(false)
                }
            }

            override fun onItemCountChanged(items: List<LottieItem>) {
                lotteSelectedAdapter.updateItems(items)
                binding.tvAddEmoji.text = getString(R.string.sticker_added_5, items.size)
            }
        }
    }

    private fun enableControls(enabled: Boolean) {
        binding.seekbarSize.isEnabled = enabled
        binding.seekbarRotation.isEnabled = enabled
        binding.btnMoveTop.isEnabled = enabled
        binding.btnMoveBottom.isEnabled = enabled
        binding.btnMoveLeft.isEnabled = enabled
        binding.btnMoveRight.isEnabled = enabled
        
        // Visual feedback
        val alpha = if (enabled) 1.0f else 0.5f
        binding.seekbarSize.alpha = alpha
        binding.seekbarRotation.alpha = alpha
        binding.btnMoveTop.alpha = alpha
        binding.btnMoveBottom.alpha = alpha
        binding.btnMoveLeft.alpha = alpha
        binding.btnMoveRight.alpha = alpha
    }

    private fun saveItemsToPreferences() {
        val dataList = interactiveLottieView.getCurrentLottieItemData()
        preferences.putLottieItemList("lottie_item_list", dataList)
    }

    private fun loadItemsFromPreferences() {
        val savedItems = preferences.getLottieItemList("lottie_item_list")
        savedItems.forEach { interactiveLottieView.addLottieItemFromData(it) }
    }

    fun isEditing(isEditing: Boolean){
        val intent = Intent(BROADCAST_ACTION).apply {
            putExtra("isEditing", isEditing)

        }
        sendBroadcast(intent)
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_lotte_shown", null)
//            if (BuildConfig.DEBUG){
//                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
//            }else{
            val existing = supportFragmentManager.findFragmentByTag("accessibility_lotti")
            if (existing == null || !existing.isAdded) {
                sheet.show(supportFragmentManager, "accessibility_lotti")
            } else {
                Log.d("Accessibility", "accessibility_lotti already shown")
            }
            //   }
        } else {
            FirebaseAnalyticsUtils.logClickEvent(this, "accessibility_lotti", null)
            val currentState = preferences.getBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, false)
            val newState = !(currentState ?: true)
            preferences.setBoolean(KEY_SHOW_LOTTIE_TOP_VIEW, newState)
            binding.btnActivateSelected.text = getString(
                if (newState) R.string.turn_off else R.string.turn_on
            )
            isEditing(true)
        }
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

}