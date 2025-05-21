package com.lowbyte.battery.animation

import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
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
import android.os.BatteryManager
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

class NotchAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var statusBarView: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        createFakeStatusBar()
    }

    private fun createFakeStatusBar() {
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, 100,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.TOP

        statusBarView = LayoutInflater.from(this).inflate(R.layout.custom_status_bar, null)
        updateBatteryInfo()

        // Gesture listener
        statusBarView?.setOnTouchListener(object : View.OnTouchListener {
            private var downX = 0f
            private var downY = 0f
            private val SWIPE_THRESHOLD = 100

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                    }
                    MotionEvent.ACTION_UP -> {
                        val deltaX = event.x - downX
                        val deltaY = event.y - downY

                        if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                            if (deltaX > 0) {
                                showToast("Swipe Right")
                            } else {
                                showToast("Swipe Left")
                            }
                        } else if (Math.abs(deltaY) < 20) {
                            showToast("Tap")
                        }
                    }
                    MotionEvent.ACTION_BUTTON_PRESS -> {
                        showToast("Long Press")
                    }
                }
                return true
            }
        })

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager?.addView(statusBarView, layoutParams)
    }

    private fun showToast(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBatteryInfo() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        statusBarView?.findViewById<TextView>(R.id.batteryPercent)?.text = "$batteryStatus%"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }
    override fun onInterrupt() {

    }
}