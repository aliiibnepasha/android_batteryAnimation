package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseAnalyticsUtils {

    private val screenStartTimes = mutableMapOf<String, Long>()

    /** Log simple click event from Activity or Fragment */
    fun logClickEvent(source: Any, eventName: String, params: Map<String, String>? = null) {
        val context = when (source) {
            is Activity -> source
            is Fragment -> source.requireContext()
            else -> return
        }
        val bundle = Bundle().apply {
            params?.forEach { putString(it.key, it.value) }
        }
        FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle)
    }

    /** Log screen view from Activity */
    fun logScreenView(activity: Activity, screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, activity.localClassName)
        }
        FirebaseAnalytics.getInstance(activity).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /** Log screen view from Fragment */
    fun logScreenView(fragment: Fragment, screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, fragment::class.java.simpleName)
        }
        FirebaseAnalytics.getInstance(fragment.requireContext()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /** Start screen timer — recommended in onCreate/onResume */
    fun startScreenTimer(screenId: String) {
        screenStartTimes[screenId] = SystemClock.elapsedRealtime()
    }

    /** Stop screen timer — recommended in onPause/onDestroy — for Activity */
    fun stopScreenTimer(activity: Activity, screenId: String) {
        screenStartTimes.remove(screenId)?.let { startTime ->
            val duration = SystemClock.elapsedRealtime() - startTime
            val bundle = Bundle().apply {
                putString("screen_id", screenId)
                putLong("screen_duration_ms", duration)
            }
            FirebaseAnalytics.getInstance(activity).logEvent("screen_duration", bundle)
        }
    }

    /** Stop screen timer — for Fragment */
    fun stopScreenTimer(fragment: Fragment, screenId: String) {
        screenStartTimes.remove(screenId)?.let { startTime ->
            val duration = SystemClock.elapsedRealtime() - startTime
            val bundle = Bundle().apply {
                putString("screen_id", screenId)
                putLong("screen_duration_ms", duration)
            }
            FirebaseAnalytics.getInstance(fragment.requireContext()).logEvent("screen_duration", bundle)
        }
    }
}