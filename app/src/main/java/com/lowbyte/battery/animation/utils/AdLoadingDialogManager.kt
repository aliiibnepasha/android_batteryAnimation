package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.app.Dialog
import android.util.Log
import com.lowbyte.battery.animation.R

object AdLoadingDialogManager {

    private var dialog: Dialog? = null

    fun show(activity: Activity, durationMillis: Long, onDialogDismiss: () -> Unit) {
        if (dialog?.isShowing == true) return
            dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
                setContentView(R.layout.dialog_ad_loading)
                setCancelable(false)
            }



        try {
            Log.d("AdManager", "Show dialogeg ad and dialog")
            dialog?.show()
            activity.window?.decorView?.postDelayed({
                try {
                    // Check if activity is valid before attempting to dismiss dialog
                    if (!activity.isFinishing && !activity.isDestroyed && dialog?.isShowing == true) {
                        dialog?.dismiss()
                        dialog?.cancel()
                    }
                } catch (e: Exception) {
                    Log.e("AdDialog", "Exception dismissing dialog: ${e.localizedMessage}")
                } finally {
                    onDialogDismiss()
                }
            }, durationMillis)

        } catch (e: Exception) {
            dialog?.dismiss()
            dialog?.cancel()
            Log.e("AdDialog", "Failed to show loading dialog: ${e.localizedMessage}")
            onDialogDismiss()
        }
    }

    fun dismiss() {
        try {
            dialog?.takeIf { it.isShowing }?.dismiss()
        } catch (e: Exception) {
            Log.e("AdDialog", "Safe dismiss failed: ${e.localizedMessage}")
        } finally {
            dialog = null
        }
    }
}