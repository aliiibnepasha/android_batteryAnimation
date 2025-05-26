package com.lowbyte.battery.animation

import android.app.Application
import com.lowbyte.battery.animation.utils.AppPreferences

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.getInstance(this)
    }
}