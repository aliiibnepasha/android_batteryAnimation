package com.lowbyte.battery.animation.broadcastReciver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.SplashActivity
import com.lowbyte.battery.animation.utils.AnimationUtils
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryLevelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetProvider = ComponentName(context, BatteryWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)
        val preferences = AppPreferences.getInstance(context)

        val isBatteryChanged = intent.action == Intent.ACTION_BATTERY_CHANGED
        val isWidgetUpdate = intent.action == BatteryWidgetProvider.ACTION_UPDATE_WIDGET

        if (!isBatteryChanged && !isWidgetUpdate) return

        widgetIds.forEach { widgetId ->
            val iconName = preferences.getWidgetIcon(widgetId)
            Log.d("BatteryLevelReceiver", "Updating widgetId: $widgetId with icon: $iconName")

            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()

            val views = RemoteViews(context.packageName, R.layout.widget_battery).apply {
                setViewVisibility(R.id.batteryLevelBottom, View.VISIBLE)
                setTextViewText(R.id.batteryLevelBottom, "${batteryPct.toInt()}%")
                setViewVisibility(R.id.batteryLevelTop, View.GONE)
                setViewVisibility(R.id.batteryLevelCenter, View.GONE)

                val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                if (resId != 0) setImageViewResource(R.id.battery_icon, resId)
                else setImageViewResource(R.id.battery_icon, R.drawable.emoji_1)

                val intentClick = Intent(context, SplashActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    putExtra(AnimationUtils.EXTRA_LABEL, iconName)
                    putExtra(AnimationUtils.EXTRA_POSITION, -1)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context, widgetId, intentClick,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }


        widgetIds.forEach { widgetId ->
            val icon = preferences.getWidgetIcon(widgetId)
            val intent = Intent(context, BatteryWidgetProvider::class.java).apply {
                action = BatteryWidgetProvider.ACTION_UPDATE_WIDGET
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                putExtra("WIDGET_ICON", icon)
            }
            context.sendBroadcast(intent)
        }
    }}