package com.lowbyte.battery.animation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lowbyte.battery.animation.databinding.ActivityApplySuccessfullyBinding

class ApplySuccessfullyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplySuccessfullyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityApplySuccessfullyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonHome.setOnClickListener {
            finish()
            // Save settings or apply logic here
        }
        binding.buttonCustomizeAgain.setOnClickListener {
            startActivity(Intent(this, StatusBarCustomizeActivity::class.java))
            finish()

            // Save settings or apply logic here
        }
        binding.actionClose.setOnClickListener {
            finish()
            // Save settings or apply logic here
        }
    }
}