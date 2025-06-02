package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresPermission
import com.lowbyte.battery.animation.R

object GestureActionUtils {

    fun performAction(context: Context, action: String) {
        when (action) {

//            context.getString(R.string.action_open_notifications) -> {
//                try {
//                    val statusBarService = context.getSystemService("statusbar")
//                    val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
//                    val expandNotificationsPanel = statusBarManagerClass.getMethod("expandNotificationsPanel")
//                    expandNotificationsPanel.invoke(statusBarService)
//                } catch (e: Exception) {
//                    Toast.makeText(context, "Unable to open notifications", Toast.LENGTH_SHORT).show()
//                }
//            }

//            context.getString(R.string.action_open_control_centre) -> {
//                try {
//                    val statusBarService = context.getSystemService("statusbar")
//                    val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
//                    val expandSettingsPanel = statusBarManagerClass.getMethod("expandSettingsPanel")
//                    expandSettingsPanel.invoke(statusBarService)
//                } catch (e: Exception) {
//                    Toast.makeText(context, "Unable to open control center", Toast.LENGTH_SHORT).show()
//                }
//            }

//            context.getString(R.string.action_home_action) -> {
//                val intent = Intent(Intent.ACTION_MAIN)
//                intent.addCategory(Intent.CATEGORY_HOME)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                context.startActivity(intent)
//            }

//            context.getString(R.string.action_recent_action) -> {
//                // TODO Not Working
//                try {
//                    val recentIntent = Intent(Intent.ACTION_ALL_APPS)
//                    recentIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    context.startActivity(recentIntent)
//                } catch (e: Exception) {
//                    Toast.makeText(context, "Recent apps not accessible on this device.", Toast.LENGTH_SHORT).show()
//                }
//            }

            context.getString(R.string.action_take_screenshot) -> {
                Toast.makeText(context, "Start screen recording flow (requires MediaProjection permission).", Toast.LENGTH_SHORT).show()
                if (context is Activity) {
                    val mgr = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    val intent = mgr.createScreenCaptureIntent()
                    context.startActivityForResult(intent, 1001) // handle in Activity
                }
                // TODO Handel Permission
            }

//            context.getString(R.string.action_lock_screen) -> {
//                // TODO Crashing on my device
//                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//                val component = ComponentName(context, AdminReceiver::class.java)
//                if (dpm.isAdminActive(component)) {
//                    dpm.lockNow()
//                } else {
//                    Toast.makeText(context, "Please enable device admin for this app to lock screen.", Toast.LENGTH_LONG).show()
//                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
//                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
//                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Permission needed to lock screen.")
//                    context.startActivity(intent)
//                }
//            }

//            context.getString(R.string.action_back_action) -> {
//                //TODO Not working on my device
//                if (context is Activity) {
//                    context.performGlobalAction(GLOBAL_ACTION_BACK)
//                    val intent = Intent(Intent.GLOBAL_ACTION_BACK)
//                    intent.addCategory(Intent.CATEGORY_HOME)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    context.startActivity(intent)
//
//                }
//            }



            context.getString(R.string.action_power_options) -> {
                // TODO Power Menu not supported on all devices
//                try {
//                    val intent = Intent(Intent.ACTION_POWER_MENU)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    context.startActivity(intent)
//                } catch (e: Exception) {
//                    Toast.makeText(context, "Power Menu not supported on your device.", Toast.LENGTH_SHORT).show()
//                }
            }


            context.getString(R.string.action_quick_scroll_to_up) -> {
                // TODO Pending
//                Toast.makeText(context, "Scrolling to top...", Toast.LENGTH_SHORT).show()
//                // Close notification shade if already open
//                context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            }




            else -> {
                Toast.makeText(context, "Unknown action", Toast.LENGTH_SHORT).show()
            }
        }
    }
}