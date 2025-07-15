package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.app.Dialog
import android.util.Log
import com.lowbyte.battery.animation.R

object AdLoadingDialogManager {

    private var dialog: Dialog? = null

    fun show(activity: Activity, durationMillis: Long, onDialogDismiss: () -> Unit) {
        // Prevent showing multiple dialogs
        if (dialog?.isShowing == true) return

        dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(R.layout.dialog_ad_loading)
            setCancelable(false)
        }

        try {
            dialog?.show()
            activity.window?.decorView?.postDelayed({
                if (dialog?.isShowing == true) {
                    dialog?.dismiss()
                    dialog?.cancel()
                }
                onDialogDismiss()
            }, durationMillis)
        } catch (e: Exception) {
            dialog?.dismiss()
            dialog?.cancel()
            Log.e("AdDialog", "Failed to show loading dialog: ${e.localizedMessage}")
            onDialogDismiss()
        }
    }

    fun dismiss() {
        dialog?.takeIf { it.isShowing }?.dismiss()
        dialog = null
    }
}