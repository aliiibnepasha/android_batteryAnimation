package com.lowbyte.battery.animation

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.widget.RemoteViews
import com.lowbyte.battery.animation.adapter.BatteryWidgetProvider
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryLevelReceiver : BroadcastReceiver() {
    private lateinit var preferences: AppPreferences

    override fun onReceive(context: Context, intent: Intent) {
        preferences = AppPreferences.getInstance(context)

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = (level / scale.toFloat() * 100).toInt()

        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, BatteryWidgetProvider::class.java)
        val views = RemoteViews(context.packageName, R.layout.widget_battery)
        preferences
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val showEmoji = prefs.getBoolean("useEmoji", false)
        val showLottie = prefs.getBoolean("showLottie", false)

        when {
            showEmoji -> views.setImageViewResource(R.id.battery_icon, R.drawable.emoji_1)
            showLottie -> views.setImageViewResource(R.id.battery_icon, R.drawable.widget_9)
            else -> views.setImageViewResource(R.id.battery_icon, R.drawable.emoji_15)
        }

        views.setTextViewText(R.id.batteryLevelTop, "$batteryPct%")
        views.setTextViewText(R.id.batteryLevelBottom, "$batteryPct%")
        views.setTextViewText(R.id.batteryLevelCenter, "$batteryPct%")
        manager.updateAppWidget(component, views)
    }
}