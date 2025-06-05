package com.lowbyte.battery.animation.activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.lowbyte.battery.animation.BaseActivity
import com.lowbyte.battery.animation.adapter.LanguageAdapter
import com.lowbyte.battery.animation.databinding.ActivityLanguagesBinding
import com.lowbyte.battery.animation.model.Language
import com.lowbyte.battery.animation.utils.LocaleHelper

class LanguagesActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguagesBinding
    private lateinit var adapter: LanguageAdapter


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val languages = listOf(
            Language("English", "en"),
            Language("العربية", "ar"),           // Arabic ok
            Language("Español", "es-rES"),       // Spanish (Spain) ok
            Language("Français", "fr-rFR"),      // French (France) ok
            Language("हिंदी", "hi"),             // Hindi ok
            Language("Italiano", "it-rIT"),      // Italian ok
            Language("日本語", "ja"),             // Japanese ok
            Language("한국어", "ko"),             // Korean ok
            Language("Bahasa Melayu", "ms-rMY"), // Malay (Malaysia) ok
            Language("Filipino", "phi"),         // Filipino  ok
            Language("ไทย", "th"),               // Thai ok
            Language("Türkçe", "tr-rTR"),        // Turkish (Turkey) ok
            Language("Tiếng Việt", "vi"),         // Vietnamese ok
            Language("Português", "pt-rPT"),     // Portuguese (Portugal) ok
            Language("Bahasa Indonesia", "in")  // Indonesian ok
        )

        val currentLanguageCode = LocaleHelper.getLanguage(this)

        adapter = LanguageAdapter(languages, currentLanguageCode) { language ->
            LocaleHelper.setLocale(this, language.code)
            recreate()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.ibBackButton.setOnClickListener {
            finish()
        }

        binding.ibNextButton.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
           finishAffinity()
        }
    }


}