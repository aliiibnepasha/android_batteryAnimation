package com.lowbyte.battery.animation.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppPreferences private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "BatteryAnimationPrefs"
        
        // Boolean keys
        private const val KEY_FIRST_RUN = "first_run"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        
        // Int keys
        private const val KEY_SELECTED_TAB = "selected_tab"
        private const val KEY_ANIMATION_SPEED = "animation_speed"
        
        // Long keys
        private const val KEY_LAST_UPDATE = "last_update"
        private const val KEY_USER_ID = "user_id"
        
        // String keys
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_SELECTED_THEME = "selected_theme"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    // Boolean preferences
    var isFirstRun: Boolean
        get() = sharedPreferences.getBoolean(KEY_FIRST_RUN, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_FIRST_RUN, value) }

    var isDarkMode: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_IS_DARK_MODE, value) }

    // Int preferences
    var selectedTab: Int
        get() = sharedPreferences.getInt(KEY_SELECTED_TAB, 0)
        set(value) = sharedPreferences.edit { putInt(KEY_SELECTED_TAB, value) }

    var animationSpeed: Int
        get() = sharedPreferences.getInt(KEY_ANIMATION_SPEED, 1)
        set(value) = sharedPreferences.edit { putInt(KEY_ANIMATION_SPEED, value) }

    // Long preferences
    var lastUpdate: Long
        get() = sharedPreferences.getLong(KEY_LAST_UPDATE, 0L)
        set(value) = sharedPreferences.edit { putLong(KEY_LAST_UPDATE, value) }

    var userId: Long
        get() = sharedPreferences.getLong(KEY_USER_ID, 0L)
        set(value) = sharedPreferences.edit { putLong(KEY_USER_ID, value) }

    // String preferences
    var userName: String
        get() = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
        set(value) = sharedPreferences.edit { putString(KEY_USER_NAME, value) }

    var selectedTheme: String
        get() = sharedPreferences.getString(KEY_SELECTED_THEME, "default") ?: "default"
        set(value) = sharedPreferences.edit { putString(KEY_SELECTED_THEME, value) }

    // Helper methods
    fun clearAll() {
        sharedPreferences.edit { clear() }
    }

    fun removeKey(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    // Generic methods for any type
    fun <T> getValue(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T
            is Int -> sharedPreferences.getInt(key, defaultValue) as T
            is Long -> sharedPreferences.getLong(key, defaultValue) as T
            is String -> sharedPreferences.getString(key, defaultValue) as T
            is Float -> sharedPreferences.getFloat(key, defaultValue) as T
            else -> throw IllegalArgumentException("Type not supported")
        }
    }

    fun <T> setValue(key: String, value: T) {
        sharedPreferences.edit {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Float -> putFloat(key, value)
                else -> throw IllegalArgumentException("Type not supported")
            }
        }
    }
}
