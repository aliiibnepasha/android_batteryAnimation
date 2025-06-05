package com.lowbyte.battery.animation.activity

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.broadcastReciver.BatteryLevelReceiver
import com.lowbyte.battery.animation.broadcastReciver.BatteryWidgetProvider
import com.lowbyte.battery.animation.databinding.ActivityBatteryWidgetEditApplyBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryWidgetEditApplyActivity : BaseActivity() {

    private lateinit var binding: ActivityBatteryWidgetEditApplyBinding
    private lateinit var preferences: AppPreferences
    private var position: Int = -1
    private lateinit var label: String
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var isNewWidget: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences.getInstance(this)

        // Get the widget ID from the intent
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        // Check if this is a new widget creation
        isNewWidget = appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID
        enableEdgeToEdge()

        binding = ActivityBatteryWidgetEditApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        position = intent.getIntExtra("EXTRA_POSITION", -1)
        label = intent.getStringExtra("EXTRA_LABEL") ?: getString(R.string.wifi)

        Log.e("BatteryWidgetEditApplyActivity", "Widget ID: $appWidgetId, Position: $position, Label: $label, IsNew: $isNewWidget")

        // Load saved widget data if it exists and this is not a new widget
        if (!isNewWidget) {
            val savedIcon = preferences.getWidgetIcon(appWidgetId)
            if (savedIcon.isNotEmpty()) {
                label = savedIcon
                Log.e("BatteryWidgetEditApplyActivity", "Loaded saved icon for widget $appWidgetId: $savedIcon")
            }
        }

        // Load and display the preview image
        loadAndDisplayImage()

        setupClickListeners()
    }

    private fun loadAndDisplayImage() {
        val resId = resources.getIdentifier(label, "drawable", packageName)
        Log.e("BatteryWidgetEditApplyActivity", "Loading image for label: $label, resId: $resId")
        if (resId == 0) {
            Log.e("BatteryWidgetEditApplyActivity", "Failed to find drawable resource for label: $label")
        }
        binding.previewWidgetView.setImageResource(
            if (resId != 0) resId else R.drawable.emoji_4
        )
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            if (isNewWidget) {
                setResult(RESULT_CANCELED)
            }
            finish()
        }

        binding.buttonForApply.setOnClickListener {
            try {
                if (isNewWidget) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // For new widget, request pinning
                        val appWidgetManager = AppWidgetManager.getInstance(this)
                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            val widgetProvider = ComponentName(this, BatteryWidgetProvider::class.java)
                            
                            // Save the widget icon before requesting pin
                            preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, label)
                            Log.e("BatteryWidgetEditApplyActivity", "Saved widget icon: $label for new widget")
                            
                            // Verify the saved icon
                            val savedIcon = preferences.getWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID)
                            Log.e("BatteryWidgetEditApplyActivity", "Verified saved icon: $savedIcon")
                            
                            // Create a bundle to pass the widget icon
                            val options = Bundle().apply {
                                putString("WIDGET_ICON", label)
                            }
                            
                            // Create the success callback intent
                            val successIntent = Intent(this, BatteryWidgetProvider::class.java).apply {
                                action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                putExtra("WIDGET_ICON", label)
                            }
                            
                            val successCallback = PendingIntent.getBroadcast(
                                this, 0, successIntent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            
                            // Request to pin the widget
                            appWidgetManager.requestPinAppWidget(widgetProvider, options, successCallback)
                            Log.e("BatteryWidgetEditApplyActivity", "Requested pinning for new widget with icon: $label")
                            
                            // Send an immediate update broadcast
                            val updateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                                action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                putExtra("WIDGET_ICON", label)
                            }
                            sendBroadcast(updateIntent)
                            Log.e("BatteryWidgetEditApplyActivity", "Sent update broadcast with icon: $label")
                            
                            // Send multiple delayed update broadcasts to ensure the widget is created and updated
                            for (delay in listOf(500, 1000, 2000)) {
                                binding.root.postDelayed({
                                    // Get all widget IDs
                                    val widgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)
                                    Log.e("BatteryWidgetEditApplyActivity", "Found ${widgetIds.size} widgets after $delay ms delay")
                                    
                                    if (widgetIds.isNotEmpty()) {
                                        // Get the most recently created widget ID
                                        val newWidgetId = widgetIds.last()
                                        Log.e("BatteryWidgetEditApplyActivity", "Using widget ID: $newWidgetId for delayed update")
                                        
                                        // Save the icon with the actual widget ID
                                        preferences.saveWidgetIcon(newWidgetId, label)
                                        Log.e("BatteryWidgetEditApplyActivity", "Saved icon: $label for widget ID: $newWidgetId")
                                        
                                        // Verify the saved icon
                                        val savedIcon = preferences.getWidgetIcon(newWidgetId)
                                        Log.e("BatteryWidgetEditApplyActivity", "Verified saved icon for widget $newWidgetId: $savedIcon")
                                        
                                        // Send update broadcast with the widget ID
                                        val delayedUpdateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                                            action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newWidgetId)
                                            putExtra("WIDGET_ICON", label)
                                        }
                                        sendBroadcast(delayedUpdateIntent)
                                        Log.e("BatteryWidgetEditApplyActivity", "Sent delayed update broadcast for widget $newWidgetId with icon: $label")
                                        
                                        // Force an immediate widget update
                                        appWidgetManager.notifyAppWidgetViewDataChanged(newWidgetId, R.id.battery_icon)
                                    }
                                }, delay.toLong())
                            }
                        }
                    } else {
                        Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // For existing widget, update it
                    preferences.saveWidgetIcon(appWidgetId, label)
                    Log.e("BatteryWidgetEditApplyActivity", "Saved widget icon: $label for widget ID: $appWidgetId")
                    
                    // Verify the saved icon
                    val savedIcon = preferences.getWidgetIcon(appWidgetId)
                    Log.e("BatteryWidgetEditApplyActivity", "Verified saved icon for widget $appWidgetId: $savedIcon")
                    
                    val updateIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                        action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        putExtra("WIDGET_ICON", label)
                    }
                    sendBroadcast(updateIntent)
                    Log.e("BatteryWidgetEditApplyActivity", "Sent update broadcast for existing widget $appWidgetId with icon: $label")
                    
                    // Force an immediate widget update
                    val appWidgetManager = AppWidgetManager.getInstance(this)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.battery_icon)
                }

                Toast.makeText(this,
                    getString(R.string.widget_applied_successfully), Toast.LENGTH_SHORT).show()

                if (!isNewWidget) {
                    val resultIntent = Intent().apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("BatteryWidgetEditApplyActivity", "Error in buttonForApply click", e)
                e.printStackTrace()
            }
        }

        binding.buttonSetAsEmoji.setOnClickListener {
            preferences.customIconName = label
            sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            Toast.makeText(this, getString(R.string.emoji_applied_successfully), Toast.LENGTH_SHORT).show()
        }
    }
}