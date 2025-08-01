package com.lowbyte.battery.animation.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lowbyte.battery.animation.serviceUtils.LottieItemData


class AppPreferences private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "BatteryAnimationPrefs"
        
        // Boolean keys
        private const val KEY_FIRST_RUN = "first_run"
        const val KEY_SHOW_LOTTIE_TOP_VIEW = "show_lottie_top_view"
        private const val KEY_DYNAMIC_ENABLED = "isDynamicEnabled"
        private const val KEY_STATUS_ENABLED = "isStatusEnabled"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_IS_VIBRATE_MODE = "is_vibrate_mode"
        private const val KEY_IS_GESTURE_MODE = "is_gesture_mode"
        private const val KEY_IS_PRO_USER = "isProUser"
        private const val KEY_SERVICE_RUNNING = "service_running"
        private const val KEY_IS_SERVICE_RUNNING = "is_service_running"


        // String keys
        private const val KEY_SELECTED_THEME = "selected_theme"


        // In AppPreferences.kt (add to companion object)
        private const val KEY_NOTCH_HEIGHT = "notch_height"
        private const val KEY_NOTCH_WIDTH = "notch_width"
        private const val KEY_X_AXIS = "x_axis"
        private const val KEY_Y_AXIS = "y_axis"



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
        private const val KEY_WIDGET_ICON = "widget_icon"
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
    var serviceRunningFlag: Boolean
        get() = sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_SERVICE_RUNNING, value) }


    var widgetCount: Int
        get() = sharedPreferences.getInt("WIDGET_COUNT", 0)
        set(value) = sharedPreferences.edit { putInt("WIDGET_COUNT", value) }

    fun incrementWidgetCount() {
        if (widgetCount < 10) {
            widgetCount += 1
        }
    }

    fun decrementWidgetCount() {
        if (widgetCount > 0) {
            widgetCount -= 1
        }
    }


    // For ints
    var statusBarHeight: Int
        get() = sharedPreferences.getInt(KEY_STATUS_HEIGHT, 40)
        set(value) = sharedPreferences.edit { putInt(KEY_STATUS_HEIGHT, value) }

     var notchHeight: Int
         get() = sharedPreferences.getInt(KEY_NOTCH_HEIGHT, 30)
        set(value) = sharedPreferences.edit { putInt(KEY_NOTCH_HEIGHT, value) }

    val screenWidth = Resources.getSystem().displayMetrics.widthPixels

    var notchWidth: Int
        get() = sharedPreferences.getInt(KEY_NOTCH_WIDTH, screenWidth / 8)
        set(value) = sharedPreferences.edit { putInt(KEY_NOTCH_WIDTH, value) }

     var notchXAxis: Int
         get() = sharedPreferences.getInt(KEY_X_AXIS, 0)
        set(value) = sharedPreferences.edit { putInt(KEY_X_AXIS, value) }

     var notchYAxis: Int
         get() = sharedPreferences.getInt(KEY_Y_AXIS, 4)
        set(value) = sharedPreferences.edit { putInt(KEY_Y_AXIS, value) }




    var statusBarMarginLeft: Int
        get() = sharedPreferences.getInt(KEY_STATUS_MARGIN_LEFT, 10)
        set(value) = sharedPreferences.edit { putInt(KEY_STATUS_MARGIN_LEFT, value) }

    var statusBarMarginRight: Int
        get() = sharedPreferences.getInt(KEY_STATUS_MARGIN_RIGHT, 10)
        set(value) = sharedPreferences.edit { putInt(KEY_STATUS_MARGIN_RIGHT, value) }

    var statusBarBgColor: Int
        get() = sharedPreferences.getInt(KEY_STATUS_BG_COLOR, Color.WHITE)
        set(value) = sharedPreferences.edit { putInt(KEY_STATUS_BG_COLOR, value) }

    // For booleans (show/hide)
    var isVibrateMode: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_VIBRATE_MODE, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_IS_VIBRATE_MODE, value) }


    var isProUser: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_PRO_USER, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_IS_PRO_USER, value) }


      // For booleans (show/hide)
    var isGestureMode: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_GESTURE_MODE, true)
        set(value) = sharedPreferences.edit { putBoolean(KEY_IS_GESTURE_MODE, value) }

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
        get() = sharedPreferences.getString(KEY_STATUS_LOTTIE, "") ?: ""
        set(value) = sharedPreferences.edit { putString(KEY_STATUS_LOTTIE, value) }

    var customIconName: String
        get() = sharedPreferences.getString(KEY_STATUS_ICON, "widget_39") ?: "widget_39"
        set(value) = sharedPreferences.edit { putString(KEY_STATUS_ICON, value) }

      var batteryIconName: String
        get() = sharedPreferences.getString(KEY_BAT_ICON, "emoji_1") ?: "emoji_1"
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

 var isDynamicEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_DYNAMIC_ENABLED, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_DYNAMIC_ENABLED, value) }


    fun saveWidgetIcon(widgetId: Int, iconName: String) {
        Log.e("AppPreferences", "Saving widget icon: $iconName for widget ID: $widgetId")
        sharedPreferences.edit { 
            putString("icon_name_$widgetId", iconName)
            apply() // Ensure immediate write
        }
        // Verify the save
        val savedIcon = getWidgetIcon(widgetId)
        Log.e("AppPreferences", "Verified saved icon for widget $widgetId: $savedIcon")
    }

    fun getWidgetIcon(widgetId: Int): String {
        val iconName = sharedPreferences.getString("icon_name_$widgetId", "") ?: ""
        Log.e("AppPreferences", "Getting widget icon for widget ID: $widgetId -> $iconName")
        return iconName
    }

    fun getInt(key: String, default: Int = 0): Int = sharedPreferences.getInt(key, default)
    fun setInt(key: String, value: Int) = sharedPreferences.edit { putInt(key, value) }


    fun shouldTriggerEveryThirdTime(key: String ="AdsToBeShow"): Boolean {
        val count = getInt(key, 1) + 1
        setInt(key, count)
        return count % 2 == 0
    }


    fun getString(key: String, default: String = ""): String? = sharedPreferences.getString(key, default)

    fun setString(key: String, value: String) = sharedPreferences.edit { putString(key, value) }


     fun getBoolean(key: String, default: Boolean = false): Boolean? = sharedPreferences.getBoolean(key, default)

    fun setBoolean(key: String, value: Boolean) = sharedPreferences.edit { putBoolean(key, value) }





    // For widget style
    var widgetStyleIndex: Int
        get() = sharedPreferences.getInt("widget_style_index", 0)
        set(value) = sharedPreferences.edit { putInt("widget_style_index", value) }



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


    // --- Widget specific style handling (per widget ID) ---
    fun saveStyleForWidget(widgetId: Int, styleIndex: Int, iconName: String) {
        Log.d("STYLEINDEX"," Pref WidgetID: $widgetId -> styleIndex: $styleIndex, icon: $iconName")
        sharedPreferences.edit {
            putInt("style_index_$widgetId", styleIndex)
                .putString("icon_name_$widgetId", iconName)
        }
    }

    fun getStyleIndexForWidget(widgetId: Int): Int {
        return sharedPreferences.getInt("style_index_$widgetId", 0)
    }

    fun getIconNameForWidget(widgetId: Int): String {
        return sharedPreferences.getString("icon_name_$widgetId", "emoji_1") ?: "emoji_1"
    }

    var widgetIconName: String
        get() = sharedPreferences.getString(KEY_WIDGET_ICON, "widget_39") ?: "widget_39"
        set(value) = sharedPreferences.edit { putString(KEY_WIDGET_ICON, value) }

    var isDarkMode: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_IS_DARK_MODE, value) }

    fun putFloat(key: String, value: Float) {
        sharedPreferences.edit { putFloat(key, value) }
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    private val gson = Gson()

    fun putLottieItemList(key: String, list: List<LottieItemData>) {
        val json = gson.toJson(list)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun getLottieItemList(key: String): List<LottieItemData> {
        val json = sharedPreferences.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<LottieItemData>>() {}.type
        return gson.fromJson(json, type)
    }

}


