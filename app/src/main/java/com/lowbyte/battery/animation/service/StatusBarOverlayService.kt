package com.lowbyte.battery.animation.service

import android.accessibilityservice.AccessibilityService
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
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
import com.lowbyte.battery.animation.R
import com.lowbyte.battery.animation.databinding.CustomStatusBarBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusBarOverlayService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())

    private var statusBarBinding: CustomStatusBarBinding? = null
    private var updateReceiver: BroadcastReceiver? = null
    private lateinit var preferences: AppPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        preferences = AppPreferences.getInstance(this)
        createCustomStatusBar()
        startTimeUpdates()
        registerUpdateReceiver()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)

        statusBarBinding?.root?.let {
            try {
                if (it.parent != null) {
                    windowManager?.removeViewImmediate(it)
                }
            } catch (e: Exception) {
                Log.e("StatusBar", "removeViewImmediate error: ${e.localizedMessage}")
            }
        }

        statusBarBinding = null

        updateReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.e("StatusBar", "Receiver not registered or already unregistered")
            }
        }
        updateReceiver = null
    }

    private fun registerUpdateReceiver() {
        if (updateReceiver == null) {
            updateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("StatusBar", "Received broadcast")
                    updateStatusBarAppearance()
                }
            }

            val filter = IntentFilter().apply {
                addAction("com.lowbyte.UPDATE_STATUSBAR")
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                addAction(Intent.ACTION_BATTERY_CHANGED)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(updateReceiver, filter, RECEIVER_EXPORTED)
            } else {
                registerReceiver(updateReceiver, filter)
            }
        }
    }

    private fun createCustomStatusBar() {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
            PixelFormat.TRANSLUCENT
        ).apply { 
            gravity = Gravity.TOP
            // Set initial height for Pixel 5
            height = if (Build.MODEL.contains("Pixel 5")) {
                (48 * resources.displayMetrics.density).toInt()
            } else {
                (24 * resources.displayMetrics.density).toInt()
            }
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        statusBarBinding = CustomStatusBarBinding.inflate(LayoutInflater.from(applicationContext))

        // Set initial padding
        statusBarBinding?.root?.setPadding(
            (preferences.statusBarMarginLeft * resources.displayMetrics.density).toInt(),
            (0 * resources.displayMetrics.density).toInt(),
            (preferences.statusBarMarginRight * resources.displayMetrics.density).toInt(),
            (0 * resources.displayMetrics.density).toInt()
        )

        // Set initial visibility
        statusBarBinding?.root?.visibility = View.VISIBLE

        updateStatusBarAppearance()
        setupGestures()
        updateBatteryInfo()

        val view = statusBarBinding?.root
        if (::preferences.isInitialized && preferences.isStatusBarEnabled) {
            if (view?.parent == null) {
                try {
                    windowManager?.addView(view, layoutParams)
                    // Force an immediate update after adding the view
                    handler.postDelayed({
                        updateStatusBarAppearance()
                        // Ensure view stays visible
                        view?.visibility = View.VISIBLE
                        // Force another update after a short delay
                        handler.postDelayed({
                            updateStatusBarAppearance()
                        }, 500)
                    }, 100)
                } catch (e: Exception) {
                    Log.e("StatusBar", "Error adding view: ${e.message}")
                }
            } else {
                Log.w("StatusBar", "View already added. Skipping re-add.")
            }
        }
    }

    private fun updateStatusBarAppearance() {
        val binding = statusBarBinding ?: return

        if (!preferences.isStatusBarEnabled && ::preferences.isInitialized) {
            if (binding.root.parent != null) {
                windowManager?.removeView(binding.root)
            }
            return
        } else {
            if (binding.root.parent == null) {
                try {
                    windowManager?.addView(binding.root, layoutParams)
                    // Ensure view is visible after adding
                    binding.root.visibility = View.VISIBLE
                    // Force another update after a short delay
                    handler.postDelayed({
                        updateStatusBarAppearance()
                    }, 500)
                } catch (e: Exception) {
                    Log.e("StatusBar", "Error adding view in update: ${e.message}")
                    return
                }
            }
        }

        // Ensure view is visible
        binding.root.visibility = View.VISIBLE

        // Calculate minimum height based on device
        val minHeight = if (Build.MODEL.contains("Pixel 5")) {
            (48 * resources.displayMetrics.density).toInt()
        } else {
            (24 * resources.displayMetrics.density).toInt()
        }

        // Ensure height is never 0 or too small
        val targetHeight = maxOf(
            (preferences.statusBarHeight * resources.displayMetrics.density).toInt(),
            minHeight
        )

        animateStatusBarHeight(targetHeight)

        // Update padding with proper density scaling
        binding.root.setPadding(
            (preferences.statusBarMarginLeft * resources.displayMetrics.density).toInt(),
            (0 * resources.displayMetrics.density).toInt(),
            (preferences.statusBarMarginRight * resources.displayMetrics.density).toInt(),
            (0 * resources.displayMetrics.density).toInt()
        )

        binding.root.setBackgroundColor(preferences.statusBarBgColor)

        val isWifiEnabled = isWifiEnabled()
        val isAirplaneModeOn = isAirplaneModeOn()
        val isHotspotOn = isHotspotEnabled()
        val isMobileDataEnabled = isMobileDataEnabled()

        with(binding) {
            wifiIcon.visibility = if (preferences.showWifi && isWifiEnabled) View.VISIBLE else View.GONE
            hotspotIcon.visibility = if (preferences.showHotspot && isHotspotOn) View.VISIBLE else View.GONE

            if (isAirplaneModeOn) {
                airplaneIcon.visibility = if (preferences.showAirplane) View.VISIBLE else View.GONE
                dataIcon.visibility = View.GONE
                signalIcon.visibility = View.GONE
            } else {
                airplaneIcon.visibility = View.GONE
                dataIcon.visibility = if (preferences.showData && isMobileDataEnabled) View.VISIBLE else View.GONE
                signalIcon.visibility = if (preferences.showSignal) View.VISIBLE else View.GONE
            }

            batteryIcon.visibility = preferences.showBatteryIcon.toVisibility()
            timeText.visibility = preferences.showTime.toVisibility()
            dateText.visibility = preferences.showDate.toVisibility()
            batteryPercent.visibility = preferences.showBatteryPercent.toVisibility()

            applyIconSize(wifiIcon, preferences.getIconSize("size_0", 24))
            applyIconSize(dataIcon, preferences.getIconSize("size_1", 24))
            applyIconSize(signalIcon, preferences.getIconSize("size_2", 24))
            applyIconSize(airplaneIcon, preferences.getIconSize("size_3", 24))
            applyIconSize(hotspotIcon, preferences.getIconSize("size_4", 24))
            applyIconSize(batteryIcon, preferences.getIconSize("batteryIconSize", 24))

            timeText.setTextSizeInSp(preferences.getIconSize("size_5", 12))
            batteryPercent.setTextSizeInSp(preferences.getIconSize("percentageSize", 12))

            wifiIcon.setTint(preferences.getInt("tint_0", Color.BLACK))
            dataIcon.setTint(preferences.getInt("tint_1", Color.BLACK))
            signalIcon.setTint(preferences.getInt("tint_2", Color.BLACK))
            airplaneIcon.setTint(preferences.getInt("tint_3", Color.BLACK))
            hotspotIcon.setTint(preferences.getInt("tint_4", Color.BLACK))
            timeText.setTextColor(preferences.getInt("tint_5", Color.BLACK))
            dateText.setTextColor(preferences.getInt("tint_5", Color.BLACK))
            batteryPercent.setTextColor(preferences.getInt("percentageColor", Color.BLACK))

            // Battery icon
            val batteryIconRes = resources.getIdentifier(preferences.batteryIconName, "drawable", packageName)
            if (preferences.batteryIconName.isNotBlank() && batteryIconRes != 0) {
                batteryIcon.setImageResource(batteryIconRes)
            }

            // Custom icon
            val customIconRes = resources.getIdentifier(preferences.customIconName, "drawable", packageName)
            if (preferences.customIconName.isNotBlank() && customIconRes != 0) {
                customIcon.setImageResource(customIconRes)
                customIcon.visibility = View.VISIBLE
            } else {
                customIcon.visibility = View.GONE
            }

            windowManager?.updateViewLayout(binding.root, layoutParams)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (preferences.isGestureMode) {
                    handleTap()
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (preferences.isGestureMode) {
                    Toast.makeText(this@StatusBarOverlayService, "Double Tap Detected", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                if (preferences.isGestureMode) {
                    handleLongPress()
                }
            }

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                val deltaX = (e2?.x ?: 0f) - (e1?.x ?: 0f)
                val deltaY = (e2?.y ?: 0f) - (e1?.y ?: 0f)
                
                // Handle vertical fling (swipe down)
                if (Math.abs(deltaY) > Math.abs(deltaX)) {
                    if (deltaY > 100) {
                        // Swipe down detected
                        statusBarBinding?.root?.visibility = View.VISIBLE
                        updateStatusBarAppearance()
                    }
                } else {
                    // Handle horizontal fling
                    if (deltaX > 100) {
                        if (preferences.isGestureMode) {
                            performGlobalActionByName(
                                preferences.getString(
                                    "swipeLeftToRightAction",
                                    getString(R.string.action_do_nothing)
                                ) ?: getString(R.string.action_do_nothing)
                            )
                        }
                    } else if (deltaX < -100) {
                        if (preferences.isGestureMode) {
                            performGlobalActionByName(
                                preferences.getString(
                                    "swipeRightToLeftAction",
                                    getString(R.string.action_do_nothing)
                                ) ?: getString(R.string.action_do_nothing)
                            )
                        }
                    }
                }
                return true
            }
        })

        statusBarBinding?.root?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.visibility = View.VISIBLE
                    updateStatusBarAppearance()
                }
                MotionEvent.ACTION_MOVE -> {
                    view.visibility = View.VISIBLE
                    updateStatusBarAppearance()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.visibility = View.VISIBLE
                    updateStatusBarAppearance()
                    // Force another update after touch ends
                    handler.postDelayed({
                        updateStatusBarAppearance()
                    }, 100)
                }
            }
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun updateBatteryInfo() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        statusBarBinding?.batteryPercent?.text = "$batteryLevel%"
    }

    private fun animateStatusBarHeight(targetHeight: Int) {
        val currentHeight = layoutParams?.height ?: return
        if (currentHeight == targetHeight) return

        // Cancel any ongoing animations
        handler.removeCallbacksAndMessages(null)

        ValueAnimator.ofInt(currentHeight, targetHeight).apply {
            duration = 150 // Increased duration for smoother animation
            addUpdateListener {
                val newHeight = it.animatedValue as Int
                layoutParams?.height = newHeight
                try {
                    windowManager?.updateViewLayout(statusBarBinding?.root, layoutParams)
                } catch (e: Exception) {
                    Log.e("StatusBar", "Error updating layout: ${e.message}")
                }
            }
            start()
        }
    }

    private fun handleTap() {
        performGlobalActionByName(
            preferences.getString(
                "gestureAction",
                getString(R.string.action_do_nothing)
            ) ?: getString(R.string.action_do_nothing)
        )
    }

    private fun handleLongPress() {
        performGlobalActionByName(
            preferences.getString(
                "longPressAction",
                getString(R.string.action_do_nothing)
            ) ?: getString(R.string.action_do_nothing)
        )
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
        statusBarBinding?.timeText?.text = timeFormat.format(Date())
    }

    private fun isAirplaneModeOn(): Boolean {
        return Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

    private fun isWifiEnabled(): Boolean {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    private fun isMobileDataEnabled(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE && cm.activeNetworkInfo?.isConnected == true
    }

    private fun isHotspotEnabled(): Boolean {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        return try {
            val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    // Extension Utils
    private fun Boolean.toVisibility(): Int = if (this) View.VISIBLE else View.GONE

    private fun View.setTint(color: Int) {
        (this as? ImageView)?.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    private fun View.setTextSizeInSp(sp: Int) {
        if (this is TextView) {
            val px = sp * resources.displayMetrics.scaledDensity
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, px)
        }
    }

    private fun applyIconSize(view: View?, sizeDp: Int) {
        val px = (sizeDp * resources.displayMetrics.density).toInt()
        view?.layoutParams = LinearLayout.LayoutParams(px, px)
    }

    fun performGlobalActionByName(actionName: String) {
        when (actionName) {
            getString(R.string.action_do_nothing) -> {
                // Do nothing
            }
            getString(R.string.action_back_action) -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            getString(R.string.action_home_action) -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            getString(R.string.action_recent_action) -> {
                performGlobalAction(GLOBAL_ACTION_RECENTS)
            }
            getString(R.string.action_lock_screen) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.lock_screen_not_supported_on_your_device),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            getString(R.string.action_open_notifications) -> {
                performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            }
            getString(R.string.action_power_options) -> {
                performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            }
            getString(R.string.action_quick_scroll_to_up) -> {
                performGlobalAction(GESTURE_SWIPE_UP)
            }
            getString(R.string.action_open_control_centre) -> {
                performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            }
            getString(R.string.action_take_screenshot) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.take_screenshot_not_supported_on_your_device),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                // Unsupported action
            }
        }
    }
} 