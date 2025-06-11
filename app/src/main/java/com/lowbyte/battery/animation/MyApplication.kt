package com.lowbyte.battery.animation

import android.app.Application
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.lowbyte.battery.animation.ads.AppOpenAdManager
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()

        AppPreferences.getInstance(this)
        appOpenAdManager = AppOpenAdManager(this)

        val lang = LocaleHelper.getLanguage(this)
        if (lang.isEmpty() || lang.isBlank()) {
            LocaleHelper.setLocale(this, "en")
        } else {
            LocaleHelper.setLocale(this, lang)
        }

        MobileAds.initialize(this) // Optional but recommended if not done elsewhere
    }
}