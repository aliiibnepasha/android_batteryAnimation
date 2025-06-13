package com.lowbyte.battery.animation.utils

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseAnalyticsUtils {

    private val screenStartTimes = mutableMapOf<String, Long>()

    fun logClickEvent(activity: Activity, eventName: String, params: Map<String, String>? = null) {
        val bundle = Bundle()
        params?.forEach { bundle.putString(it.key, it.value) }
        FirebaseAnalytics.getInstance(activity).logEvent(eventName, bundle)
    }

    fun logScreenView(activity: Activity, screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, activity.localClassName)
        }
        FirebaseAnalytics.getInstance(activity).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logScreenView(fragment: Fragment, screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, fragment.javaClass.simpleName)
        }
        FirebaseAnalytics.getInstance(fragment.requireContext()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun startScreenTimer(screenId: String) {
        screenStartTimes[screenId] = SystemClock.elapsedRealtime()
    }

    fun stopScreenTimer(activity: Activity, screenId: String) {
        val startTime = screenStartTimes.remove(screenId)
        startTime?.let {
            val duration = SystemClock.elapsedRealtime() - it
            val bundle = Bundle().apply {
                putString("screen_id", screenId)
                putLong("screen_duration_ms", duration)
            }
            FirebaseAnalytics.getInstance(activity).logEvent("screen_duration", bundle)
        }
    }

    fun stopScreenTimer(fragment: Fragment, screenId: String) {
        val startTime = screenStartTimes.remove(screenId)
        startTime?.let {
            val duration = SystemClock.elapsedRealtime() - it
            val bundle = Bundle().apply {
                putString("screen_id", screenId)
                putLong("screen_duration_ms", duration)
            }
            FirebaseAnalytics.getInstance(fragment.requireContext()).logEvent("screen_duration", bundle)
        }
    }
}