package com.lowbyte.battery.animation.broadcastReciver

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class BatteryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        super.onUpdate(context, manager, ids)
        // DO NOT registerReceiver here
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // DO NOT registerReceiver here
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Nothing to unregister since we aren't registering
    }
}