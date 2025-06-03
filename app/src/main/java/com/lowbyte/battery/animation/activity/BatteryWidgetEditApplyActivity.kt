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
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.broadcastReciver.BatteryLevelReceiver
import com.lowbyte.battery.animation.broadcastReciver.BatteryWidgetProvider
import com.lowbyte.battery.animation.databinding.ActivityBatteryWidgetEditApplyBinding
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryWidgetEditApplyActivity : AppCompatActivity() {

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

        Log.i(
            "ITEM_CLICK",
            "Widget ID: $appWidgetId, Position: $position, Label: $label, IsNew: $isNewWidget"
        )

        // Load saved widget data if it exists and this is not a new widget
        if (!isNewWidget) {
            val savedIcon = preferences.getWidgetIcon(appWidgetId)
            if (savedIcon.isNotEmpty()) {
                label = savedIcon
            }
        }

        val resId = resources.getIdentifier(label, "drawable", packageName)
        binding.previewWidgetView.setImageResource(
            if (resId != 0) resId else R.drawable.emoji_4
        )

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            if (isNewWidget) {
                setResult(RESULT_CANCELED)
            }
            finish()
        }

        binding.buttonForApply.setOnClickListener {
            // Always save the widget icon first
            preferences.saveWidgetIcon(appWidgetId, label)

            if (isNewWidget) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // For new widget, request pinning
                    val appWidgetManager = AppWidgetManager.getInstance(this)
                    if (appWidgetManager.isRequestPinAppWidgetSupported) {
                        val widgetProvider = ComponentName(this, BatteryWidgetProvider::class.java)
                        val pinIntent = Intent(this, BatteryWidgetProvider::class.java).apply {
                            putExtra("WIDGET_ICON", label) // Pass the icon name to the provider
                        }
                        val successCallback = PendingIntent.getBroadcast(
                            this, 0, pinIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        appWidgetManager.requestPinAppWidget(widgetProvider, null, successCallback)

                    }
                } else {
                    Toast.makeText(this, "Device not supported", Toast.LENGTH_SHORT).show()
                }

            } else {
                updateWidget()
            }
        }

        binding.buttonSetAsEmoji.setOnClickListener {
            preferences.customIconName = label
            sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            Toast.makeText(this, "Emoji Applied Successfully.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWidget() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val batteryScale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        val batteryStatusIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, batteryLevel)
            putExtra(BatteryManager.EXTRA_SCALE, batteryScale)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("WIDGET_ICON", label) // Pass the icon name
        }
        sendBroadcast(batteryStatusIntent)

        Toast.makeText(this, "Widget Applied Successfully.", Toast.LENGTH_SHORT).show()

        if (!isNewWidget) {
            val resultIntent = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}