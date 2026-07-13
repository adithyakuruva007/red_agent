package com.inspiredandroid.red

import android.content.Context
import android.content.Intent
import com.inspiredandroid.red.data.AppSettings
import org.koin.java.KoinJavaComponent.inject

actual fun createDaemonController(): DaemonController = AndroidDaemonController()

class AndroidDaemonController : DaemonController {
    private val context: Context by inject(Context::class.java)
    private val appSettings: AppSettings by inject(AppSettings::class.java)

    fun shouldAutoStart(): Boolean = appSettings.isDaemonEnabled()

    override fun start() {
        if (appSettings.isDaemonEnabled()) {
            val intent = Intent(context, DaemonService::class.java)
            context.startForegroundService(intent)
        }
    }

    override fun stop() {
        val intent = Intent(context, DaemonService::class.java)
        context.stopService(intent)
    }
}
