package com.lowbyte.battery.animation

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt
import com.lowbyte.battery.animation.utils.AppPreferences

class NotchAccessibilityService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var statusBarView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isLongPress = false
    private var longPressRunnable: Runnable? = null
    private var updateReceiver: BroadcastReceiver? = null

    // Status bar customization
    private var statusBarHeight = 24 // Default height in dp
    private var iconColor = Color.WHITE
    private var iconSize = 24 // Default size in dp

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
        layoutParams?.height = (prefs.statusBarHeight * resources.displayMetrics.density).toInt()


        statusBarView?.setPadding(
            (prefs.statusBarMarginLeft * resources.displayMetrics.density).toInt(),
            statusBarView?.paddingTop ?: 0,
            (prefs.statusBarMarginRight * resources.displayMetrics.density).toInt(),
            statusBarView?.paddingBottom ?: 0
        )

        // Background color
        statusBarView?.setBackgroundColor(prefs.statusBarBgColor)

        // Lookup once, use many times:
        val wifiIcon = statusBarView?.findViewById<ImageView>(R.id.wifiIcon)
        val hotspotIcon = statusBarView?.findViewById<ImageView>(R.id.hotspotIcon)
        val dataIcon = statusBarView?.findViewById<ImageView>(R.id.dataIcon)
        val signalIcon = statusBarView?.findViewById<ImageView>(R.id.signalIcon)
        val airplaneIcon = statusBarView?.findViewById<ImageView>(R.id.airplaneIcon)
        val timeText = statusBarView?.findViewById<TextView>(R.id.timeText)
        val dateText = statusBarView?.findViewById<TextView>(R.id.dateText)

        // Show/hide icons by preferences
        wifiIcon?.visibility = if (prefs.showWifi) View.VISIBLE else View.GONE
        hotspotIcon?.visibility = if (prefs.showHotspot) View.VISIBLE else View.GONE
        dataIcon?.visibility = if (prefs.showData) View.VISIBLE else View.GONE
        signalIcon?.visibility = if (prefs.showSignal) View.VISIBLE else View.GONE
        airplaneIcon?.visibility = if (prefs.showAirplane) View.VISIBLE else View.GONE

        timeText?.visibility = if (prefs.showTime) View.VISIBLE else View.GONE
        dateText?.visibility = if (prefs.showDate) View.VISIBLE else View.GONE

        // Per-icon size
        wifiIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("wifi", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("wifi", 24) * resources.displayMetrics.density).toInt()
        )
        hotspotIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("hotspot", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("hotspot", 24) * resources.displayMetrics.density).toInt()
        )
        dataIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("data", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("data", 24) * resources.displayMetrics.density).toInt()
        )
        signalIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("signal", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("signal", 24) * resources.displayMetrics.density).toInt()
        )
        airplaneIcon?.layoutParams = LinearLayout.LayoutParams(
            (prefs.getIconSize("airplane", 24) * resources.displayMetrics.density).toInt(),
            (prefs.getIconSize("airplane", 24) * resources.displayMetrics.density).toInt()
        )

        // Lottie animation
        val lottieView = statusBarView?.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.lottieIcon)
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
        val imageView = statusBarView?.findViewById<ImageView>(R.id.customIcon)

        if (prefs.statusIconName.isNotBlank()) {
            val iconRes = resources.getIdentifier(prefs.statusIconName, "drawable", packageName)
            if (iconRes != 0) {
                imageView?.setImageResource(iconRes)
                imageView?.visibility = View.VISIBLE
            } else {
                imageView?.visibility = View.GONE
            }
        } else {
            imageView?.visibility = View.GONE
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