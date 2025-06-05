package com.lowbyte.battery.animation.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale
import androidx.core.content.edit

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val DEFAULT_LANGUAGE = "en"

    fun getLanguage(context: Context): String {
        return getPersistedData(context, DEFAULT_LANGUAGE)
    }

    private fun persist(context: Context, language: String) {
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .edit {
                putString(SELECTED_LANGUAGE, language)
                apply()
            }
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }


    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = if (language.contains("-")) {
            val parts = language.split("-")
            Locale(parts[0], parts[1])
        } else {
            Locale(language)
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

} 