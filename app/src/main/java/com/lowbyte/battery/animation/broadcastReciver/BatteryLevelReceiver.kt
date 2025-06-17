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
import com.lowbyte.battery.animation.activity.SplashActivity
import com.lowbyte.battery.animation.utils.AnimationUtils
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryLevelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BatteryLevelReceiver","onReceive")
        if (intent.action != BatteryWidgetProvider.ACTION_UPDATE_WIDGET) return

        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val preferences = AppPreferences.getInstance(context)
        val iconName = intent.getStringExtra("WIDGET_ICON") ?: preferences.getWidgetIcon(widgetId)
        Log.d("BatteryLevelReceiver","iconName $iconName")

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level * 100 / scale.toFloat()

        Log.d("BatteryLevelReceiver","level $level")
        Log.d("BatteryLevelReceiver","scale $scale")
        Log.d("BatteryLevelReceiver","batteryPct $batteryPct")


        val views = RemoteViews(context.packageName, R.layout.widget_battery).apply {
            setViewVisibility(R.id.batteryLevelBottom, View.VISIBLE)
            setTextViewText(R.id.batteryLevelBottom, "${batteryPct.toInt()}%")
            setViewVisibility(R.id.batteryLevelTop, View.GONE)
            setViewVisibility(R.id.batteryLevelCenter, View.GONE)

            val resId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
            if (resId != 0) {
                Log.d("BatteryLevelReceiver","onReceive $resId")
                setImageViewResource(R.id.battery_icon, resId)
            } else {
                Log.d("BatteryLevelReceiver","onReceive Default Image ")
                setImageViewResource(R.id.battery_icon, R.drawable.emoji_1)
            }

            val intentClick = Intent(context, SplashActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                putExtra(AnimationUtils.EXTRA_LABEL, iconName)
                putExtra(AnimationUtils.EXTRA_POSITION, -1)
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, widgetId, intentClick,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
            setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }

        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
    }
}