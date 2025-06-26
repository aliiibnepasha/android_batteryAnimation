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
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
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
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION_NOTIFICATION
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
import kotlin.math.abs

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
    private var handeRunnable: Boolean = false
    private var notificationView: View? = null
    private var notificationHandler = Handler(Looper.getMainLooper())

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
                    Log.d("NotificationReceiver", "Received broadcast: $action")
                    when (action) {
                        BROADCAST_ACTION -> {
                            Log.d("NotificationReceiver", "Custom UI update action")
                            updateStatusBarAppearance()
                            updateNotificationNotch()
                        }

                        Intent.ACTION_BATTERY_CHANGED -> {
                            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                                if (preferences.getBoolean("switch_battery", false) == false) {
                                    return
                                }
                                updateNotchIcons(
                                    showNotification = "showCharging",
                                    label = getString(R.string.charging),
                                    icon = R.drawable.ic_dynamic_battery,
                                    drawable = null,

                                    ) { isClickAllowed ->
                                    if (isClickAllowed) {
                                        Log.d("Callback", "Click is allowed, perform action")
                                        // Perform your action here
                                    } else {
                                        Log.d("Callback", "Click not allowed")
                                    }

                                }
                            } else {
                                Log.d("services", "Device not charging")
                            }
                        }

                        BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                            val state =
                                intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1)
                            when (state) {
                                BluetoothAdapter.STATE_CONNECTED -> {
                                    if (preferences.getBoolean("switch_bluetooth", false) == true) {
                                        updateNotchIcons(
                                            showNotification = "showBluetooth",
                                            label = getString(R.string.connected),
                                            icon = null,
                                            drawable = null
                                        ) { isClickAllowed ->
                                            if (isClickAllowed) {
                                                Log.d(
                                                    "Callback", "Click is allowed, perform action"
                                                )
                                                // Perform your action here
                                            } else {
                                                Log.d("Callback", "Click not allowed")
                                            }
                                        }
                                    }

                                }
                                BluetoothAdapter.STATE_DISCONNECTED -> Log.d(
                                    "services", "Bluetooth disconnected"
                                )
                            }
                        }

                        BluetoothDevice.ACTION_ACL_CONNECTED -> {
                            Log.d("services", "Bluetooth ACL connected")
                            if (preferences.getBoolean("switch_bluetooth", false) == true) {
                                updateNotchIcons(
                                    showNotification = "showBluetooth",
                                    label = getString(R.string.connected),
                                    icon = R.drawable.ic_dynamic_bluetooth,
                                    drawable = null
                                ) { isClickAllowed ->
                                    if (isClickAllowed) {
                                        Log.d("Callback", "Click is allowed, perform action")
                                        // Perform your action here
                                    } else {
                                        Log.d("Callback", "Click not allowed")
                                    }
                                }
                            }
                        }

                        AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                            val ringerMode = audioManager.ringerMode
                            when (ringerMode) {
                                AudioManager.RINGER_MODE_SILENT -> {
                                    if (preferences.getBoolean("switch_mute", false) == true) {
                                        updateNotchIcons(
                                            showNotification = "showMuted",
                                            label = getString(R.string.mute),
                                            icon = R.drawable.ic_dynamic_mute,
                                            drawable = null
                                        ) { isClickAllowed ->
                                            if (isClickAllowed) {
                                                Log.d(
                                                    "Callback", "Click is allowed, perform action"
                                                )
                                                // Perform your action here
                                            } else {
                                                Log.d("Callback", "Click not allowed")
                                            }
                                        }
                                    }

                                }
                            }
                        }

                        BROADCAST_ACTION_NOTIFICATION -> {
                            Log.d("NotificationReceiver", "Custom notch update triggered")
                            if (preferences.getBoolean("switch_notification", false) == true) {
                                Log.d("NotificationReceiver", "Feature enabled keep tracking")
                                val rmPkg = intent.getStringExtra("rm_package_name")
                                val pkg = intent.getStringExtra("package_name")

                                if (rmPkg != "" && rmPkg == pkg) {
                                    resetNotchView(false)
                                    Log.d("NotificationReceiver", "packege matched remove icon")
                                } else {
                                    Log.d(
                                        "NotificationReceiver",
                                        "New Notification to be show with Icon"
                                    )
                                    resetNotchView(true)
                                    val title = intent.getStringExtra("title")
                                    val text = intent.getStringExtra("text")
                                    val subText = intent.getStringExtra("sub_text")
                                    val bigText = intent.getStringExtra("big_text")
                                    val url = intent.getStringExtra("url")
                                    val launchIntentUri = intent.getStringExtra("launch_intent_uri")
                                    // Get app icon
                                    val appIcon: Drawable? = try {
                                        packageManager.getApplicationIcon(pkg ?: return)
                                    } catch (e: Exception) {
                                        null
                                    }

                                    Log.d(
                                        "NotificationReceiver", """
                                    receiving
                                        📦 $pkg 
                                        🔤 $title 
                                        📝 $text 
                                        🔍 $subText 
                                        📘 $bigText 
                                        🌐 $url 
                                        🎯 $launchIntentUri
                                         """.trimIndent()
                                    )
                                    updateNotificationNotch()
                                    updateNotchIcons(
                                        showNotification = "showNotification",
                                        label = "",
                                        icon = R.drawable.ic_dynamic_notification,
                                        drawable = appIcon
                                    ) { isClickAllowed ->
                                        if (isClickAllowed) {
                                            Log.d(
                                                "NotificationReceiver", "Click is allowed, open App"
                                            )
                                            showNotificationBanner(title, text, url, pkg)
//                                            val intent = packageManager.getLaunchIntentForPackage(
//                                                pkg ?: return@updateNotchIcons
//                                            )
//                                            intent?.let {
//                                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                                startActivity(it)
//                                            }
                                            // Perform your action here
                                        } else {
                                            Log.d(
                                                "NotificationReceiver",
                                                "Click not allowed dont open app"
                                            )
                                        }
                                    }

                                }

                            } else {
                                Log.d("NotificationReceiver", "else Custom notch update triggered")

                            }

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
                addAction(BROADCAST_ACTION_NOTIFICATION)
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

    private fun updateNotchIcons(
        showNotification: String?,
        label: String?,
        icon: Int?,
        drawable: Drawable?,
        onActionClick: (isClickAllow: Boolean) -> Unit
    ) {

        Log.d(
            "NotificationReceiver",
            "action to be calling label: $label , showNotification: $showNotification"
        )

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
                handeRunnable = false
                binding.iconContainerLeft.visibility = View.GONE
                binding.iconContainerRight.visibility = View.VISIBLE
                binding.notchLabel.visibility = View.VISIBLE
                binding.rightPercentage.visibility = View.VISIBLE
                binding.notchLabel.text = label
                binding.rightPercentage.text = updateBatteryInfo()
                binding.rightIcon.setImageResource(icon ?: R.drawable.ic_dynamic_battery)
                binding.statusBarRoot.setOnClickListener {
                    onActionClick(false)
                }
            }

            "showNotification" -> {
                handeRunnable = true
                binding.iconContainerLeft.visibility = View.VISIBLE
                binding.iconContainerRight.visibility = View.VISIBLE
                binding.notchLabel.visibility = View.VISIBLE
                binding.rightPercentage.visibility = View.GONE
                binding.leftIcon.setImageDrawable(drawable)
                binding.rightIcon.setImageResource(R.drawable.ic_dynamic_notification)
                binding.statusBarRoot.setOnClickListener {
                    onActionClick(true)
                }
            }

            "showMuted" -> {
                handeRunnable = false
                binding.iconContainerLeft.visibility = View.GONE
                binding.iconContainerRight.visibility = View.VISIBLE
                binding.notchLabel.visibility = View.VISIBLE
                binding.rightPercentage.visibility = View.GONE
                binding.notchLabel.text = label
                binding.rightIcon.setImageResource(icon ?: R.drawable.ic_dynamic_mute)
                binding.statusBarRoot.setOnClickListener {
                    onActionClick(false)
                }
            }

            "showBluetooth" -> {
                handeRunnable = false
                binding.iconContainerLeft.visibility = View.GONE
                binding.iconContainerRight.visibility = View.VISIBLE
                binding.notchLabel.visibility = View.VISIBLE
                binding.rightPercentage.visibility = View.GONE
                binding.notchLabel.text = label
                binding.rightIcon.setImageResource(icon ?: R.drawable.ic_dynamic_bluetooth)
                binding.statusBarRoot.setOnClickListener {
                    onActionClick(false)
                }
            }

            else -> resetNotchView(false)
        }

        // Apply enlarged layout
        windowManager?.updateViewLayout(binding.root, enlargedParams)

        // Schedule reset after 5 seconds
        handlerNotification.removeCallbacks(resetNotchRunnable)
        handlerNotification.postDelayed(resetNotchRunnable, 3800)
    }

    private fun resetNotchView(isFromNotification: Boolean) {
        val binding = notificationNotchBinding ?: return
        // Hide all UI elements
        binding.iconContainerLeft.visibility = if (isFromNotification) View.VISIBLE else View.GONE
        binding.iconContainerRight.visibility = View.GONE
        binding.rightPercentage.visibility = View.GONE
        binding.notchLabel.visibility = View.INVISIBLE
        binding.notchLabel.text = ""

        // Reset to default size and position
        updateNotificationNotch()
    }

    private fun createCustomStatusBar() {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
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
            notchWidth,
            notchHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
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
        resetNotchView(handeRunnable)
    }

    private fun updateNotificationNotch() {
        val binding = notificationNotchBinding ?: return
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val notchWidth = dpToPx(preferences.notchWidth)
        val notchHeight = dpToPx(preferences.notchHeight)
        val notchX = (screenWidth - notchWidth) / 2 + dpToPx(preferences.notchXAxis)
        val notchY = dpToPx(preferences.notchYAxis)
        if (preferences.getInt("widget_style_index", 0) == 0) {
            binding.statusBarRoot.background = resources.getDrawable(R.drawable.black_rounded_notch,null)
        } else {
            binding.statusBarRoot.background = resources.getDrawable(R.drawable.black_bottom_rounded_notch,null)
        }

        val notchParams = WindowManager.LayoutParams(
            notchWidth,
            notchHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
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
            (preferences.statusBarMarginLeft * resources.displayMetrics.density).toInt(),
            0,
            (preferences.statusBarMarginRight * resources.displayMetrics.density).toInt(),
            0
        )
        binding.root.setBackgroundColor(preferences.statusBarBgColor)

        // Real System State Checks — Replace with actual checks
        val isWifiEnabled = isWifiEnabled(this)
        val isAirplaneModeOn = isAirplaneModeOn(this)
        val isHotspotOn = isHotspotEnabled(this)
        val isMobileDataEnabled = isMobileDataEnabled(this)

        with(binding) {
            wifiIcon.visibility =
                if (preferences.showWifi && isWifiEnabled) View.VISIBLE else View.GONE
            hotspotIcon.visibility =
                if (preferences.showHotspot && isHotspotOn) View.VISIBLE else View.GONE

            if (isAirplaneModeOn) {
                airplaneIcon.visibility = if (preferences.showAirplane) View.VISIBLE else View.GONE
                dataIcon.visibility = View.GONE
                signalIcon.visibility = View.GONE
            } else {
                airplaneIcon.visibility = View.GONE
                dataIcon.visibility =
                    if (preferences.showData && isMobileDataEnabled) View.VISIBLE else View.GONE
                signalIcon.visibility = if (preferences.showSignal) View.VISIBLE else View.GONE
            }

            batteryIcon.visibility = preferences.showBatteryIcon.toVisibility()
            timeText.visibility = preferences.showTime.toVisibility()
            dateText.visibility = preferences.showDate.toVisibility()
            batteryPercent.visibility = preferences.showBatteryPercent.toVisibility()

            applyIconSize(
                this@NotchAccessibilityService, wifiIcon, preferences.getIconSize("size_0", 24)
            )
            applyIconSize(
                this@NotchAccessibilityService, dataIcon, preferences.getIconSize("size_1", 24)
            )
            applyIconSize(
                this@NotchAccessibilityService, signalIcon, preferences.getIconSize("size_2", 24)
            )
            applyIconSize(
                this@NotchAccessibilityService, airplaneIcon, preferences.getIconSize("size_3", 24)
            )
            applyIconSize(
                this@NotchAccessibilityService, hotspotIcon, preferences.getIconSize("size_4", 24)
            )
            applyIconSize(
                this@NotchAccessibilityService,
                batteryIcon,
                preferences.getIconSize("batteryIconSize", 24)
            )
            applyIconSize(
                this@NotchAccessibilityService,
                lottieIcon,
                preferences.getIconSize("lottieView", 24)
            )

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
            val batteryIconRes =
                resources.getIdentifier(preferences.batteryIconName, "drawable", packageName)
            if (preferences.batteryIconName.isNotBlank() && batteryIconRes != 0) {
                batteryIcon.setImageResource(batteryIconRes)
            }

            // Custom icon
            val customIconRes =
                resources.getIdentifier(preferences.customIconName, "drawable", packageName)
            if (preferences.customIconName.isNotBlank() && customIconRes != 0) {
                customIcon.setImageResource(customIconRes)
                customIcon.visibility = View.GONE
            } else {
                customIcon.visibility = View.GONE
            }

            // Lottie icon
            val lottieRes =
                resources.getIdentifier(preferences.statusLottieName, "raw", packageName)
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
                val deltaX = (e2.x) - (e1?.x ?: 0f)
                val deltaY = (e2.y) - (e1?.y ?: 0f)
                if (abs(deltaX) > abs(deltaY)) {
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

    private fun updateBatteryInfo(): String {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        statusBarBinding?.batteryPercent?.text = "$batteryLevel%"
        return "$batteryLevel %"
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
    fun showNotificationBanner(
        title: String?, text: String?, url: String?, pkg: String?
    ) {
        // Remove old view if exists
        if (notificationView != null) {
            windowManager?.removeView(notificationView)
            notificationHandler.removeCallbacksAndMessages(null)
        }

        // Inflate new layout
        val binding = CustomNotificationBarBinding.inflate(LayoutInflater.from(this))
        val view = binding.root

        // Set content
        if (title != null) binding.notificationTitle.text =
            title else binding.notificationTitle.visibility = View.GONE
        if (text != null) binding.notificationText.text =
            text else binding.notificationText.visibility = View.GONE
        //   binding.notificationText.text = text ?: ""
        if (!url.isNullOrEmpty()) {
            binding.notificationUrl.text = url
            binding.openUrlIcon.visibility = View.VISIBLE
            binding.notificationUrl.visibility = View.VISIBLE
        } else {
            binding.notificationUrl.visibility = View.GONE
            binding.openUrlIcon.visibility = View.GONE
        }

        // Set app icon
        try {
            val icon = packageManager.getApplicationIcon(pkg ?: "")
            binding.notificationIcon.setImageDrawable(icon)
        } catch (e: Exception) {
            binding.notificationIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        // Click to launch app
        view.setOnClickListener {
            val launchIntent =
                packageManager.getLaunchIntentForPackage(pkg ?: return@setOnClickListener)
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        }

        // Prepare layout params
        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val desiredWidth = (screenWidth * 0.9).toInt() // 90% of screen width
        val desiredHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 85f, displayMetrics
        ).toInt() // 85dp to px

        val params = WindowManager.LayoutParams(
            desiredWidth,
            desiredHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        // Add to WindowManager
        windowManager?.addView(view, params)
        notificationView = view

        // Auto-remove after 3 seconds
        notificationHandler.postDelayed({
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
            }
            notificationView = null
        }, 3000)
    }

}