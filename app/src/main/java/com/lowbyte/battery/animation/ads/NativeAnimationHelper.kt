package com.lowbyte.battery.animation.ads

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout

class NativeAnimationHelper(
    context: Activity,
    adId: String,
    showAdRemoteFlag: Boolean,
    isProUser: Boolean,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailed: (() -> Unit)? = null,
    adContainer: FrameLayout? = null
) : BaseNativeAdHelper(context, adId, showAdRemoteFlag,  isProUser, onAdLoaded, onAdFailed, adContainer) {
    override val logTag: String = "AdHelperNativeAnimation"
}