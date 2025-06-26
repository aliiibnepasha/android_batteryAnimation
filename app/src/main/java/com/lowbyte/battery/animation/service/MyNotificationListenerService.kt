package com.lowbyte.battery.animation.service

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.lowbyte.battery.animation.utils.AnimationUtils.BROADCAST_ACTION_NOTIFICATION

class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.notification == null) {
            // No notification or null entry, optionally clear state or notify
            Log.w("NotificationListener", "Null notification posted, clearing or ignoring.")
            return
        }

        val packageName = sbn.packageName

        // 🚫 Exclude system notifications
        if (packageName == "android" || packageName.startsWith("com.android") || packageName == "com.google.android.settings.intelligence") {
            Log.d("NotificationListener", "System notification skipped: $packageName")
            return
        }

        val notification = sbn.notification
        val extras: Bundle = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        // Get launch intent
        val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(packageName)

        // Try to extract a possible URL
        val possibleUrl = extras.keySet().find { key ->
            val value = extras.get(key)
            value is String && value.startsWith("http")
        }?.let { extras.getString(it) }

        Log.d(
            "NotificationReceiver", """
            ✅ Sending Latest Notification
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

        // Send only latest valid non-system notification
        val broadcastIntent = Intent(BROADCAST_ACTION_NOTIFICATION).apply {
            putExtra("rm_package_name", "")
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

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d("NotificationListener", "Notification removed from ${sbn?.packageName}")
        val broadcastIntent = Intent(BROADCAST_ACTION_NOTIFICATION).apply {
            putExtra("rm_package_name", packageName)
        }
        sendBroadcast(broadcastIntent)
    }
}