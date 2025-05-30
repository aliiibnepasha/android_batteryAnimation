package com.lowbyte.battery.animation.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ibBackButton.setOnClickListener {
            finish() // Back button behavior
        }

        binding.proView.setOnClickListener {
            // Open Premium screen
        }

        binding.ivNextDark.setOnCheckedChangeListener { _, isChecked ->
            // Toggle dark mode here
        }

        binding.viewTerms.setOnClickListener {
            // Open Terms of Service
        }

        binding.viewPrivacy.setOnClickListener {
            // Open Privacy Policy
        }

        binding.viewLanguage.setOnClickListener {
            // startActivity(Intent(this, LanguageActivity::class.java)))
        }
    }
}