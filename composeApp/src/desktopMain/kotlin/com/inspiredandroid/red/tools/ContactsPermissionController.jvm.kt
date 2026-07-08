package com.inspiredandroid.red.tools

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class ContactsPermissionController actual constructor() {
    private val _permissionRequested = MutableStateFlow(false)
    actual val permissionRequested: StateFlow<Boolean> = _permissionRequested

    actual fun hasPermission(): Boolean {
        return true
    }

    actual suspend fun requestPermission(): Boolean {
        return true
    }

    actual fun onPermissionResult(granted: Boolean) {
        // No-op for desktop
    }
}

@Composable
actual fun SetupContactsPermissionHandler(controller: ContactsPermissionController) {
    // No-op for desktop
}
