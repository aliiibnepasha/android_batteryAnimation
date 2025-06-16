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
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_LABEL
import com.lowbyte.battery.animation.utils.AnimationUtils.EXTRA_POSITION
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryLevelReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BatteryLevelReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.e(TAG, "onReceive called with action: ${intent.action}")
            
            if (intent.action != BatteryWidgetProvider.ACTION_UPDATE_WIDGET) {
                return
            }
            
            // Get battery information
            val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()
            
            Log.e(TAG, "Battery level: ${batteryPct.toInt()}%")
            
            // Get the specific widget ID from the intent
            val specificWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (specificWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.e(TAG, "No specific widget ID provided, skipping update")
                return
            }
            
            Log.e(TAG, "Processing update for widget ID: $specificWidgetId")
            
            // Initialize preferences
            val preferences = AppPreferences.getInstance(context)
            
            // Get the widget's specific icon from preferences
            var widgetIconName = preferences.getWidgetIcon(specificWidgetId)
            Log.e(TAG, "Widget $specificWidgetId current icon from preferences: $widgetIconName")
            
            // If no icon is found in preferences and we have an icon from the intent, use that
            val widgetIcon = intent.getStringExtra("WIDGET_ICON")
            if (widgetIcon != null) {
                Log.e(TAG, "Received widget icon from intent: $widgetIcon for widget $specificWidgetId")
                if (widgetIconName.isEmpty()) {
                    widgetIconName = widgetIcon
                    preferences.saveWidgetIcon(specificWidgetId, widgetIcon)
                    Log.e(TAG, "Saved and using new icon from intent: $widgetIcon for widget $specificWidgetId")
                } else {
                    Log.e(TAG, "Keeping existing icon $widgetIconName for widget $specificWidgetId")
                }
            }
            
            try {
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
                        Log.e(TAG, "Loading specific image for widget $specificWidgetId: $widgetIconName, resId: $resId")
                        views.setImageViewResource(R.id.battery_icon, resId)
                        Log.e(TAG, "Successfully set image for widget $specificWidgetId with resId: $resId")
                    } else {
                        Log.e(TAG, "Failed to find drawable resource for widget $specificWidgetId: $widgetIconName")
                        // Use default image if specific image not found
                        views.setImageViewResource(R.id.battery_icon, R.drawable.emoji_1)
                    }
                } else {
                    Log.e(TAG, "No icon name found for widget $specificWidgetId, using default")
                    // Use default image if no icon name is found
                    views.setImageViewResource(R.id.battery_icon, R.drawable.emoji_1)
                }
                
                // Create a unique PendingIntent for the widget
                val clickIntent = Intent(context, BatteryWidgetEditApplyActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, specificWidgetId)
                    putExtra(EXTRA_POSITION, -1)
                    putExtra(EXTRA_LABEL, widgetIconName)
                }
                val pendingIntent = android.app.PendingIntent.getActivity(
                    context,
                    specificWidgetId, // Use widget ID as request code to make it unique
                    clickIntent,
                    android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                
                // Update the specific widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                appWidgetManager.updateAppWidget(specificWidgetId, views)
                Log.e(TAG, "Successfully updated widget $specificWidgetId with icon $widgetIconName")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget $specificWidgetId", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onReceive", e)
        }
    }
}
