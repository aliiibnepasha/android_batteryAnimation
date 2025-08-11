package com.lowbyte.battery.animation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.activity.SplashActivity
import com.lowbyte.battery.animation.broadcastReciver.BatteryWidgetProvider
import com.lowbyte.battery.animation.utils.AnimationUtils
import com.lowbyte.battery.animation.utils.AppPreferences
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BatteryWidgetForegroundService : Service() {

    private lateinit var preferences: AppPreferences
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastUpdateTime = 0L
    private var lastBatteryPercentage = -1


    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                updateWidgets() // keep as-is; force not needed here
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences.getInstance(this)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundWithNotification()
//        }

        registerBatteryReceiver()
        updateWidgets()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundWithNotification()
        }
        preferences.serviceRunningFlag = true

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                preferences.serviceRunningFlag = false
                // Update widgets NOW to reveal the restart button on all widgets
                updateWidgets(force = true)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_START_SERVICE -> {
                updateWidgets(force = true) // optional, ensures UI sync immediately
            }
            else -> {
                updateWidgets()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        preferences.serviceRunningFlag = false
        unregisterReceiver(batteryReceiver)
        // Force a final UI refresh so every widget shows the Start button
        updateWidgets(force = true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerBatteryReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(batteryReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(batteryReceiver, filter)
        }
    }

    private fun startForegroundWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Battery Widget Updates",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, BatteryWidgetForegroundService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.battery_widget_service))
            .setContentText(getString(R.string.please_keep_service_running_for_widgets_update))
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .addAction(R.drawable.ic_close, getString(R.string.stop_service), stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateWidgets(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastUpdateTime < 2000) return
        lastUpdateTime = now

        serviceScope.launch {
            val appWidgetManager = AppWidgetManager.getInstance(this@BatteryWidgetForegroundService)
            val component = ComponentName(this@BatteryWidgetForegroundService, BatteryWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(component)
            val isServiceRunning = preferences.serviceRunningFlag

            // Only read battery if needed (skip if force and not needed for UI)
            val batteryIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED), RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }

            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val percentage = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else lastBatteryPercentage

            // Don’t exit on same percentage when we’re forcing a UI change
            if (!force && percentage == lastBatteryPercentage) return@launch
            if (percentage >= 0) lastBatteryPercentage = percentage

            val startIntent = Intent(this@BatteryWidgetForegroundService, BatteryWidgetForegroundService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            val startPendingIntent = PendingIntent.getService(
                this@BatteryWidgetForegroundService, 1, startIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            widgetIds.forEach { widgetId ->
                val iconName = preferences.getWidgetIcon(widgetId) ?: ""
                val resId = resources.getIdentifier(iconName, "drawable", packageName)

                val views = RemoteViews(packageName, R.layout.widget_battery).apply {
                    if (percentage >= 0) setTextViewText(R.id.batteryLevelBottom, "$percentage %")
                    setViewVisibility(R.id.batteryLevelBottom, View.VISIBLE)
                    setViewVisibility(R.id.batteryLevelTop, View.GONE)
                    setViewVisibility(R.id.batteryLevelCenter, View.GONE)
                    setImageViewResource(R.id.battery_icon, if (resId != 0) resId else R.drawable.emoji_1)

                    val intentClick = Intent(this@BatteryWidgetForegroundService, SplashActivity::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        putExtra(AnimationUtils.EXTRA_LABEL, iconName)
                        putExtra(AnimationUtils.EXTRA_POSITION, -1)
                    }
                    val clickPendingIntent = PendingIntent.getActivity(
                        this@BatteryWidgetForegroundService, widgetId, intentClick,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    setOnClickPendingIntent(R.id.widget_root, clickPendingIntent)

                    // This is the key visibility flip that must run even if % didn’t change
                    setViewVisibility(R.id.widget_start_button, if (isServiceRunning) View.GONE else View.VISIBLE)
                    setOnClickPendingIntent(R.id.widget_start_button, startPendingIntent)
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "battery_widget_channel"
        private const val NOTIFICATION_ID = 101
        const val ACTION_STOP_SERVICE = "com.lowbyte.battery.STOP_FOREGROUND_SERVICE"
        const val ACTION_START_SERVICE = "com.lowbyte.battery.START_FOREGROUND_SERVICE"
    }
}