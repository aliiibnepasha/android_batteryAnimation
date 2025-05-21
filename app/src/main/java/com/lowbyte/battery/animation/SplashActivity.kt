package com.lowbyte.battery.animation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.lowbyte.battery.animation.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_DURATION = 5000L // 10 seconds
    private val UPDATE_INTERVAL = 100L // Update every 100ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val progressBar = binding.progressBar
        progressBar.max = 100

        // Start progress animation
        val handler = Handler(Looper.getMainLooper())
        var progress = 0

        val runnable = object : Runnable {
            override fun run() {
                progress += 1
                progressBar.progress = progress

                if (progress < 100) {
                    handler.postDelayed(this, UPDATE_INTERVAL)
                } else {
                    // Navigate to MainActivity after 10 seconds
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }

        handler.post(runnable)
    }
} 