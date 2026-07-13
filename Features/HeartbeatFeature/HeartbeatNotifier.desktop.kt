package com.inspiredandroid.red

import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.util.Timer
import java.util.TimerTask

enum class DesktopPlatform {
    MAC, WINDOWS, LINUX, OTHER
}

private fun detectDesktopPlatform(): DesktopPlatform {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("mac") -> DesktopPlatform.MAC
        os.contains("win") -> DesktopPlatform.WINDOWS
        os.contains("nix") || os.contains("nux") || os.contains("aix") -> DesktopPlatform.LINUX
        else -> DesktopPlatform.OTHER
    }
}

/**
 * Posts a native OS notification. Each platform has its own surface:
 *   - macOS: `osascript` invokes the user-facing Notification Center.
 *   - Linux: `notify-send` (libnotify) is the freedesktop standard and ships in most distros.
 *   - Windows: AWT [java.awt.SystemTray] briefly registers a tray icon to display a balloon
 *     toast, then removes it so we don't leave a persistent tray entry.
 * All paths swallow failures — if the OS hook is missing the in-app heartbeat banner still fires.
 */
actual fun sendHeartbeatNotification(title: String, body: String) {
    try {
        when (detectDesktopPlatform()) {
            DesktopPlatform.MAC -> {
                val safeTitle = title.replace("\\", "\\\\").replace("\"", "\\\"")
                val safeBody = body.replace("\\", "\\\\").replace("\"", "\\\"")
                ProcessBuilder("osascript", "-e", "display notification \"$safeBody\" with title \"$safeTitle\"")
                    .start()
            }

            DesktopPlatform.WINDOWS -> {
                if (!SystemTray.isSupported()) return
                val tray = SystemTray.getSystemTray()
                val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
                val trayIcon = TrayIcon(image, "Red")
                trayIcon.isImageAutoSize = true
                tray.add(trayIcon)
                trayIcon.displayMessage(title, body, TrayIcon.MessageType.INFO)
                Timer(true).schedule(
                    object : TimerTask() {
                        override fun run() = tray.remove(trayIcon)
                    },
                    5_000,
                )
            }

            DesktopPlatform.LINUX -> {
                ProcessBuilder("notify-send", "--", title, body).start()
            }

            DesktopPlatform.OTHER -> {
                // Unsupported fallback
            }
        }
    } catch (_: Exception) {
        // Fallback silently
    }
}
