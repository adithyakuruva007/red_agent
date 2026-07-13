package com.inspiredandroid.red

interface DaemonController {
    fun start()
    fun stop()
}

expect fun createDaemonController(): DaemonController
