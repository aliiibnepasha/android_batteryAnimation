package com.lowbyte.battery.animation.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale
import androidx.core.content.edit

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun setLocale(context: Context, language: String) {
        persist(context, language)
        updateResources(context, language)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context, Locale.getDefault().language)
    }

    private fun persist(context: Context, language: String) {
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .edit {
                putString(SELECTED_LANGUAGE, language)
            }
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        return context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    private fun updateResources(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
} 