package com.lowbyte.battery.animation.broadcastReciver

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

            val tempIcon = preferences.getWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID)
            if (tempIcon.isNotEmpty()) {
                Log.e(TAG, "Assigning temp icon: $tempIcon to widgetId: $widgetId")
                preferences.saveWidgetIcon(widgetId, tempIcon)
                preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, "")
            }

            val icon = preferences.getString("tempImageForWidget").toString()
            preferences.saveWidgetIcon(widgetId, icon)
            Log.e(TAG, "Final icon assigned to $widgetId = $icon")

            sendUpdateBroadcast(context, widgetId, icon)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val widgetIcon = intent.getStringExtra("WIDGET_ICON") ?: ""
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.d(TAG, "onReceive: Update requested for $appWidgetId with icon $widgetIcon")
                AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
                sendUpdateBroadcast(context, appWidgetId, widgetIcon)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val widgetIcon = newOptions.getString("WIDGET_ICON")
        if (!widgetIcon.isNullOrEmpty()) {
            AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
            Log.d(TAG, "onAppWidgetOptionsChanged for $appWidgetId with icon $widgetIcon")
            sendUpdateBroadcast(context, appWidgetId, widgetIcon)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val preferences = AppPreferences.getInstance(context)
        appWidgetIds.forEach {
            Log.d(TAG, "onDeleted widgetId: $it")
            preferences.saveWidgetIcon(it, "")
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startRepeatingWidgetUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        stopRepeatingWidgetUpdate()
    }

    private fun sendUpdateBroadcast(context: Context, widgetId: Int, icon: String) {
        val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
            action = ACTION_UPDATE_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra("WIDGET_ICON", icon)
        }
        context.sendBroadcast(updateIntent)
    }

    private fun startRepeatingWidgetUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, BatteryWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        val preferences = AppPreferences.getInstance(context)
        widgetIds.forEach { widgetId ->
            val icon = preferences.getWidgetIcon(widgetId)
            sendUpdateBroadcast(context, widgetId, icon)
        }
        Log.d(TAG, "Started repeating updates every 5 seconds.")
    }

    private fun stopRepeatingWidgetUpdate() {

        Log.d(TAG, "Stopped repeating updates.")
    }
}