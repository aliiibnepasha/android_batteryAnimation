package com.lowbyte.battery.animation

import android.accessibilityservice.AccessibilityService
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.lowbyte.battery.animation.utils.AppPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotchAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var statusBarView: View? = null
    private var batteryIcon: ImageView? = null
    private var wifiIcon: ImageView? = null
    private var hotspotIcon: ImageView? = null
    private var dataIcon: ImageView? = null
    private var signalIcon: ImageView? = null
    private var airplaneIcon: ImageView? = null
    private var customIconImageView: ImageView? = null
    private var timeText: TextView? = null
    private var dateText: TextView? = null
    private var batteryPercent: TextView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPress = false
    private var longPressRunnable: Runnable? = null
    private var updateReceiver: BroadcastReceiver? = null


    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("servicesdd", "Service connected!")
        createCustomStatusBar()
        startTimeUpdates()
        registerUpdateReceiver()
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
        updateReceiver?.let {
            unregisterReceiver(it)
            updateReceiver = null
        }
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerUpdateReceiver() {
        if (updateReceiver == null) {
            updateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("servicesdd","Received broadcast")
                    updateStatusBarAppearance()
                }
            }
            val filter = IntentFilter("com.lowbyte.UPDATE_STATUSBAR")
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(updateReceiver, filter, Context.RECEIVER_EXPORTED)
                Log.d("servicesdd","Received broadcast>= 33")

            } else {
                registerReceiver(updateReceiver, filter)
                Log.d("servicesdd","Received broadcast<33")

            }
        }
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
        val prefs = AppPreferences.getInstance(this)

        // Height and margins
        val newHeight = (prefs.statusBarHeight * resources.displayMetrics.density).toInt()
        animateStatusBarHeight(newHeight)

        statusBarView?.setPadding(
            (prefs.statusBarMarginLeft * resources.displayMetrics.density).toInt(),
            statusBarView?.paddingTop ?: 0,
            (prefs.statusBarMarginRight * resources.displayMetrics.density).toInt(),
            statusBarView?.paddingBottom ?: 0
        )

        // Background color
        statusBarView?.setBackgroundColor(prefs.statusBarBgColor)

        // Lookup once, use many times:
        batteryIcon = statusBarView?.findViewById<ImageView>(R.id.batteryIcon)
        wifiIcon = statusBarView?.findViewById<ImageView>(R.id.wifiIcon)
        hotspotIcon = statusBarView?.findViewById<ImageView>(R.id.hotspotIcon)
        dataIcon = statusBarView?.findViewById<ImageView>(R.id.dataIcon)
        signalIcon = statusBarView?.findViewById<ImageView>(R.id.signalIcon)
        airplaneIcon = statusBarView?.findViewById<ImageView>(R.id.airplaneIcon)
        customIconImageView = statusBarView?.findViewById<ImageView>(R.id.customIcon)
        timeText = statusBarView?.findViewById<TextView>(R.id.timeText)
        dateText = statusBarView?.findViewById<TextView>(R.id.dateText)
        batteryPercent = statusBarView?.findViewById<TextView>(R.id.batteryPercent)

        val lottieView = statusBarView?.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.lottieIcon)

        // Show/hide icons by preferences
        batteryIcon?.visibility = if (prefs.showBatteryIcon) View.VISIBLE else View.GONE
        wifiIcon?.visibility = if (prefs.showWifi) View.VISIBLE else View.GONE
        hotspotIcon?.visibility = if (prefs.showHotspot) View.VISIBLE else View.GONE
        dataIcon?.visibility = if (prefs.showData) View.VISIBLE else View.GONE
        signalIcon?.visibility = if (prefs.showSignal) View.VISIBLE else View.GONE
        airplaneIcon?.visibility = if (prefs.showAirplane) View.VISIBLE else View.GONE
        timeText?.visibility = if (prefs.showTime) View.VISIBLE else View.GONE
        dateText?.visibility = if (prefs.showDate) View.VISIBLE else View.GONE
        batteryPercent?.visibility = if (prefs.showBatteryPercent) View.VISIBLE else View.GONE

        // Per-icon size
        wifiIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("size_0", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("size_0", 24) * resources.displayMetrics.density).toInt()
        )
        dataIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("size_1", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("size_1", 24) * resources.displayMetrics.density).toInt()
        )
        signalIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("size_2", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("size_2", 24) * resources.displayMetrics.density).toInt()
        )
        airplaneIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("size_3", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("size_3", 24) * resources.displayMetrics.density).toInt()
        )
        hotspotIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("size_4", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("size_4", 24) * resources.displayMetrics.density).toInt()
        )
        batteryIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("batteryIconSize", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("batteryIconSize", 24) * resources.displayMetrics.density).toInt()
        )

        val scaledSizeSp = prefs.getIconSize("size_5", 12)  // base size in sp
        val scaledSizePx = scaledSizeSp * resources.displayMetrics.scaledDensity
        timeText?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, scaledSizePx)

        val scaledSizeSpPercentage = prefs.getIconSize("percentageSize", 12)  // base size in sp
        val scaledSizePxPercentage = scaledSizeSpPercentage * resources.displayMetrics.scaledDensity
        batteryPercent?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, scaledSizePxPercentage)


        lottieView?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("lottieView", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("lottieView", 24) * resources.displayMetrics.density).toInt()
        )

// Pre Colors
        // Per-icon tint color
        wifiIcon?.setColorFilter(
            prefs.getInt("tint_0", Color.BLACK),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        dataIcon?.setColorFilter(
            prefs.getInt("tint_1", Color.BLACK),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        signalIcon?.setColorFilter(
            prefs.getInt("tint_2", Color.BLACK),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        airplaneIcon?.setColorFilter(
            prefs.getInt("tint_3", Color.BLACK),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        hotspotIcon?.setColorFilter(
            prefs.getInt("tint_4", Color.BLACK),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

// Text color (e.g. for timeText)
        timeText?.setTextColor(
            prefs.getInt("tint_5", Color.BLACK)
        )
        dateText?.setTextColor(
            prefs.getInt("tint_5", Color.BLACK)
        )

        batteryPercent?.setTextColor(
            prefs.getInt("percentageColor", Color.BLACK)
        )


        // Lottie animation
        if (prefs.statusLottieName.isNotBlank()) {
            val rawId = resources.getIdentifier(prefs.statusLottieName, "raw", packageName)
            if (rawId != 0) {
                lottieView?.setAnimation(rawId)
                lottieView?.visibility = View.VISIBLE
                lottieView?.playAnimation()
            } else {
                lottieView?.visibility = View.GONE
            }
        } else {
            lottieView?.visibility = View.GONE
        }

        // Icon from drawable




        if (prefs.statusIconName.isNotBlank()) {
            val iconRes = resources.getIdentifier(prefs.statusIconName, "drawable", packageName)
            if (iconRes != 0) {
                customIconImageView?.setImageResource(iconRes)
                customIconImageView?.visibility = View.VISIBLE
            } else {
                customIconImageView?.visibility = View.GONE
            }
        } else {
            customIconImageView?.visibility = View.GONE
        }

        //Battery Icon
         if (prefs.statusIconName.isNotBlank()) {
            val iconRes = resources.getIdentifier(prefs.batteryIconName, "drawable", packageName)
            if (iconRes != 0) {
                batteryIcon?.setImageResource(iconRes)
                batteryIcon?.visibility = View.VISIBLE
            } else {
                batteryIcon?.visibility = View.GONE
            }
        } else {
             batteryIcon?.visibility = View.GONE
        }





        windowManager?.updateViewLayout(statusBarView, layoutParams)
        Log.d("servicesdd","Received updateViewLayout")

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


    private fun updateBatteryInfo() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        batteryPercent?.text = "$batteryStatus%"
    }


    private fun animateStatusBarHeight(targetHeight: Int) {
        val currentHeight = layoutParams?.height ?: return
        if (currentHeight == targetHeight) return
        val animator = ValueAnimator.ofInt(currentHeight, targetHeight)
        animator.duration = 10 // 150-200ms is best for UI
        animator.addUpdateListener { valueAnimator ->
            layoutParams?.height = valueAnimator.animatedValue as Int
            windowManager?.updateViewLayout(statusBarView, layoutParams)
        }
        animator.start()
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


}