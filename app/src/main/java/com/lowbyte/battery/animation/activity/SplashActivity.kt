package com.lowbyte.battery.animation.activity

import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.MyApplication
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@SplashActivity) {}
        }
        AdManager.initializeAds(this)
    }

    override fun onResume() {
        super.onResume()
    }
}