package com.lowbyte.battery.animation.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.lowbyte.battery.animation.databinding.ActivityProBinding
import com.lowbyte.battery.animation.BuildConfig

class ProActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG){
            Log.d("deguv","deguv")
        }
    }
}