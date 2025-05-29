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
import com.lowbyte.battery.animation.databinding.CustomStatusBarBinding
import com.lowbyte.battery.animation.utils.AppPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotchAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())

    private var statusBarBinding: CustomStatusBarBinding? = null
    private var isLongPress = false
    private var longPressRunnable: Runnable? = null
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
        statusBarBinding?.root?.let { windowManager?.removeView(it) }
        statusBarBinding = null
        updateReceiver?.let { unregisterReceiver(it) }
        updateReceiver = null
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
                registerReceiver(updateReceiver, filter, RECEIVER_EXPORTED)
                Log.d("servicesdd","Received broadcast>= 33")

            } else {
                registerReceiver(updateReceiver, filter)
                Log.d("servicesdd","Received broadcast<33")

            }
        }
    }


    private fun createCustomStatusBar() {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP }

        statusBarBinding = CustomStatusBarBinding.inflate(LayoutInflater.from(this))
        updateStatusBarAppearance()
        setupGestures()
        updateBatteryInfo()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (preferences.isStatusBarEnabled){
            windowManager?.addView(statusBarBinding?.root, layoutParams)
        }else{
            statusBarBinding?.root?.let { windowManager?.removeView(it) }
            statusBarBinding = null
        }

    }

    private fun updateStatusBarAppearance() {
        val binding = statusBarBinding ?: return

        // Height & padding
        animateStatusBarHeight((preferences.statusBarHeight * resources.displayMetrics.density).toInt())
        binding.root.setPadding(
            (preferences.statusBarMarginLeft * resources.displayMetrics.density).toInt(), 0,
            (preferences.statusBarMarginRight * resources.displayMetrics.density).toInt(), 0
        )

        binding.root.setBackgroundColor(preferences.statusBarBgColor)

        // Icon visibilities
        with(binding) {
            batteryIcon.visibility = preferences.showBatteryIcon.toVisibility()
            wifiIcon.visibility = preferences.showWifi.toVisibility()
            hotspotIcon.visibility = preferences.showHotspot.toVisibility()
            dataIcon.visibility = preferences.showData.toVisibility()
            signalIcon.visibility = preferences.showSignal.toVisibility()
            airplaneIcon.visibility = preferences.showAirplane.toVisibility()
            timeText.visibility = preferences.showTime.toVisibility()
            dateText.visibility = preferences.showDate.toVisibility()
            batteryPercent.visibility = preferences.showBatteryPercent.toVisibility()
        }

        // Icon Sizes
        applyIconSize(binding.wifiIcon, preferences.getIconSize("size_0", 24))
        applyIconSize(binding.dataIcon, preferences.getIconSize("size_1", 24))
        applyIconSize(binding.signalIcon, preferences.getIconSize("size_2", 24))
        applyIconSize(binding.airplaneIcon, preferences.getIconSize("size_3", 24))
        applyIconSize(binding.hotspotIcon, preferences.getIconSize("size_4", 24))
        applyIconSize(binding.batteryIcon, preferences.getIconSize("batteryIconSize", 24))
        applyIconSize(binding.lottieIcon, preferences.getIconSize("lottieView", 24))

        // Text Sizes
        binding.timeText.setTextSizeInSp(preferences.getIconSize("size_5", 12))
        binding.batteryPercent.setTextSizeInSp(preferences.getIconSize("percentageSize", 12))

        // Icon colors
        binding.wifiIcon.setTint(preferences.getInt("tint_0", Color.BLACK))
        binding.dataIcon.setTint(preferences.getInt("tint_1", Color.BLACK))
        binding.signalIcon.setTint(preferences.getInt("tint_2", Color.BLACK))
        binding.airplaneIcon.setTint(preferences.getInt("tint_3", Color.BLACK))
        binding.hotspotIcon.setTint(preferences.getInt("tint_4", Color.BLACK))

        binding.timeText.setTextColor(preferences.getInt("tint_5", Color.BLACK))
        binding.dateText.setTextColor(preferences.getInt("tint_5", Color.BLACK))
        binding.batteryPercent.setTextColor(preferences.getInt("percentageColor", Color.BLACK))

        // Lottie animation
        if (preferences.statusLottieName.isNotBlank()) {
            val animId = resources.getIdentifier(preferences.statusLottieName, "raw", packageName)
            binding.lottieIcon.apply {
                visibility = if (animId != 0) View.VISIBLE else View.GONE
                if (animId != 0) {
                    setAnimation(animId)
                    playAnimation()
                }
            }
        } else {
            binding.lottieIcon.visibility = View.GONE
        }

        // Custom drawable icon
        binding.customIcon.apply {
            val resId = resources.getIdentifier(preferences.statusIconName, "drawable", packageName)
            if (preferences.statusIconName.isNotBlank() && resId != 0) {
                setImageResource(resId)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        // Battery icon override
        binding.batteryIcon.apply {
            val resId = resources.getIdentifier(preferences.batteryIconName, "drawable", packageName)
            if (preferences.batteryIconName.isNotBlank() && resId != 0) {
                setImageResource(resId)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
        if (preferences.isStatusBarEnabled){
            windowManager?.updateViewLayout(binding.root, layoutParams)
        }else{
            statusBarBinding?.root?.let { windowManager?.removeView(it) }
          //  statusBarBinding = null
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        statusBarBinding?.root?.setOnTouchListener { _, event ->
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
                    if (!isLongPress) handleTap()
                }
            }
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
        ValueAnimator.ofInt(currentHeight, targetHeight).apply {
            duration = 10
            addUpdateListener {
                layoutParams?.height = it.animatedValue as Int
                windowManager?.updateViewLayout(statusBarBinding?.root, layoutParams)
            }
            start()
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
        statusBarBinding?.timeText?.text = timeFormat.format(Date())
    }

    // ========== Extension Utils ==========

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
}