package com.lowbyte.battery.animation.broadcastReciver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.BatteryWidgetEditApplyActivity
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

        // If a specific widget ID was passed, only update that widget
        val specificWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val widgetsToUpdate = if (specificWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.d("ITEM_CLICK", "Updating widget $specificWidgetId")
            listOf(specificWidgetId)

        } else {
            Log.d("ITEM_CLICK", "Updating widget List")
            widgetIds.toList()
        }


        for (widgetId in widgetsToUpdate) {
            val widgetIconName =  preferences.getWidgetIcon(widgetId)

            val views = RemoteViews(context.packageName, R.layout.widget_battery)
            Log.d("ITEM_CLICK", "Updating widget $widgetId with icon: $widgetIconName")
            val resId = context.resources.getIdentifier(widgetIconName, "drawable", context.packageName)
            views.setImageViewResource(R.id.battery_icon, if (resId != 0) resId else R.drawable.emoji_1)
            val percent = "$batteryPct %"
            views.setViewVisibility(R.id.batteryLevelBottom, View.VISIBLE)
            views.setTextViewText(R.id.batteryLevelBottom, percent)
            views.setViewVisibility(R.id.batteryLevelTop, View.GONE)
            views.setViewVisibility(R.id.batteryLevelCenter, View.GONE)
            val intent = Intent(context, BatteryWidgetEditApplyActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId) // 🔥 REQUIRED!
            }
            intent.data = intent.toUri(Intent.URI_INTENT_SCHEME).toUri()
            val pendingIntent = PendingIntent.getActivity(
                context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.battery_icon, pendingIntent)
            manager.updateAppWidget(widgetId, views)
        }
    }
}
