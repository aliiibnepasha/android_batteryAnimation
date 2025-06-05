package com.lowbyte.battery.animation.broadcastReciver

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.BatteryWidgetEditApplyActivity
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryLevelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.e("BatteryLevelReceiver", "onReceive called with action: ${intent.action}")
            
            if (intent.action != BatteryWidgetProvider.ACTION_UPDATE_WIDGET) {
                return
            }
            
            // Get battery information
            val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()
            
            Log.e("BatteryLevelReceiver", "Battery level: ${batteryPct.toInt()}%")
            
            // Get the widget icon from the intent
            val widgetIcon = intent.getStringExtra("WIDGET_ICON")
            if (widgetIcon != null) {
                Log.e("BatteryLevelReceiver", "Received widget icon from intent: $widgetIcon")
            }
            
            // Get the specific widget ID from the intent
            val specificWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            
            // Get all widget IDs
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetProvider = android.content.ComponentName(context, BatteryWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)
            
            // Initialize preferences
            val preferences = AppPreferences.getInstance(context)
            
            // Determine which widgets to update
            val widgetsToUpdate = if (specificWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // If a specific widget ID is provided, only update that widget
                intArrayOf(specificWidgetId)
            } else if (widgetIcon != null) {
                // If we have a widget icon but no specific widget ID, find the widget that needs updating
                val widgetToUpdate = allWidgetIds.find { id ->
                    preferences.getWidgetIcon(id).isEmpty()
                }
                if (widgetToUpdate != null) {
                    intArrayOf(widgetToUpdate)
                } else {
                    intArrayOf()
                }
            } else {
                // Otherwise update all widgets
                allWidgetIds
            }
            
            Log.e("BatteryLevelReceiver", "Found ${widgetsToUpdate.size} widgets to update")
            Log.e("BatteryLevelReceiver", "Updating all widgets: ${widgetsToUpdate.joinToString()}")
            
            // Update each widget
            for (widgetId in widgetsToUpdate) {
                try {
                    // Get the widget's specific icon from preferences
                    var widgetIconName = preferences.getWidgetIcon(widgetId)
                    Log.e("BatteryLevelReceiver", "Widget $widgetId icon name from preferences: $widgetIconName")
                    
                    // If no icon is found in preferences and we have an icon from the intent, use that
                    if (widgetIconName.isEmpty() && widgetIcon != null) {
                        widgetIconName = widgetIcon
                        preferences.saveWidgetIcon(widgetId, widgetIcon)
                        Log.e("BatteryLevelReceiver", "Saved and using icon from intent: $widgetIcon for widget $widgetId")
                    }
                    
                    // Create RemoteViews
                    val views = RemoteViews(context.packageName, R.layout.widget_battery)
                    
                    // Set the battery percentage
                    val percent = "${batteryPct.toInt()}%"
                    views.setViewVisibility(R.id.batteryLevelBottom, View.VISIBLE)
                    views.setTextViewText(R.id.batteryLevelBottom, percent)
                    views.setViewVisibility(R.id.batteryLevelTop, View.GONE)
                    views.setViewVisibility(R.id.batteryLevelCenter, View.GONE)
                    
                    // Load and set the widget's specific image
                    if (widgetIconName.isNotEmpty()) {
                        val resId = context.resources.getIdentifier(widgetIconName, "drawable", context.packageName)
                        if (resId != 0) {
                            Log.e("BatteryLevelReceiver", "Loading specific image for widget $widgetId: $widgetIconName, resId: $resId")
                            views.setImageViewResource(R.id.battery_icon, resId)
                            Log.e("BatteryLevelReceiver", "Successfully set image for widget $widgetId with resId: $resId")
                        } else {
                            Log.e("BatteryLevelReceiver", "Failed to find drawable resource for widget $widgetId: $widgetIconName")
                            // Use default image if specific image not found
                            views.setImageViewResource(R.id.battery_icon, R.drawable.emoji_1)
                        }
                    } else {
                        Log.e("BatteryLevelReceiver", "No icon name found for widget $widgetId, using default")
                        // Use default image if no icon name is found
                        views.setImageViewResource(R.id.battery_icon, R.drawable.emoji_1)
                    }
                    
                    // Create a unique PendingIntent for each widget
                    val clickIntent = Intent(context, BatteryWidgetEditApplyActivity::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        putExtra("EXTRA_POSITION", -1)
                        putExtra("EXTRA_LABEL", widgetIconName)
                    }
                    val pendingIntent = android.app.PendingIntent.getActivity(
                        context,
                        widgetId, // Use widget ID as request code to make it unique
                        clickIntent,
                        android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                    
                    // Update the widget
                    appWidgetManager.updateAppWidget(widgetId, views)
                    Log.e("BatteryLevelReceiver", "Successfully updated widget $widgetId")
                } catch (e: Exception) {
                    Log.e("BatteryLevelReceiver", "Error updating widget $widgetId", e)
                }
            }
        } catch (e: Exception) {
            Log.e("BatteryLevelReceiver", "Error in onReceive", e)
        }
    }
}
