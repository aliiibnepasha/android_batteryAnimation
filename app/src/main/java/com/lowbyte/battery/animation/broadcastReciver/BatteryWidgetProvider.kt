package com.lowbyte.battery.animation.broadcastReciver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.SplashActivity
import com.lowbyte.battery.animation.utils.AnimationUtils
import com.lowbyte.battery.animation.utils.AppPreferences

class BatteryWidgetProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_UPDATE_WIDGET = "com.lowbyte.battery.animation.ACTION_UPDATE_WIDGET"
        private const val TAG = "BatteryWidgetProvider"

    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val preferences = AppPreferences.getInstance(context)

        appWidgetIds.forEach { widgetId ->
            if (preferences.widgetCount >= 10) {
                Log.e(TAG, "Widget limit reached (10). Widget $widgetId not updated.")
                return@forEach
            }

            Log.e(TAG, "onUpdate called for widgetId: $widgetId")

            val tempIcon = preferences.getWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID)
            if (tempIcon.isNotEmpty()) {
                preferences.saveWidgetIcon(widgetId, tempIcon)
                preferences.saveWidgetIcon(AppWidgetManager.INVALID_APPWIDGET_ID, "")
            }

            val icon = preferences.getString("tempImageForWidget").toString()
            preferences.saveWidgetIcon(widgetId, icon)

            // ✅ Increment count if new widget
            if (preferences.getWidgetIcon(widgetId).isEmpty().not()) {
                preferences.incrementWidgetCount()
            }

            sendUpdateBroadcast(context, widgetId, icon)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val widgetIcon = intent.getStringExtra("WIDGET_ICON") ?: ""
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.d(TAG, "onReceive: Update requested for $appWidgetId with icon $widgetIcon")
                AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
                sendUpdateBroadcast(context, appWidgetId, widgetIcon)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val widgetIcon = newOptions.getString("WIDGET_ICON")
        if (!widgetIcon.isNullOrEmpty()) {
            AppPreferences.getInstance(context).saveWidgetIcon(appWidgetId, widgetIcon)
            Log.d(TAG, "onAppWidgetOptionsChanged for $appWidgetId with icon $widgetIcon")
            sendUpdateBroadcast(context, appWidgetId, widgetIcon)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val preferences = AppPreferences.getInstance(context)
        appWidgetIds.forEach {
            Log.d(TAG, "onDeleted widgetId: $it")
            preferences.saveWidgetIcon(it, "")
            preferences.decrementWidgetCount()
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Handler(Looper.getMainLooper()).post {
            startRepeatingWidgetUpdate(context)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        stopRepeatingWidgetUpdate()
    }

    private fun sendUpdateBroadcast(context: Context, widgetId: Int, icon: String) {

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetProvider = ComponentName(context, BatteryWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)
        val preferences = AppPreferences.getInstance(context)

        try {
            widgetIds.forEach { widgetId ->
                val iconName = preferences.getWidgetIcon(widgetId)
                Log.d("BatteryLevelReceiver", "Updating widgetId: $widgetId with icon: $iconName")

                val batteryIntent =
                    context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val batteryPct = level * 100 / scale.toFloat()

                val views = RemoteViews(context.packageName, R.layout.widget_battery).apply {
                    setViewVisibility(R.id.batteryLevelBottom, View.VISIBLE)
                    setTextViewText(R.id.batteryLevelBottom, "${batteryPct.toInt()} %")
                    setViewVisibility(R.id.batteryLevelTop, View.GONE)
                    setViewVisibility(R.id.batteryLevelCenter, View.GONE)

                    val resId =
                        context.resources.getIdentifier(iconName, "drawable", context.packageName)
                    if (resId != 0) setImageViewResource(R.id.battery_icon, resId)
                    else setImageViewResource(R.id.battery_icon, R.drawable.emoji_battery_preview_1)

                    val intentClick = Intent(context, SplashActivity::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        putExtra(AnimationUtils.EXTRA_LABEL, iconName)
                        putExtra(AnimationUtils.EXTRA_POSITION, -1)
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        widgetId,
                        intentClick,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }



    }

    private fun startRepeatingWidgetUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, BatteryWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
        val preferences = AppPreferences.getInstance(context)
        try {
            widgetIds.forEach { widgetId ->
                val icon = preferences.getWidgetIcon(widgetId)
                sendUpdateBroadcast(context, widgetId, icon)
            }
            Log.d(TAG, "Started repeating updates every 5 seconds.")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stopRepeatingWidgetUpdate() {

        Log.d(TAG, "Stopped repeating updates.")
    }
}