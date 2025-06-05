package com.lowbyte.battery.animation

import android.app.Application
import android.content.Context
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.LocaleHelper

class MyApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        val newBase = base?.let { LocaleHelper.setLocale(it, LocaleHelper.getLanguage(it)) }
        super.attachBaseContext(newBase)
    }

    override fun onCreate() {
        super.onCreate()
        AppPreferences.getInstance(this)
        // This is optional now, attachBaseContext already sets locale early
    }
}