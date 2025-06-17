package com.lowbyte.battery.animation.broadcastReciver

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
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

        appWidgetIds.forEach { widgetId ->
            Log.e(TAG, "onUpdate called for widgetId: $widgetId")

            // Handle temp icon assignment
            val tempIcon = preferences.getWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID)
            if (tempIcon.isNotEmpty()) {
                preferences.saveWidgetIcon(widgetId, tempIcon)
                preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, "")
                Log.e(TAG, "Assigned temp icon: $tempIcon to widgetId: $widgetId")
            }

            val icon = preferences.getWidgetIcon(widgetId)
            sendUpdateBroadcast(context, widgetId, icon)
        }

        // Start periodic updates
        startRepeatingWidgetUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val preferences = AppPreferences.getInstance(context)
            val widgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = widgetManager.getAppWidgetIds(ComponentName(context, BatteryWidgetProvider::class.java))

            widgetIds.forEach { widgetId ->
                val icon = preferences.getWidgetIcon(widgetId)
                sendUpdateBroadcast(context, widgetId, icon)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val widgetIcon = newOptions.getString("WIDGET_ICON")
        if (!widgetIcon.isNullOrEmpty()) {
            AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
            sendUpdateBroadcast(context, appWidgetId, widgetIcon)
            Log.d(TAG, "onAppWidgetOptionsChanged: $appWidgetId -> $widgetIcon")
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val preferences = AppPreferences.getInstance(context)
        appWidgetIds.forEach {
            preferences.saveWidgetIcon(it, "")
            Log.d(TAG, "onDeleted widgetId: $it")
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startRepeatingWidgetUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        stopRepeatingWidgetUpdate(context)
    }

    private fun sendUpdateBroadcast(context: Context, widgetId: Int, icon: String) {
        val intent = Intent(context, BatteryLevelReceiver::class.java).apply {
            action = ACTION_UPDATE_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra("WIDGET_ICON", icon)
        }
        context.sendBroadcast(intent)
    }

    private fun startRepeatingWidgetUpdate(context: Context) {
        val intent = Intent(context, BatteryWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            5000L,
            pendingIntent
        )

        Log.d(TAG, "Started repeating updates every 5 seconds.")
    }

    private fun stopRepeatingWidgetUpdate(context: Context) {
        val intent = Intent(context, BatteryWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.d(TAG, "Stopped repeating updates.")
    }
}