package com.lowbyte.battery.animation.activity

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences.getInstance(this)

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

        Log.i("ITEMCLICK", "$position $label")

        val resId = resources.getIdentifier(label, "drawable", packageName)
        binding.previewWidgetView.setImageResource(
            if (resId != 0) resId else R.drawable.emoji_4
        )

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            finish()
        }

        binding.buttonForApply.setOnClickListener {
            val widgetId = position
            preferences.saveStyleForWidget(widgetId, position, label)

            val appWidgetManager = AppWidgetManager.getInstance(this)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val widgetProvider = ComponentName(this, BatteryWidgetProvider::class.java)
                val pinIntent = Intent(this, BatteryWidgetProvider::class.java)
                val successCallback = PendingIntent.getBroadcast(
                    this, 0, pinIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                appWidgetManager.requestPinAppWidget(widgetProvider, null, successCallback)
            }

            val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val batteryScale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

            val batteryStatusIntent = Intent(this, BatteryLevelReceiver::class.java).apply {
                putExtra(BatteryManager.EXTRA_LEVEL, batteryLevel)
                putExtra(BatteryManager.EXTRA_SCALE, batteryScale)
            }
            sendBroadcast(batteryStatusIntent)
        }
        binding.buttonSetAsEmoji.setOnClickListener {
            Log.d("BUTTON", "Home clicked")
            preferences.customIconName = label
            sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
        }
    }
}