package com.inspiredandroid.red.notifications

import android.app.NotificationManager
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.inspiredandroid.red.data.NotificationRecord
import com.inspiredandroid.red.data.NotificationStore
import org.koin.java.KoinJavaComponent.inject

// Whether the listener service is declared in the merged manifest. The `foss` flavor
// declares it, `playStore` does not — so this is a compile-time property per flavor,
// safe to cache for the process lifetime. Shared with Platform.android.kt's
// `isNotificationsSupported`.
internal fun Context.declaresNotificationListener(): Boolean = try {
    val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES)
    info.services?.any { it.name == NOTIFICATION_LISTENER_FQN } == true
} catch (_: Exception) {
    false
}

private const val NOTIFICATION_LISTENER_FQN =
    "com.inspiredandroid.red.notifications.RedNotificationListenerService"

actual class NotificationReader actual constructor() {
    private val context: Context by inject(Context::class.java)
    private val store: NotificationStore by inject(NotificationStore::class.java)
    private val supported: Boolean by lazy { context.declaresNotificationListener() }

    actual fun isSupported(): Boolean = supported

    actual fun hasAccess(): Boolean {
        if (!supported) return false
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return false
        return try {
            nm.isNotificationListenerAccessGranted(
                android.content.ComponentName(context, NOTIFICATION_LISTENER_FQN),
            )
        } catch (_: Exception) {
            false
        }
    }

    actual suspend fun getById(id: String): NotificationRecord? {
        if (!hasAccess()) return null
        return store.getStore().firstOrNull { it.id == id }
    }

    actual suspend fun search(query: String, limit: Int, packageName: String?): List<NotificationRecord> {
        if (!hasAccess()) return emptyList()
        if (query.isBlank()) return emptyList()
        val needle = query.lowercase()
        return store.getStore()
            .asSequence()
            .filter { packageName == null || it.packageName == packageName }
            .filter {
                it.appLabel.lowercase().contains(needle) ||
                    it.title.lowercase().contains(needle) ||
                    it.text.lowercase().contains(needle)
            }
            .sortedByDescending { it.postedAtEpochMs }
            .take(limit)
            .toList()
    }

    actual suspend fun reply(id: String, text: String): Boolean {
        if (!hasAccess()) return false
        val service = RedNotificationListenerService.getActiveInstance() ?: return false
        val activeNotifications = try {
            service.activeNotifications
        } catch (e: Exception) {
            return false
        }
        val sbn = activeNotifications.firstOrNull { it.key == id } ?: return false
        val notification = sbn.notification ?: return false
        val action = notification.actions?.firstOrNull { act ->
            act.remoteInputs?.any { it.resultKey != null } == true
        } ?: return false
        val remoteInput = action.remoteInputs.first { it.resultKey != null }
        val bundle = Bundle()
        bundle.putCharSequence(remoteInput.resultKey, text)
        val intent = Intent()
        RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
        return try {
            action.actionIntent.send(context, 0, intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
