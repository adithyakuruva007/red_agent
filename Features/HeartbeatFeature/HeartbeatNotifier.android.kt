package com.inspiredandroid.red

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.inspiredandroid.red.shared.R
import org.koin.java.KoinJavaComponent.inject

/**
 * Intent extra read by MainActivity when the user taps a heartbeat notification. The
 * receiver forwards the signal to `DataRepository.requestOpenHeartbeat()` so the
 * ChatViewModel observer can load the heartbeat conversation.
 */
const val EXTRA_OPEN_HEARTBEAT = "com.inspiredandroid.red.OPEN_HEARTBEAT"

private const val CHANNEL_ID = "red_heartbeat_channel"

/**
 * Fixed ID so a new heartbeat report replaces any earlier unread one in the tray
 * instead of piling up. The app only ever has one pending heartbeat conversation.
 */
private const val HEARTBEAT_NOTIFICATION_ID = 9002

actual fun sendHeartbeatNotification(title: String, body: String) {
    val context: Context by inject(Context::class.java)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create the channel if it doesn't exist (safe to call repeatedly)
    val channel = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.settings_heartbeat),
        NotificationManager.IMPORTANCE_DEFAULT,
    )
    notificationManager.createNotificationChannel(channel)

    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(EXTRA_OPEN_HEARTBEAT, true)
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = Notification.Builder(context, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(body)
        .setSmallIcon(android.R.drawable.ic_popup_sync)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(HEARTBEAT_NOTIFICATION_ID, notification)
}
