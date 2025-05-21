package com.lowbyte.battery.animation

import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.graphics.PixelFormat
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

class NotchAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var notchView: View? = null
    private var notificationBanner: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentNotification: NotificationData? = null

    data class NotificationData(
        val packageName: String,
        val title: String,
        val text: String,
        val intent: PendingIntent?
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotchView()
    }

    private fun createNotchView() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            100,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        notchView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0x80000000.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                100
            )
        }

        windowManager?.addView(notchView, params)
    }

    private fun showNotificationBanner(notification: NotificationData) {
        currentNotification = notification
        
        val bannerParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        bannerParams.gravity = Gravity.TOP
        bannerParams.y = 100

        notificationBanner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF333333.toInt())
            setPadding(20, 20, 20, 20)

            addView(TextView(context).apply {
                text = notification.title
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 16f
            })

            addView(TextView(context).apply {
                text = notification.text
                setTextColor(0xFFCCCCCC.toInt())
                textSize = 14f
            })

            setOnClickListener {
//                notification.intent?.let { intent ->
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//                }
                hideNotificationBanner()
            }
        }

        windowManager?.addView(notificationBanner, bannerParams)
        
        handler.postDelayed({
            hideNotificationBanner()
        }, 5000)
    }

    private fun hideNotificationBanner() {
        notificationBanner?.let {
            windowManager?.removeView(it)
            notificationBanner = null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            val notification = event.parcelableData as? android.app.Notification ?: return
            
            val notificationData = NotificationData(
                packageName = packageName,
                title = notification.extras.getString("android.title") ?: "",
                text = notification.extras.getString("android.text") ?: "",
                intent = notification.contentIntent
            )
            
            showNotificationBanner(notificationData)
        }
    }

    override fun onInterrupt() {
        hideNotificationBanner()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideNotificationBanner()
        notchView?.let {
            windowManager?.removeView(it)
            notchView = null
        }
    }
} 