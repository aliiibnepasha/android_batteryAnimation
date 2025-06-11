package com.lowbyte.battery.animation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lowbyte.battery.animation.databinding.ActivityProBinding

class ProActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

}