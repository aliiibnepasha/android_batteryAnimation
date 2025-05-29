package com.lowbyte.battery.animation.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.core.content.edit


class AppPreferences private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "BatteryAnimationPrefs"
        
        // Boolean keys
        private const val KEY_FIRST_RUN = "first_run"
        private const val KEY_STATUS_ENABLED = "isStatusEnabled"
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


        // In AppPreferences.kt (add to companion object)
        private const val KEY_STATUS_HEIGHT = "status_height"
        private const val KEY_STATUS_MARGIN_LEFT = "status_margin_left"
        private const val KEY_STATUS_MARGIN_RIGHT = "status_margin_right"
        private const val KEY_STATUS_BG_COLOR = "status_bg_color"

        // Icon show/hide
        private const val KEY_SHOW_WIFI = "show_wifi"
        private const val KEY_SHOW_BAT_ICON = "show_battery_icon"
        private const val KEY_SHOW_HOTSPOT = "show_hotspot"
        private const val KEY_SHOW_DATA = "show_data"
        private const val KEY_SHOW_SIGNAL = "show_signal"
        private const val KEY_SHOW_AIRPLANE = "show_airplane"
        private const val KEY_SHOW_TIME = "show_time"
        private const val KEY_SHOW_DATE = "show_date"
        private const val KEY_SHOW_PERCENTAGE = "show_percentage"

        // Lottie and icon resource names
        private const val KEY_STATUS_LOTTIE = "status_lottie"
        private const val KEY_STATUS_ICON = "status_icon"
        private const val KEY_BAT_ICON = "batteryIconName"

        // Each icon size, e.g. icon_size_wifi, icon_size_hotspot, etc.
        fun iconSizeKeyFor(label: String) = "icon_size_${label.trim().replace("\\s+".toRegex(), "_").lowercase()}"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppPreferences(context.applicationContext).also { instance = it }
            }
        }
    }


    // For ints
    var statusBarHeight: Int
        get() = sharedPreferences.getInt(KEY_STATUS_HEIGHT, 24)
        set(value) = sharedPreferences.edit { putInt(KEY_STATUS_HEIGHT, value) }

    var statusBarMarginLeft: Int
        get() = sharedPreferences.getInt(KEY_STATUS_MARGIN_LEFT, 0)
        set(value) = sharedPreferences.edit { putInt(KEY_STATUS_MARGIN_LEFT, value) }

    var statusBarMarginRight: Int
        get() = sharedPreferences.getInt(KEY_STATUS_MARGIN_RIGHT, 0)
        set(value) = sharedPreferences.edit { putInt(KEY_STATUS_MARGIN_RIGHT, value) }

    var statusBarBgColor: Int
        get() = sharedPreferences.getInt(KEY_STATUS_BG_COLOR, Color.WHITE)
        set(value) = sharedPreferences.edit { putInt(KEY_STATUS_BG_COLOR, value) }

    // For booleans (show/hide)
    var showWifi: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_WIFI, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_WIFI, value) }

 var showBatteryIcon: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_BAT_ICON, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_BAT_ICON, value) }

    // Repeat for other icons...
    var showHotspot: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_HOTSPOT, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_HOTSPOT, value) }
    var showData: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_DATA, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_DATA, value) }
    var showSignal: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_SIGNAL, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_SIGNAL, value) }
    var showAirplane: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_AIRPLANE, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_AIRPLANE, value) }
    var showTime: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_TIME, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_TIME, value) }
    var showDate: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_DATE, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_DATE, value) }

    var showBatteryPercent: Boolean
        get() = sharedPreferences.getBoolean(KEY_SHOW_PERCENTAGE, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SHOW_PERCENTAGE, value) }

    // For icon/lottie resource names
    var statusLottieName: String
        get() = sharedPreferences.getString(KEY_STATUS_LOTTIE, "anim_8") ?: "anim_8"
        set(value) = sharedPreferences.edit { putString(KEY_STATUS_LOTTIE, value) }

    var statusIconName: String
        get() = sharedPreferences.getString(KEY_STATUS_ICON, "widget_fantasy_1") ?: "widget_fantasy_1"
        set(value) = sharedPreferences.edit { putString(KEY_STATUS_ICON, value) }

      var batteryIconName: String
        get() = sharedPreferences.getString(KEY_BAT_ICON, "widget_fantasy_1") ?: "widget_fantasy_1"
        set(value) = sharedPreferences.edit { putString(KEY_BAT_ICON, value) }




    // For per-icon size (dynamically via key)
    fun getIconSize(label: String, default: Int = 24) = getInt(iconSizeKeyFor(label), default)
    fun setIconSize(label: String, size: Int) = setInt(iconSizeKeyFor(label), size)




    // Boolean preferences
    var isFirstRun: Boolean
        get() = sharedPreferences.getBoolean(KEY_FIRST_RUN, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_FIRST_RUN, value) }

     var isStatusBarEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_STATUS_ENABLED, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_STATUS_ENABLED, value) }


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



    fun getInt(key: String, default: Int = 0): Int = sharedPreferences.getInt(key, default)
    fun setInt(key: String, value: Int) = sharedPreferences.edit { putInt(key, value) }

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

}

