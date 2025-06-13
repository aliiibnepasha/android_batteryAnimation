package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.databinding.ActivityApplySuccessfullyBinding
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils // Make sure this exists

class ApplySuccessfullyActivity : BaseActivity() {

    private lateinit var binding: ActivityApplySuccessfullyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityApplySuccessfullyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAnalyticsUtils.logScreenView(this, "ApplySuccessfullyScreen")
        FirebaseAnalyticsUtils.startScreenTimer("ApplySuccessfullyScreen")

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonHome.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_home_button", mapOf("source" to "ApplySuccessfullyScreen"))
            finish()
        }

        binding.buttonCustomizeAgain.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_customize_again", mapOf("source" to "ApplySuccessfullyScreen"))
            startActivity(Intent(this, StatusBarCustomizeActivity::class.java))
            finish()
        }

        binding.actionClose.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_close_button", mapOf("source" to "ApplySuccessfullyScreen"))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "ApplySuccessfullyScreen")
    }
}