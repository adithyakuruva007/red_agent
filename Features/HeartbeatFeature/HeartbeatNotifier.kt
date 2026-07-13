package com.inspiredandroid.red

/**
 * Fires a background push notification for a heartbeat that produced a non-trivial
 * response. Android additionally wires a tap-to-open-heartbeat deep link via its
 * receiver. Desktop runs platform-native scripts or balloon notifications.
 */
expect fun sendHeartbeatNotification(title: String, body: String)
