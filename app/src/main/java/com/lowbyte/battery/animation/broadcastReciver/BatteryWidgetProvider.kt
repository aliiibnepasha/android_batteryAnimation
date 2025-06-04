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

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.e("BatteryWidgetProvider", "onUpdate called with ${appWidgetIds.size} widgets")
        
        // Send broadcast to update widgets
        val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
            action = ACTION_UPDATE_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(updateIntent)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        
        // Get the widget icon from options
        val widgetIcon = newOptions.getString("WIDGET_ICON")
        if (widgetIcon != null) {
            Log.e("BatteryWidgetProvider", "Saving widget icon: $widgetIcon for widget ID: $appWidgetId")
            val preferences = AppPreferences.getInstance(context)
            
            // Get the saved icon for the new widget
            val savedIcon = preferences.getWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID)
            if (savedIcon.isNotEmpty()) {
                // Save it with the actual widget ID
                preferences.saveWidgetIcon(appWidgetId, savedIcon)
                Log.e("BatteryWidgetProvider", "Transferred saved icon: $savedIcon to widget ID: $appWidgetId")
                
                // Clear the temporary saved icon
                preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, "")
                
                // Send an immediate update broadcast with the saved icon
                val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                    action = ACTION_UPDATE_WIDGET
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("WIDGET_ICON", savedIcon)
                }
                context.sendBroadcast(updateIntent)
                Log.e("BatteryWidgetProvider", "Sent immediate update broadcast for widget $appWidgetId with icon: $savedIcon")
            } else {
                // Use the icon from options
                preferences.saveWidgetIcon(appWidgetId, widgetIcon)
                
                // Send an immediate update broadcast with the icon from options
                val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                    action = ACTION_UPDATE_WIDGET
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("WIDGET_ICON", widgetIcon)
                }
                context.sendBroadcast(updateIntent)
                Log.e("BatteryWidgetProvider", "Sent immediate update broadcast for widget $appWidgetId with icon: $widgetIcon")
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val widgetIcon = intent.getStringExtra("WIDGET_ICON")
            
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && widgetIcon != null) {
                Log.e("BatteryWidgetProvider", "Received update for widget $widgetId with icon: $widgetIcon")
                val preferences = AppPreferences.getInstance(context)
                
                // Get the saved icon for the new widget
                val savedIcon = preferences.getWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID)
                if (savedIcon.isNotEmpty()) {
                    // Save it with the actual widget ID
                    preferences.saveWidgetIcon(widgetId, savedIcon)
                    Log.e("BatteryWidgetProvider", "Transferred saved icon: $savedIcon to widget ID: $widgetId")
                    
                    // Clear the temporary saved icon
                    preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, "")
                    
                    // Send an immediate update broadcast with the saved icon
                    val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                        action = ACTION_UPDATE_WIDGET
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        putExtra("WIDGET_ICON", savedIcon)
                    }
                    context.sendBroadcast(updateIntent)
                    Log.e("BatteryWidgetProvider", "Sent immediate update broadcast for widget $widgetId with icon: $savedIcon")
                } else {
                    // Use the icon from the intent
                    preferences.saveWidgetIcon(widgetId, widgetIcon)
                    
                    // Send an immediate update broadcast with the icon from intent
                    val updateIntent = Intent(context, BatteryLevelReceiver::class.java).apply {
                        action = ACTION_UPDATE_WIDGET
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        putExtra("WIDGET_ICON", widgetIcon)
                    }
                    context.sendBroadcast(updateIntent)
                    Log.e("BatteryWidgetProvider", "Sent immediate update broadcast for widget $widgetId with icon: $widgetIcon")
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        Log.e("BatteryWidgetProvider", "onEnabled called")
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        Log.e("BatteryWidgetProvider", "onDisabled called")
        super.onDisabled(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.e("BatteryWidgetProvider", "onDeleted called for widgets: ${appWidgetIds.joinToString()}")
        super.onDeleted(context, appWidgetIds)
    }
}