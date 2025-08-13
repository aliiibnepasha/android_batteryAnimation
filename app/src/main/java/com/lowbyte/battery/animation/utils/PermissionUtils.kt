package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import com.lowbyte.battery.animation.BuildConfig
import com.lowbyte.battery.animation.NotchAccessibilityService

object PermissionUtils {



     fun Activity.checkAccessibilityPermission(isDebug: Boolean=false , callback: (String) -> Unit) {
        if (!isAccessibilityServiceEnabled()) {
            if (BuildConfig.DEBUG && isDebug){
                callback("GoInSettings")
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }else{
                callback("OpenBottomSheet")
               }
        } else {
            callback("Allowed")

        }
    }

     fun Activity.isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName =
            "${packageName}/${NotchAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
           contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':').any {
            it.equals(expectedComponentName, ignoreCase = true)
        }
    }




}