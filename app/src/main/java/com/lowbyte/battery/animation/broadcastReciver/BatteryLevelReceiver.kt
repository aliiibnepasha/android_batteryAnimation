package com.lowbyte.battery.animation.broadcastReciver

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import android.widget.RemoteViews
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryLevelReceiver : BroadcastReceiver() {
    private lateinit var preferences: AppPreferences

    override fun onReceive(context: Context, intent: Intent) {
         preferences = AppPreferences.getInstance(context)
        val manager = AppWidgetManager.getInstance(context)
        val widgetIds = manager.getAppWidgetIds(ComponentName(context, BatteryWidgetProvider::class.java))

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = (level / scale.toFloat() * 100).toInt()

        for (widgetId in widgetIds) {
            val styleIndex = preferences.getStyleIndexForWidget(widgetId)
            val widgetIconName = preferences.getIconNameForWidget(widgetId)
            val views = RemoteViews(context.packageName, R.layout.widget_battery)

            Log.d("STYLEINDEX", "Rec WidgetID: $widgetId -> styleIndex: $styleIndex, icon: $widgetIconName")

            val resId = context.resources.getIdentifier(widgetIconName, "drawable", context.packageName)
            views.setImageViewResource(R.id.battery_icon, if (resId != 0) resId else R.drawable.emoji_1)

            when (styleIndex) {
                0 -> applyStyleClassic(context, views, batteryPct)
                1 -> applyStyleDark(context, views, batteryPct)
                2 -> applyStyleGreen(context, views, batteryPct)
                else -> applyStyleClassic(context, views, batteryPct)
            }

            manager.updateAppWidget(widgetId, views)
        }
    }

    private fun applyStyleClassic(context: Context, views: RemoteViews, batteryPct: Int) {
        views.setTextColor(R.id.batteryLevelTop, context.getColor(R.color.white))
        views.setTextColor(R.id.batteryLevelCenter, context.getColor(R.color.white))
        views.setTextColor(R.id.batteryLevelBottom, context.getColor(R.color.white))
        views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_1)
        setBatteryPercentage(views, batteryPct)
    }

    private fun applyStyleDark(context: Context, views: RemoteViews, batteryPct: Int) {
        views.setTextColor(R.id.batteryLevelTop, context.getColor(R.color.black))
        views.setTextColor(R.id.batteryLevelCenter, context.getColor(R.color.black))
        views.setTextColor(R.id.batteryLevelBottom, context.getColor(R.color.black))
        views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_5)
        setBatteryPercentage(views, batteryPct)
    }

    private fun applyStyleGreen(context: Context, views: RemoteViews, batteryPct: Int) {
        views.setTextColor(R.id.batteryLevelTop, context.getColor(R.color.black))
        views.setTextColor(R.id.batteryLevelCenter, context.getColor(R.color.black))
        views.setTextColor(R.id.batteryLevelBottom, context.getColor(R.color.black))
        views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_9)
        setBatteryPercentage(views, batteryPct)
    }

    private fun setBatteryPercentage(views: RemoteViews, batteryPct: Int) {
        val percent = "$batteryPct%"
        views.setTextViewText(R.id.batteryLevelTop, percent)
        views.setTextViewText(R.id.batteryLevelCenter, percent)
        views.setTextViewText(R.id.batteryLevelBottom, percent)
    }
}
