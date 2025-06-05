package com.lowbyte.battery.animation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}