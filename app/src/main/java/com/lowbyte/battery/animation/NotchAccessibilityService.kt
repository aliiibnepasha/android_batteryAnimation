package com.lowbyte.battery.animation

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

class NotchAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var statusBarView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPress = false
    private var longPressRunnable: Runnable? = null

    // Status bar customization
    private var statusBarHeight = 24 // Default height in dp
 //   private var statusBarColor = "#80000000".toColorInt()
    private var iconColor = Color.WHITE
    private var iconSize = 24 // Default size in dp

    override fun onServiceConnected() {
        super.onServiceConnected()
        createCustomStatusBar()
        startTimeUpdates()
    }

    private fun createCustomStatusBar() {
        try {
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                y = 0 // Position at the very top
                flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            }

            statusBarView = LayoutInflater.from(this).inflate(R.layout.custom_status_bar, null)
            updateStatusBarAppearance()
            setupGestures()
            updateBatteryInfo()

            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager?.addView(statusBarView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateStatusBarAppearance() {
        statusBarView?.apply {
          //  setBackgroundColor(statusBarColor)
            
            // Update icon colors
            findViewById<ImageView>(R.id.batteryIcon)?.setColorFilter(iconColor)
            findViewById<ImageView>(R.id.wifiIcon)?.setColorFilter(iconColor)
            findViewById<ImageView>(R.id.signalIcon)?.setColorFilter(iconColor)
            
            // Update text colors
            findViewById<TextView>(R.id.timeText)?.setTextColor(iconColor)
            findViewById<TextView>(R.id.batteryPercent)?.setTextColor(iconColor)
            
            // Update icon sizes
            val iconSizePx = (iconSize * resources.displayMetrics.density).toInt()
            findViewById<ImageView>(R.id.batteryIcon)?.layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx)
            findViewById<ImageView>(R.id.wifiIcon)?.layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx)
            findViewById<ImageView>(R.id.signalIcon)?.layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        statusBarView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isLongPress = false
                    longPressRunnable = Runnable {
                        isLongPress = true
                        handleLongPress()
                    }
                    handler.postDelayed(longPressRunnable!!, 500)
                }
                MotionEvent.ACTION_UP -> {
                    longPressRunnable?.let { handler.removeCallbacks(it) }
                    if (!isLongPress) {
                        handleTap()
                    }
                }
            }
            true
        }
    }

    private fun handleTap() {
        Toast.makeText(this, "Status Bar Tapped", Toast.LENGTH_SHORT).show()
    }

    private fun handleLongPress() {
        Toast.makeText(this, "Status Bar Long Pressed", Toast.LENGTH_SHORT).show()
    }

    private fun startTimeUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                updateTime()
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateTime() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        statusBarView?.findViewById<TextView>(R.id.timeText)?.text = timeFormat.format(Date())
    }

    private fun updateBatteryInfo() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        statusBarView?.findViewById<TextView>(R.id.batteryPercent)?.text = "$batteryStatus%"
    }

    // Public methods to customize status bar
    fun setStatusBarHeight(height: Int) {
        statusBarHeight = height
        layoutParams?.height = (height * resources.displayMetrics.density).toInt()
        windowManager?.updateViewLayout(statusBarView, layoutParams)
    }

    fun setStatusBarColor(color: Int) {
    //    statusBarColor = color
        statusBarView?.setBackgroundColor(color)
    }

    fun setIconColor(color: Int) {
        iconColor = color
        updateStatusBarAppearance()
    }

    fun setIconSize(size: Int) {
        iconSize = size
        updateStatusBarAppearance()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        statusBarView?.let {
            windowManager?.removeView(it)
            statusBarView = null
        }
    }
}