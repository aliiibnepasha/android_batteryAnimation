package com.lowbyte.battery.animation.broadcastReciver

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lowbyte.battery.animation.broadcastReciver.BatteryWidgetProvider
import com.lowbyte.battery.animation.utils.AppPreferences

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, BatteryWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        val preferences = AppPreferences.getInstance(context)
        widgetIds.forEach { widgetId ->
            val icon = preferences.getWidgetIcon(widgetId)
            val intent = android.content.Intent(context, BatteryWidgetProvider::class.java).apply {
                action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                putExtra("WIDGET_ICON", icon)
            }
            context.sendBroadcast(intent)
        }

        return Result.success()
    }
}