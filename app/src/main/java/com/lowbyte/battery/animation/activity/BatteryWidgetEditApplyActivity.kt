package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.R
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
        // Read intent extras
         position = intent.getIntExtra("EXTRA_POSITION", -1)
         label = intent.getStringExtra("EXTRA_LABEL") ?: getString(R.string.wifi)
        Log.i("ITEMCLICK", "$position $label")

        Log.i("ITEMCLICK", "$position $label")
        val resId = resources.getIdentifier(label, "drawable", packageName)
        if (resId != 0) {
            binding.previewWidgetView.setImageResource(resId)
        } else {
            binding.previewWidgetView.setImageResource(R.drawable.emoji_4)
        }

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
        }

        binding.buttonHome.setOnClickListener {
            Log.d("BUTTON", "Home clicked")
        }
    }
}