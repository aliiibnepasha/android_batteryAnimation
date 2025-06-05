package com.lowbyte.battery.animation

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.lowbyte.battery.animation.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val localeUpdatedContext = LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase))
        super.attachBaseContext(localeUpdatedContext)
    }
}