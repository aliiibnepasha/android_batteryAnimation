package com.lowbyte.battery.animation.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.adapter.LanguageAdapter
import com.lowbyte.battery.animation.ads.AdManager
import com.lowbyte.battery.animation.ads.NativeLanguageHelper
import com.lowbyte.battery.animation.databinding.ActivityLanguagesBinding
import com.lowbyte.battery.animation.model.Language
import com.lowbyte.battery.animation.utils.AnimationUtils.getNativeLanguageId
import com.lowbyte.battery.animation.utils.AnimationUtils.initialLanguageCode
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.FirebaseAnalyticsUtils
import com.lowbyte.battery.animation.utils.LocaleHelper

class LanguagesActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguagesBinding
    private lateinit var adapter: LanguageAdapter
    private lateinit var preferences: AppPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = AppPreferences.getInstance(this)

        FirebaseAnalyticsUtils.logScreenView(this, "LanguagesScreen")
        FirebaseAnalyticsUtils.startScreenTimer("LanguagesScreen")

        val languages = listOf(
            Language("English", "en"),
            Language("العربية", "ar"),
            Language("Español", "es-rES"),
            Language("Français", "fr-rFR"),
            Language("हिंदी", "hi"),
            Language("Italiano", "it-rIT"),
            Language("日本語", "ja"),
            Language("한국어", "ko"),
            Language("Bahasa Melayu", "ms-rMY"),
            Language("Filipino", "phi"),
            Language("ไทย", "th"),
            Language("Türkçe", "tr-rTR"),
            Language("Tiếng Việt", "vi"),
            Language("Português", "pt-rPT"),
            Language("Bahasa Indonesia", "in")
        )

        val currentLanguageCode = LocaleHelper.getLanguage(this)

        adapter = LanguageAdapter(languages, currentLanguageCode) { language ->
            FirebaseAnalyticsUtils.logClickEvent(
                this,
                "language_selected",
                mapOf("language_name" to language.name, "language_code" to language.code)
            )
            LocaleHelper.setLocale(this, language.code)
            recreate()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.ibBackButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_back_button", mapOf("screen" to "LanguagesScreen"))
            LocaleHelper.setLocale(this, initialLanguageCode)
            finish()
        }

        binding.ibNextButton.setOnClickListener {
            FirebaseAnalyticsUtils.logClickEvent(this, "click_next_button", mapOf("screen" to "LanguagesScreen"))
            val currentLanguageCode = LocaleHelper.getLanguage(this)
            if (currentLanguageCode != initialLanguageCode) {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finishAffinity()
            } else {
                finish()
            }
        }

        NativeLanguageHelper(
            context = this,
            adId = getNativeLanguageId(),
            showAdRemoteFlag = true,
            isProUser = preferences.isProUser,
            adContainer = binding.nativeAdContainer,
            onAdLoaded = { Log.d("AD", "Native ad shown") },
            onAdFailed = { Log.d("AD", "Ad failed to load") }
        )
    }

    override fun onPause() {
        super.onPause()
        FirebaseAnalyticsUtils.stopScreenTimer(this, "LanguagesScreen")
    }
}