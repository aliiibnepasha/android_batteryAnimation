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
        Log.e(TAG, "onUpdate called with widget IDs: ${appWidgetIds.joinToString()}")
        
        // Send update broadcast for each widget
        for (appWidgetId in appWidgetIds) {
            val preferences = AppPreferences.getInstance(context)
            val widgetIcon = preferences.getWidgetIcon(appWidgetId)
            Log.e(TAG, "Widget $appWidgetId current icon from preferences: $widgetIcon")
            
            // If this is a new widget (no icon set), try to get the icon from the pending widget
            if (widgetIcon.isEmpty()) {
                val pendingIcon = preferences.getWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID)
                if (pendingIcon.isNotEmpty()) {
                    Log.e(TAG, "Found pending icon $pendingIcon for new widget $appWidgetId")
                    preferences.saveWidgetIcon(appWidgetId, pendingIcon)
                    preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, "")
                    
                    // Send update broadcast with the new icon
                    val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                        action = ACTION_UPDATE_WIDGET
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        putExtra("WIDGET_ICON", pendingIcon)
                    }
                    context.sendBroadcast(updateIntent)
                    Log.e(TAG, "Sent update broadcast for widget $appWidgetId with icon $pendingIcon")
                }
            } else {
                // Send update broadcast with existing icon
                val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                    action = ACTION_UPDATE_WIDGET
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("WIDGET_ICON", widgetIcon)
                }
                context.sendBroadcast(updateIntent)
                Log.e(TAG, "Sent update broadcast for widget $appWidgetId with existing icon $widgetIcon")
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.e(TAG, "onAppWidgetOptionsChanged called for widget $appWidgetId")
        
        // Get the widget icon from options
        val widgetIcon = newOptions.getString("WIDGET_ICON")
        if (widgetIcon != null) {
            Log.e(TAG, "Received widget icon from options: $widgetIcon for widget $appWidgetId")
            
            // Save the widget icon
            val preferences = AppPreferences.getInstance(context)
            preferences.saveWidgetIcon(appWidgetId, widgetIcon)
            Log.e(TAG, "Saved widget icon for widget $appWidgetId: $widgetIcon")
            
            // Send update broadcast only for this specific widget
            val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra("WIDGET_ICON", widgetIcon)
            }
            context.sendBroadcast(updateIntent)
            Log.e(TAG, "Sent update broadcast for widget $appWidgetId with icon $widgetIcon")
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.e(TAG, "onEnabled called")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.e(TAG, "onDisabled called")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.e(TAG, "onDeleted called for widgets: ${appWidgetIds.joinToString()}")
        
        // Remove saved icons for deleted widgets
        val preferences = AppPreferences.getInstance(context)
        for (appWidgetId in appWidgetIds) {
            preferences.saveWidgetIcon(appWidgetId, "")
            Log.e(TAG, "Removed saved icon for widget $appWidgetId")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.e(TAG, "onReceive called with action: ${intent.action}")
        
        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val widgetIcon = intent.getStringExtra("WIDGET_ICON")
                
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    Log.e(TAG, "Updating widget $appWidgetId with icon: $widgetIcon")
                    
                    // Save the widget icon if provided
                    if (widgetIcon != null) {
                        val preferences = AppPreferences.getInstance(context)
                        preferences.saveWidgetIcon(appWidgetId, widgetIcon)
                        Log.e(TAG, "Saved widget icon for widget $appWidgetId: $widgetIcon")
                    }
                    
                    // Send update broadcast only for this specific widget
                    val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                        action = ACTION_UPDATE_WIDGET
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        if (widgetIcon != null) {
                            putExtra("WIDGET_ICON", widgetIcon)
                            Log.e(TAG, "Sending update with icon $widgetIcon for widget $appWidgetId")
                        }
                    }
                    context.sendBroadcast(updateIntent)
                }
            }
        }
    }
}