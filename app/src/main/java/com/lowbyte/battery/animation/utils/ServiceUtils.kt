package com.lowbyte.battery.animation.utils

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GESTURE_SWIPE_UP
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_POWER_DIALOG
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import java.text.SimpleDateFormat
import java.util.Locale

object ServiceUtils {



     fun Boolean.toVisibility(): Int = if (this) View.VISIBLE else View.GONE

     fun View.setTint(color: Int) {
        (this as? ImageView)?.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

     fun View.setTextSizeInSp(sp: Int) {
        if (this is TextView) {
            val px = sp * resources.displayMetrics.scaledDensity
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, px)
        }
    }

     fun applyIconSize(context:Context , view: View?, sizeDp: Int) {
        val px = (sizeDp * context.resources.displayMetrics.density).toInt()
        view?.layoutParams = LinearLayout.LayoutParams(px, px)
    }

    fun performGlobalActionByName(context:AccessibilityService ,preferences :AppPreferences ,actionName: String) {
        if (preferences.isVibrateMode && preferences.isGestureMode) {
            val vibrator = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator
            Log.d("isVIbation", "isVIbation ${vibrator?.hasVibrator()}")

            vibrator?.let {
                if (it.hasVibrator()) {
                    Log.d("isVIbation", "isVIbation r ${vibrator?.hasVibrator()}")
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val timings = longArrayOf(0, 300, 50, 200) // delay, vibrate, pause, vibrate
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(1000)
                    }
                }

            }
        }


        when (actionName) {
            "action_do_nothing" -> {

            }

            "action_back_action" -> {
                context.performGlobalAction(GLOBAL_ACTION_BACK)
            }

            "action_home_action" -> {
                context.performGlobalAction(GLOBAL_ACTION_HOME)
            }

            "action_recent_action" -> {
                context.performGlobalAction(GLOBAL_ACTION_RECENTS)
            }

            "action_lock_screen" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    context.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.lock_screen_not_supported_on_your_device),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            "action_open_notifications" -> {
                context.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            }

            "action_power_options" -> {
                context.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            }

            "action_quick_scroll_to_up" -> {
                context.performGlobalAction(GESTURE_SWIPE_UP)
            }

            "action_open_control_centre" -> {
                context.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            }

            "action_take_screenshot" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    context.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.take_screenshot_not_supported_on_your_device),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


            else -> {
                // Unsupported
            }
        }
    }

     fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

     fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

     fun isMobileDataEnabled(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE && cm.activeNetworkInfo?.isConnected == true
    }

    // Hotspot detection is tricky. Here's an API-30+ example using ConnectivityManager
     fun isHotspotEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return try {
            val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            false
        }
    }

     fun updateTime():SimpleDateFormat {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat
    }

     fun Context.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    fun Context.isEditing(isEditing: Boolean){
        val intent = Intent(BROADCAST_ACTION).apply {
            putExtra("isEditing", isEditing)
        }
        sendBroadcast(intent)
    }

}