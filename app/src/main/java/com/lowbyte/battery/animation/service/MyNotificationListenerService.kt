package com.lowbyte.battery.animation.service

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION_NOTIFICATION

class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        val extras: Bundle = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val icon = notification.smallIcon


        // Get launch intent
        val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(packageName)

        // Try to get possible URL from extras
        val possibleUrl = extras.keySet().find { key ->
            val value = extras.get(key)
            value is String && value.startsWith("http")
        }?.let { extras.getString(it) }

        Log.d(
            "NotificationReceiver", """
            sending
            📱 Package: $packageName
            🏷️ Title: $title
            📩 Text: $text
            🔍 SubText: $subText
            📝 BigText: $bigText
            🌐 URL: $possibleUrl
            🎯 Launch Intent: ${launchIntent?.toUri(0)}
            🔑 Keys: ${extras.keySet()}
        """.trimIndent()
        )

        val broadcastIntent = Intent(BROADCAST_ACTION_NOTIFICATION).apply {
            putExtra("package_name", packageName)
            putExtra("title", title)
            putExtra("text", text)
            putExtra("sub_text", subText)
            putExtra("big_text", bigText)
            putExtra("url", possibleUrl)
            putExtra("launch_intent_uri", launchIntent?.toUri(0))
        }
        sendBroadcast(broadcastIntent)

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("NotificationListener", "Notification removed from ${sbn.packageName}")
    }
}