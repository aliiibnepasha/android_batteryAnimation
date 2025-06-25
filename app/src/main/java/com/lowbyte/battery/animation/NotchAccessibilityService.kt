package com.lowbyte.battery.animation

import android.accessibilityservice.AccessibilityService
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.lowbyte.battery.animation.databinding.CustomNotchBarBinding
import com.lowbyte.battery.animation.databinding.CustomNotificationBarBinding
import com.lowbyte.battery.animation.databinding.CustomStatusBarBinding
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AppPreferences
import com.lowbyte.battery.animation.utils.ServiceUtils.applyIconSize
import com.lowbyte.battery.animation.utils.ServiceUtils.dpToPx
import com.lowbyte.battery.animation.utils.ServiceUtils.isAirplaneModeOn
import com.lowbyte.battery.animation.utils.ServiceUtils.isHotspotEnabled
import com.lowbyte.battery.animation.utils.ServiceUtils.isMobileDataEnabled
import com.lowbyte.battery.animation.utils.ServiceUtils.isWifiEnabled
import com.lowbyte.battery.animation.utils.ServiceUtils.performGlobalActionByName
import com.lowbyte.battery.animation.utils.ServiceUtils.setTextSizeInSp
import com.lowbyte.battery.animation.utils.ServiceUtils.setTint
import com.lowbyte.battery.animation.utils.ServiceUtils.toVisibility
import com.lowbyte.battery.animation.utils.ServiceUtils.updateTime
import java.util.Date

class NotchAccessibilityService : AccessibilityService() {


    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val handler = Handler(Looper.getMainLooper())
    private val handlerNotification = Handler(Looper.getMainLooper())

    private var statusBarBinding: CustomStatusBarBinding? = null
    private var notificationViewBinding: CustomNotificationBarBinding? = null
    private var notificationNotchBinding: CustomNotchBarBinding? = null
    private var updateReceiver: BroadcastReceiver? = null
    private lateinit var preferences: AppPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        preferences = AppPreferences.getInstance(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createCustomStatusBar()
        //  createNotificationBar()
        createNotificationNotch()
        registerUpdateReceiver()
        startTimeUpdates()

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {

    }



    private fun registerUpdateReceiver() {
        if (updateReceiver == null) {
            updateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val action = intent?.action
                    Log.d("services", "Received broadcast: $action")

                    when (action) {
                        BROADCAST_ACTION -> {
                            Log.d("services", "Custom UI update action")
                            updateStatusBarAppearance()
                            updateNotificationNotch()
                        }

                        Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                            Log.d("services", "Airplane mode changed")
                        }

                        WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                            Log.d("services", "WiFi state changed")
                        }

                        ConnectivityManager.CONNECTIVITY_ACTION -> {
                            Log.d("services", "Connectivity changed")
                        }

                        Intent.ACTION_BATTERY_CHANGED -> {
                            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                                Log.d("services", "Device is charging (connected)")
                                updateNotchIcons("showCharging")
                            } else {
                                Log.d("services", "Device not charging")
                            }
                        }

                        BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                            val state =
                                intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)
                            when (state) {
                                BluetoothAdapter.STATE_CONNECTED -> updateNotchIcons("showBluetooth")
                                BluetoothAdapter.STATE_DISCONNECTED -> Log.d(
                                    "services", "Bluetooth disconnected"
                                )
                            }
                        }

                        BluetoothDevice.ACTION_ACL_CONNECTED -> {
                            Log.d("services", "Bluetooth ACL connected")
                            updateNotchIcons("showBluetooth")
                        }

                        AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                            val audioManager =
                                getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            val ringerMode = audioManager.ringerMode
                            when (ringerMode) {
                                AudioManager.RINGER_MODE_SILENT -> updateNotchIcons("showMuted")
                            }
                        }

                        "com.example.CUSTOM_NOTCH_UPDATE" -> {
                            Log.d("services", "Custom notch update triggered")
                            updateNotificationNotch()
                            updateNotchIcons("showNotification")
                        }
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(BROADCAST_ACTION)
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction("com.example.CUSTOM_NOTCH_UPDATE")
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(updateReceiver, filter, RECEIVER_EXPORTED)
                    Log.d("services", "Registered receiver for API 33+")
                } else {
                    registerReceiver(updateReceiver, filter)
                    Log.d("servicesdd", "Registered receiver for pre-API 33")
                }
            } catch (e: Exception) {
                Log.e("servicesdd", "Failed to register receiver: ${e.message}")
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

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        statusBarBinding = CustomStatusBarBinding.inflate(LayoutInflater.from(this))
        notificationViewBinding = CustomNotificationBarBinding.inflate(LayoutInflater.from(this))
        notificationNotchBinding = CustomNotchBarBinding.inflate(LayoutInflater.from(this))

        updateStatusBarAppearance()
        setupGestures()


        val view = statusBarBinding?.root
        if (::preferences.isInitialized && preferences.isStatusBarEnabled) {
            if (view?.parent == null) {
                windowManager?.addView(view, layoutParams)
            } else {
                Log.w("StatusBar", "View already added. Skipping re-add.")
            }
        }
    }

    private fun createNotificationBar() {
        notificationViewBinding = CustomNotificationBarBinding.inflate(LayoutInflater.from(this))
        val view = notificationViewBinding?.root ?: return
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
          //  y = dpToPx(30)
        }
        windowManager?.addView(view, params)
    }

    private fun createNotificationNotch() {
        if (notificationNotchBinding != null && notificationNotchBinding?.root?.parent != null) return

        notificationNotchBinding = CustomNotchBarBinding.inflate(LayoutInflater.from(this))
        val view = notificationNotchBinding?.root ?: return

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val notchWidth = dpToPx(preferences.notchWidth)
        val notchHeight = dpToPx(preferences.notchHeight)
        val notchX = (screenWidth - notchWidth) / 2 + dpToPx(preferences.notchXAxis)
        val notchY = dpToPx(preferences.notchYAxis)

        val notchParams = WindowManager.LayoutParams(
            notchWidth, notchHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = notchX
            y = notchY
        }

        if (preferences.isDynamicEnabled) {
            windowManager?.addView(view, notchParams)
        }
    }
    private val resetNotchRunnable = Runnable {
        resetNotchView()
    }

    fun updateNotificationNotch() {
        val binding = notificationNotchBinding ?: return
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val notchWidth = dpToPx(preferences.notchWidth)
        val notchHeight = dpToPx(preferences.notchHeight)
        val notchX = (screenWidth - notchWidth) / 2 + dpToPx(preferences.notchXAxis)
        val notchY = dpToPx(preferences.notchYAxis)

        val notchParams = WindowManager.LayoutParams(
            notchWidth, notchHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = notchX
            y = notchY
        }

        if (!preferences.isDynamicEnabled && ::preferences.isInitialized) {
            if (binding.root.parent != null) {
                windowManager?.removeView(binding.root)
            }
            return
        } else {
            if (binding.root.parent == null) {
                windowManager?.addView(binding.root, notchParams)
                Log.d("services", "Notch addView via broadcast")
            }
        }
        windowManager?.updateViewLayout(binding.root, notchParams)
        Log.d("services", "Notch updateViewLayout via broadcast")
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
                windowManager?.addView(binding.root, layoutParams)
            }
        }

        animateStatusBarHeight((preferences.statusBarHeight * resources.displayMetrics.density).toInt())
        binding.root.setPadding(
            (preferences.statusBarMarginLeft * resources.displayMetrics.density).toInt(), 0,
            (preferences.statusBarMarginRight * resources.displayMetrics.density).toInt(), 0
        )
        binding.root.setBackgroundColor(preferences.statusBarBgColor)

        // ⛔ Real System State Checks — Replace with actual checks
        val isWifiEnabled = isWifiEnabled(this)
        val isAirplaneModeOn = isAirplaneModeOn(this)
        val isHotspotOn = isHotspotEnabled(this)
        val isMobileDataEnabled = isMobileDataEnabled(this)

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

            applyIconSize(this@NotchAccessibilityService,wifiIcon, preferences.getIconSize("size_0", 24))
            applyIconSize(this@NotchAccessibilityService,dataIcon, preferences.getIconSize("size_1", 24))
            applyIconSize(this@NotchAccessibilityService,signalIcon, preferences.getIconSize("size_2", 24))
            applyIconSize(this@NotchAccessibilityService,airplaneIcon, preferences.getIconSize("size_3", 24))
            applyIconSize(this@NotchAccessibilityService,hotspotIcon, preferences.getIconSize("size_4", 24))
            applyIconSize(this@NotchAccessibilityService,batteryIcon, preferences.getIconSize("batteryIconSize", 24))
            applyIconSize(this@NotchAccessibilityService,lottieIcon, preferences.getIconSize("lottieView", 24))

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
                customIcon.visibility = View.GONE
            } else {
                customIcon.visibility = View.GONE
            }

            // Lottie icon
            val lottieRes = resources.getIdentifier(preferences.statusLottieName, "raw", packageName)
            if (preferences.statusLottieName.isNotBlank() && lottieRes != 0) {
                lottieIcon.setAnimation(lottieRes)
                lottieIcon.visibility = View.VISIBLE
                lottieIcon.playAnimation()
            } else {
                lottieIcon.visibility = View.GONE
            }

            windowManager?.updateViewLayout(binding.root, layoutParams)
        }
        updateBatteryInfo()

    }

    fun updateNotchIcons(showNotification: String) {
        val binding = notificationNotchBinding ?: return

        // Enlarge notch
        val enlargedWidth = dpToPx(270)
        val enlargedHeight = dpToPx(35)

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val notchX = (screenWidth - enlargedWidth) / 2 + dpToPx(preferences.notchXAxis)
        val notchY = dpToPx(preferences.notchYAxis)

        val enlargedParams = WindowManager.LayoutParams(
            enlargedWidth,
            enlargedHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = notchX
            y = notchY
        }

        when (showNotification) {
            "showCharging" -> {
                binding.iconContainerLeft.visibility = View.GONE
                binding.iconContainerRight.visibility = View.VISIBLE
                binding.notchLabel.visibility = View.VISIBLE
                binding.notchLabel.text = getString(R.string.charging)
                binding.rightIcon.setImageResource(R.drawable.ic_dynamic_battery)
            }

            "showNotification" -> {
                binding.iconContainerLeft.visibility = View.VISIBLE
                binding.iconContainerRight.visibility = View.VISIBLE
                binding.notchLabel.visibility = View.VISIBLE
                binding.rightIcon.setImageResource(R.drawable.ic_dynamic_notification)
            }

            "showMuted" -> {
                binding.iconContainerLeft.visibility = View.GONE
                binding.iconContainerRight.visibility = View.VISIBLE
                binding.notchLabel.visibility = View.VISIBLE
                binding.notchLabel.text = getString(R.string.mute)
                binding.rightIcon.setImageResource(R.drawable.ic_dynamic_mute)
            }

            "showBluetooth" -> {
                binding.iconContainerLeft.visibility = View.GONE
                binding.iconContainerRight.visibility = View.VISIBLE
                binding.notchLabel.visibility = View.VISIBLE
                binding.notchLabel.text = getString(R.string.connected)
                binding.rightIcon.setImageResource(R.drawable.ic_dynamic_bluetooth)
            }

            else -> resetNotchView()
        }

        // Apply enlarged layout
        windowManager?.updateViewLayout(binding.root, enlargedParams)

        // Schedule reset after 5 seconds
        handlerNotification.removeCallbacks(resetNotchRunnable)
        handlerNotification.postDelayed(resetNotchRunnable, 3800)
    }


    private fun resetNotchView() {
        val binding = notificationNotchBinding ?: return

        // Hide all UI elements
        binding.iconContainerLeft.visibility = View.INVISIBLE
        binding.iconContainerRight.visibility = View.INVISIBLE
        binding.notchLabel.visibility = View.INVISIBLE
        binding.notchLabel.text = ""
        // Reset to default size and position
        updateNotificationNotch()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestures() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (preferences.isGestureMode){
                    handleTap()
                }

                return true
            }

            override fun onLongPress(e: MotionEvent) {
                if (preferences.isGestureMode){
                    handleLongPress()
                }

            }

            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                val deltaX = (e2?.x ?: 0f) - (e1?.x ?: 0f)
                val deltaY = (e2?.y ?: 0f) - (e1?.y ?: 0f)
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 100) {
                        if (preferences.isGestureMode){
                            performGlobalActionByName(
                                this@NotchAccessibilityService,
                                preferences,
                                preferences.getString(
                                    "swipeLeftToRightAction",
                                    getString(R.string.action_do_nothing)
                                ) ?: getString(R.string.action_do_nothing)
                            )

                        }
                    } else if (deltaX < -100) {
                        if (preferences.isGestureMode){
                            performGlobalActionByName(
                                this@NotchAccessibilityService,
                                preferences,
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

        statusBarBinding?.root?.setOnTouchListener { _, event ->
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
        performGlobalActionByName(
            this,
            preferences,
            preferences.getString(
                "gestureAction",
                getString(R.string.action_do_nothing)
            ) ?: getString(R.string.action_do_nothing)
        )
    }

    private fun handleLongPress() {
        performGlobalActionByName(
            this,
            preferences,
            preferences.getString(
                "longPressAction",
                getString(R.string.action_do_nothing)
            ) ?: getString(R.string.action_do_nothing)
        )

    }

    private fun startTimeUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                statusBarBinding?.timeText?.text =  updateTime().format(Date())
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        statusBarBinding?.root?.let { windowManager?.removeView(it) }
        notificationViewBinding?.root?.let { windowManager?.removeView(it) }
        notificationNotchBinding?.root?.let { windowManager?.removeView(it) }
        statusBarBinding = null
        notificationViewBinding = null
        notificationNotchBinding = null
        updateReceiver?.let { unregisterReceiver(it) }
        updateReceiver = null
        handler.removeCallbacks(resetNotchRunnable)

    }
}