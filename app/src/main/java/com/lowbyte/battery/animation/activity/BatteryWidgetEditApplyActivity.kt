package com.lowbyte.battery.animation.activity

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.adapter.BatteryWidgetProvider
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
            preferences.statusLottieName = label
            sendBroadcast(Intent("com.lowbyte.UPDATE_STATUSBAR"))
            Log.d("BUTTON", "Apply clicked")

            // 🧠 Pin the widget to home screen
            val appWidgetManager = AppWidgetManager.getInstance(this)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val widgetProvider = ComponentName(this, BatteryWidgetProvider::class.java)

                val pinIntent = Intent(this, BatteryWidgetProvider::class.java)
                val successCallback = PendingIntent.getBroadcast(
                    this, 0, pinIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                appWidgetManager.requestPinAppWidget(widgetProvider, null, successCallback)
                Toast.makeText(this, "Widget pin request sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Pinning widgets not supported on this device", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonHome.setOnClickListener {
            Log.d("BUTTON", "Home clicked")
        }
    }
}