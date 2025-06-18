package com.lowbyte.battery.animation.service

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.*
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
import kotlinx.coroutines.delay

class BatteryWidgetForegroundService : Service() {

    private lateinit var preferences: AppPreferences

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_BATTERY_CHANGED /*|| action == Intent.ACTION_POWER_CONNECTED || action == Intent.ACTION_POWER_DISCONNECTED*/) {
                Log.d("widgetTracking", "iconName - $action / widgetId - Receiver")
                updateWidgets()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences.getInstance(this)
        preferences.serviceRunningFlag = true
        registerBatteryReceiver()
        startForegroundWithNotification()
        updateWidgets()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> stopSelf()
            ACTION_START_SERVICE -> updateWidgets()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.serviceRunningFlag = false
        unregisterReceiver(batteryReceiver)
        updateWidgets()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerBatteryReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)
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
            .setContentTitle("Battery Widget Service")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .addAction(R.drawable.ic_close, "Stop Service", stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateWidgets() {
        Thread {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val component = ComponentName(this, BatteryWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(component)
            val isServiceRunning = preferences.serviceRunningFlag

            val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val percentage = (level * 100 / scale.toFloat()).toInt()

            widgetIds.forEach { widgetId ->
                val iconName = preferences.getWidgetIcon(widgetId)
                val resId = resources.getIdentifier(iconName, "drawable", packageName)
                Log.e("widgetTracking", "loading bitmap for $iconName / $widgetId")

                val bitmap = try {
                    if (resId != 0) Picasso.get().load(resId).get()
                    else Picasso.get().load(R.drawable.emoji_1).get()
                } catch (e: Exception) {
                    Log.e("widgetTracking", "Error loading bitmap for $iconName", e)
                    Picasso.get().load(R.drawable.emoji_1).get()
                }

                val views = RemoteViews(packageName, R.layout.widget_battery).apply {
                    setTextViewText(R.id.batteryLevelBottom, "$percentage %")
                    setViewVisibility(R.id.batteryLevelBottom, View.VISIBLE)
                    setViewVisibility(R.id.batteryLevelTop, View.GONE)
                    setViewVisibility(R.id.batteryLevelCenter, View.GONE)
                    setImageViewBitmap(R.id.battery_icon, bitmap)

                    val intentClick = Intent(this@BatteryWidgetForegroundService, SplashActivity::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        putExtra(AnimationUtils.EXTRA_LABEL, iconName)
                        putExtra(AnimationUtils.EXTRA_POSITION, -1)
                    }

                    val clickPendingIntent = PendingIntent.getActivity(
                        this@BatteryWidgetForegroundService,
                        widgetId,
                        intentClick,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    setOnClickPendingIntent(R.id.widget_root, clickPendingIntent)

                    val startIntent = Intent(this@BatteryWidgetForegroundService, BatteryWidgetForegroundService::class.java).apply {
                        action = ACTION_START_SERVICE
                    }
                    val startPendingIntent = PendingIntent.getService(
                        this@BatteryWidgetForegroundService,
                        1,
                        startIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    setViewVisibility(
                        R.id.widget_start_button,
                        if (isServiceRunning) View.GONE else View.VISIBLE
                    )
                    setOnClickPendingIntent(R.id.widget_start_button, startPendingIntent)
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }.start()
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "battery_widget_channel"
        private const val NOTIFICATION_ID = 101
        const val ACTION_STOP_SERVICE = "com.lowbyte.battery.STOP_FOREGROUND_SERVICE"
        const val ACTION_START_SERVICE = "com.lowbyte.battery.START_FOREGROUND_SERVICE"
    }
}