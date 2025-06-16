// Updated: BatteryWidgetProvider.kt
package com.lowbyte.battery.animation.broadcastReciver

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryWidgetProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_UPDATE_WIDGET = "com.lowbyte.battery.animation.ACTION_UPDATE_WIDGET"
        private const val TAG = "BatteryWidgetProvider"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val preferences = AppPreferences.getInstance(context)

        for (appWidgetId in appWidgetIds) {
            val widgetIcon = preferences.getWidgetIcon(appWidgetId)
            sendUpdateBroadcast(context, appWidgetId, widgetIcon)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val widgetIcon = newOptions.getString("WIDGET_ICON")
        if (!widgetIcon.isNullOrEmpty()) {
            AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
            sendUpdateBroadcast(context, appWidgetId, widgetIcon)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val preferences = AppPreferences.getInstance(context)
        appWidgetIds.forEach { preferences.saveWidgetIcon(it, "") }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val widgetIcon = intent.getStringExtra("WIDGET_ICON") ?: ""
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
                sendUpdateBroadcast(context, appWidgetId, widgetIcon)
            }
        }
    }

    private fun sendUpdateBroadcast(context: Context, widgetId: Int, icon: String) {
        val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
            action = ACTION_UPDATE_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra("WIDGET_ICON", icon)
        }
        context.sendBroadcast(updateIntent)
    }
}
