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
import com.lowbyte.battery.animation.broadcastReciver.BatteryWidgetProvider
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryLevelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.e("BatteryLevelReceiver", "onReceive called with action: ${intent.action}")
            
            // Get battery information
            val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()
            
            Log.e("BatteryLevelReceiver", "Battery level: ${batteryPct.toInt()}%")
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val preferences = AppPreferences.getInstance(context)
            
            // Get widget icon from intent if available
            val intentWidgetIcon = intent.getStringExtra("WIDGET_ICON")
            if (intentWidgetIcon != null) {
                Log.e("BatteryLevelReceiver", "Received widget icon from intent: $intentWidgetIcon")
            }
            
            // Determine which widgets to update
            val widgetIds = if (intent.action == BatteryWidgetProvider.ACTION_UPDATE_WIDGET) {
                val specificWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (specificWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    intArrayOf(specificWidgetId)
                } else {
                    intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: appWidgetManager.getAppWidgetIds(
                        android.content.ComponentName(context, BatteryWidgetProvider::class.java)
                    )
                }
            } else {
                appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, BatteryWidgetProvider::class.java)
                )
            }
            
            Log.e("BatteryLevelReceiver", "Found ${widgetIds.size} widgets to update")
            Log.e("BatteryLevelReceiver", "Updating all widgets: ${widgetIds.joinToString()}")
            
            // Update each widget
            widgetIds.forEach { widgetId ->
                try {
                    // Get the widget's specific icon name from preferences
                    val widgetIconName = preferences.getWidgetIcon(widgetId)
                    Log.e("BatteryLevelReceiver", "Widget $widgetId icon name from preferences: $widgetIconName")
                    
                    // If no icon in preferences but we have one from intent, use that
                    val finalIconName = if (widgetIconName.isEmpty() && intentWidgetIcon != null) {
                        Log.e("BatteryLevelReceiver", "Using icon from intent: $intentWidgetIcon")
                        preferences.saveWidgetIcon(widgetId, intentWidgetIcon)
                        intentWidgetIcon
                    } else {
                        widgetIconName
                    }
                    
                    // Create RemoteViews for the widget
                    val views = RemoteViews(context.packageName, R.layout.widget_battery)
                    
                    // Try to load the specific widget image
                    var resId = 0
                    if (finalIconName.isNotEmpty()) {
                        resId = context.resources.getIdentifier(finalIconName, "drawable", context.packageName)
                        Log.e("BatteryLevelReceiver", "Loading specific image for widget $widgetId: $finalIconName, resId: $resId")
                        if (resId == 0) {
                            Log.e("BatteryLevelReceiver", "Failed to find drawable resource for icon: $finalIconName")
                        }
                    }
                    
                    // If specific image not found, use default
                    if (resId == 0) {
                        Log.e("BatteryLevelReceiver", "No icon name found for widget $widgetId, using default")
                        resId = R.drawable.emoji_1
                    }
                    
                    // Set the image
                    views.setImageViewResource(R.id.battery_icon, resId)
                    Log.e("BatteryLevelReceiver", "Successfully set image for widget $widgetId with resId: $resId")
                    
                    // Set battery percentage
                    val percent = "${batteryPct.toInt()}%"
                    views.setViewVisibility(R.id.batteryLevelBottom, View.VISIBLE)
                    views.setTextViewText(R.id.batteryLevelBottom, percent)
                    views.setViewVisibility(R.id.batteryLevelTop, View.GONE)
                    views.setViewVisibility(R.id.batteryLevelCenter, View.GONE)
                    
                    // Create click intent for widget configuration
                    val configIntent = Intent(context, BatteryWidgetEditApplyActivity::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        putExtra("EXTRA_LABEL", finalIconName)
                    }
                    val pendingIntent = android.app.PendingIntent.getActivity(
                        context,
                        widgetId, // Use widget ID as request code to make each PendingIntent unique
                        configIntent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
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
