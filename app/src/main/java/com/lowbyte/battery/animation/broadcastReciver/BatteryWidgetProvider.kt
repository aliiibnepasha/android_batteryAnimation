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
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.e("BatteryWidgetProvider", "onUpdate called with widget IDs: ${appWidgetIds.joinToString()}")
        
        // Send update broadcast for each widget
        for (appWidgetId in appWidgetIds) {
            val preferences = AppPreferences.getInstance(context)
            val widgetIcon = preferences.getWidgetIcon(appWidgetId)
            
            val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                if (widgetIcon.isNotEmpty()) {
                    putExtra("WIDGET_ICON", widgetIcon)
                }
            }
            context.sendBroadcast(updateIntent)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.e("BatteryWidgetProvider", "onAppWidgetOptionsChanged called for widget $appWidgetId")
        
        // Get the widget icon from options
        val widgetIcon = newOptions.getString("WIDGET_ICON")
        if (widgetIcon != null) {
            Log.e("BatteryWidgetProvider", "Received widget icon from options: $widgetIcon")
            
            // Save the widget icon
            val preferences = AppPreferences.getInstance(context)
            preferences.saveWidgetIcon(appWidgetId, widgetIcon)
            Log.e("BatteryWidgetProvider", "Saved widget icon for widget $appWidgetId: $widgetIcon")
            
            // Send update broadcast
            val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("WIDGET_ICON", widgetIcon)
            }
            context.sendBroadcast(updateIntent)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.e("BatteryWidgetProvider", "onEnabled called")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.e("BatteryWidgetProvider", "onDisabled called")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.e("BatteryWidgetProvider", "onDeleted called for widgets: ${appWidgetIds.joinToString()}")
        
        // Remove saved icons for deleted widgets
        val preferences = AppPreferences.getInstance(context)
        for (appWidgetId in appWidgetIds) {
            preferences.saveWidgetIcon(appWidgetId, "")
            Log.e("BatteryWidgetProvider", "Removed saved icon for widget $appWidgetId")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.e("BatteryWidgetProvider", "onReceive called with action: ${intent.action}")
        
        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val widgetIcon = intent.getStringExtra("WIDGET_ICON")
                
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    Log.e("BatteryWidgetProvider", "Updating widget $appWidgetId with icon: $widgetIcon")
                    
                    // Save the widget icon if provided
                    if (widgetIcon != null) {
                        val preferences = AppPreferences.getInstance(context)
                        preferences.saveWidgetIcon(appWidgetId, widgetIcon)
                        Log.e("BatteryWidgetProvider", "Saved widget icon for widget $appWidgetId: $widgetIcon")
                    }
                    
                    // Send update broadcast
                    val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                        action = ACTION_UPDATE_WIDGET
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        if (widgetIcon != null) {
                            putExtra("WIDGET_ICON", widgetIcon)
                        }
                    }
                    context.sendBroadcast(updateIntent)
                }
            }
        }
    }
}