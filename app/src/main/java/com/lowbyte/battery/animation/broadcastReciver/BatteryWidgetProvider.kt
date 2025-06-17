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

        appWidgetIds.forEach { widgetId ->
            Log.e("BatteryWidgetProvider", "onUpdate called for widgetId: $widgetId")

            // Check if any icon was saved temporarily under INVALID_APPWIDGET_ID
            val tempIcon = preferences.getWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID)
            if (tempIcon.isNotEmpty()) {
                Log.e("BatteryWidgetProvider", "from Receiver --------------Assigning temp icon: $tempIcon to widgetId: $widgetId")
                preferences.saveWidgetIcon(widgetId, tempIcon)
                Log.e("BatteryWidgetProvider", "from Receiver ---/--$widgetId / -${preferences.getWidgetIcon(widgetId)}")

                preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, "") // clear
            }

            // Send update broadcast to draw the widget
            val intent = Intent(context, BatteryLevelReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                putExtra("WIDGET_ICON", preferences.getString("tempImageForWidget"))
            }
            //                    preferences.setString("tempImageForWidget",label)
            Log.e("BatteryWidgetProvider", "from Receiver ---/--$widgetId / -${preferences.getWidgetIcon(widgetId)}")

            context.sendBroadcast(intent)
        }

    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val widgetIcon = newOptions.getString("WIDGET_ICON")
        if (!widgetIcon.isNullOrEmpty()) {
            AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
            Log.d(TAG,"onAppWidgetOptionsChanged 3 $widgetIcon")
            sendUpdateBroadcast(context, appWidgetId, widgetIcon)
        }else{
            Log.d(TAG,"onAppWidgetOptionsChanged $widgetIcon")

        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val preferences = AppPreferences.getInstance(context)
        appWidgetIds.forEach {
            Log.d(TAG,"onDeleted $it")
            preferences.saveWidgetIcon(it, "")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val widgetIcon = intent.getStringExtra("WIDGET_ICON") ?: ""
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

                Log.d(TAG,"onReceive $appWidgetId  / $widgetIcon ")

                AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
                sendUpdateBroadcast(context, appWidgetId, widgetIcon)
            }
        }
    }

    private fun sendUpdateBroadcast(context: Context, widgetId: Int, icon: String) {
        val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
            action = ACTION_UPDATE_WIDGET

            Log.d(TAG,"sendUpdateBroadcast $widgetId / $icon ")

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra("WIDGET_ICON", icon)
        }
        context.sendBroadcast(updateIntent)
    }
}
