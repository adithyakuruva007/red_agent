package com.inspiredandroid.red

import android.content.Context
import android.content.Intent
import androidx.compose.ui.Modifier
import android.provider.AlarmClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.data.EmailStore
import com.inspiredandroid.red.data.MemoryStore
import com.inspiredandroid.red.data.NotificationStore
import com.inspiredandroid.red.data.SmsDraftStore
import com.inspiredandroid.red.data.SmsStore
import com.inspiredandroid.red.data.TaskStore
import com.inspiredandroid.red.mcp.McpServerManager
import com.inspiredandroid.red.network.tools.ParameterSchema
import com.inspiredandroid.red.network.tools.Tool
import com.inspiredandroid.red.network.tools.ToolInfo
import com.inspiredandroid.red.network.tools.ToolSchema
import com.inspiredandroid.red.notifications.NotificationReader
import com.inspiredandroid.red.notifications.declaresNotificationListener
import com.inspiredandroid.red.sandbox.LinuxSandboxManager
import com.inspiredandroid.red.sandbox.SandboxState
import com.inspiredandroid.red.sms.SmsReader
import com.inspiredandroid.red.sms.SmsSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.inspiredandroid.red.sms.declaresReadSms
import com.inspiredandroid.red.tools.CalendarPermissionController
import com.inspiredandroid.red.tools.CalendarRepository
import com.inspiredandroid.red.tools.CalendarResult
import com.inspiredandroid.red.tools.CommonTools
import com.inspiredandroid.red.tools.EmailTools
import com.inspiredandroid.red.tools.HeartbeatTools
import com.inspiredandroid.red.tools.NotificationHelper
import com.inspiredandroid.red.tools.NotificationPermissionController
import com.inspiredandroid.red.tools.NotificationResult
import com.inspiredandroid.red.tools.NotificationTools
import com.inspiredandroid.red.tools.ContactsTools
import com.inspiredandroid.red.contacts.ContactsReader
import com.inspiredandroid.red.tools.OpenFileTool
import com.inspiredandroid.red.tools.ProcessManagerTool
import com.inspiredandroid.red.tools.SchedulingTools
import com.inspiredandroid.red.tools.ShellCommandTool
import com.inspiredandroid.red.tools.SmsTools
import com.inspiredandroid.red.tools.SshConfigureHostTool
import com.inspiredandroid.red.tools.WebSearchTool
import com.inspiredandroid.red.tools.WebBrowserTools
import com.russhwolf.settings.BuildConfig
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.spght.encryptedprefs.EncryptedSharedPreferences
import dev.spght.encryptedprefs.MasterKey
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.android.Android
import java.io.File
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.tool_create_calendar_event_description
import red.composeapp.generated.resources.tool_create_calendar_event_name
import red.composeapp.generated.resources.tool_open_file_description
import red.composeapp.generated.resources.tool_open_file_name
import red.composeapp.generated.resources.tool_send_notification_description
import red.composeapp.generated.resources.tool_send_notification_name
import red.composeapp.generated.resources.tool_set_alarm_description
import red.composeapp.generated.resources.tool_set_alarm_name
import kotlinx.coroutines.Dispatchers
import org.koin.java.KoinJavaComponent.inject
import kotlin.coroutines.CoroutineContext

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(Android) {
    config(this)
}

actual fun getBackgroundDispatcher(): CoroutineContext = Dispatchers.IO

actual fun onDragAndDropEventDropped(event: DragAndDropEvent): PlatformFile? = null

actual val BackIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack

actual val currentPlatform: Platform = Platform.Mobile.Android

actual val defaultUiScale: Float = 1.0f

actual val isEmailSupported: Boolean = true

// Evaluated lazily because we need the Koin-injected Context. Whether READ_SMS
// is declared in the merged manifest is a build-time property (foss flavor adds
// it, playStore does not), so caching the first result is safe for the process
// lifetime. The try/catch guards screenshot / unit-test environments that may
// call `getPlatformToolDefinitions()` before Koin has been started.
actual val isSmsSupported: Boolean by lazy {
    try {
        val context: Context by inject(Context::class.java)
        context.declaresReadSms()
    } catch (_: Throwable) {
        false
    }
}

// Same lazy pattern as `isSmsSupported`: probe the merged manifest for the listener
// service. Foss flavor declares it, playStore does not.
actual val isNotificationsSupported: Boolean by lazy {
    try {
        val context: Context by inject(Context::class.java)
        context.declaresNotificationListener()
    } catch (_: Throwable) {
        false
    }
}

actual suspend fun compressImageBytes(bytes: ByteArray, mimeType: String): ByteArray {
    if (!mimeType.startsWith("image/")) return bytes
    return try {
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val maxDim = 1024
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            bitmap.scale(newWidth, newHeight)
        } else {
            bitmap
        }
        val outputStream = java.io.ByteArrayOutputStream()
        scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()
        outputStream.toByteArray()
    } catch (_: Exception) {
        bytes
    }
}

actual fun getAppFilesDirectory(): String {
    val context: Context by inject(Context::class.java)
    return context.filesDir.absolutePath
}

// Uses dev.spght:encryptedprefs-ktx — a maintained community fork of the deprecated
// androidx.security:security-crypto. We keep application-level encryption because
// secure settings store API keys, email passwords, and conversation encryption keys.
actual fun createSecureSettings(): Settings {
    val context: Context by inject(Context::class.java)
    val lazySettings = LazySettings {
        try {
            SharedPreferencesSettings(createEncryptedPrefs(context))
        } catch (_: Exception) {
            // AEADBadTagException occurs when Android Auto Backup restores the encrypted
            // prefs file but the Keystore key is hardware-bound and doesn't transfer.
            // Delete the corrupted file and recreate fresh encrypted prefs.
            context.deleteSharedPreferences("red_secure_prefs")
            SharedPreferencesSettings(createEncryptedPrefs(context))
        }
    }
    CoroutineScope(Dispatchers.IO).launch {
        lazySettings.prewarm()
    }
    return lazySettings
}

private fun createEncryptedPrefs(context: Context): android.content.SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    return EncryptedSharedPreferences.create(
        context,
        "red_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}

actual fun createLegacySettings(): Settings? {
    val context: Context by inject(Context::class.java)
    val prefs = context.getSharedPreferences("com.inspiredandroid.red_preferences", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}

// Tool definitions for Android platform
actual fun getPlatformToolDefinitions(): List<ToolInfo> = buildList {
    addAll(CommonTools.commonToolDefinitions)
    add(
        ToolInfo(
            id = "send_notification",
            name = "Send Notification",
            description = "Send a push notification to the device",
            nameRes = Res.string.tool_send_notification_name,
            descriptionRes = Res.string.tool_send_notification_description,
        ),
    )
    add(
        ToolInfo(
            id = "create_calendar_event",
            name = "Create Calendar Event",
            description = "Create a calendar event on the user's device",
            nameRes = Res.string.tool_create_calendar_event_name,
            descriptionRes = Res.string.tool_create_calendar_event_description,
        ),
    )
    add(
        ToolInfo(
            id = "set_alarm",
            name = "Set Alarm",
            description = "Set an alarm or countdown timer on the device",
            nameRes = Res.string.tool_set_alarm_name,
            descriptionRes = Res.string.tool_set_alarm_description,
        ),
    )
    add(
        ToolInfo(
            id = "open_file",
            name = "Open File",
            description = "Open sandbox files in your default Android app",
            nameRes = Res.string.tool_open_file_name,
            descriptionRes = Res.string.tool_open_file_description,
        ),
    )
    addAll(WebBrowserTools.browserToolDefinitions)
    addAll(ContactsTools.contactsToolDefinitions)
    // SMS tools are intentionally absent here: availability is driven by the Agent-tab
    // master toggles (isSmsEnabled / isSmsSendEnabled) plus the FOSS-only `isSmsSupported`
    // check in `getAvailableTools()`. Listing per-tool toggles in the Tools tab was dead
    // UI — `getAvailableTools()` never consulted them.
}

actual fun getAvailableTools(): List<Tool> {
    val context: Context by inject(Context::class.java)
    val appSettings: AppSettings by inject(AppSettings::class.java)
    val memoryStore: MemoryStore by inject(MemoryStore::class.java)
    val taskStore: TaskStore by inject(TaskStore::class.java)
    val calendarPermissionController: CalendarPermissionController by inject(CalendarPermissionController::class.java)
    val calendarRepository = CalendarRepository(context, calendarPermissionController)
    val emailStore: EmailStore by inject(EmailStore::class.java)

    return buildList {
        if (appSettings.isMemoryEnabled()) {
            addAll(CommonTools.getMemoryTools(memoryStore))
        }
        if (appSettings.isSchedulingEnabled()) {
            addAll(SchedulingTools.getSchedulingTools(taskStore))
            addAll(HeartbeatTools.getHeartbeatTools(memoryStore, appSettings))
        }
        if (appSettings.isToolEnabled(CommonTools.localTimeTool.schema.name)) {
            add(CommonTools.localTimeTool)
        }

        if (appSettings.isToolEnabled(CommonTools.ipLocationTool.schema.name)) {
            add(CommonTools.ipLocationTool)
        }

        if (appSettings.isToolEnabled(WebSearchTool.schema.name)) {
            add(WebSearchTool)
        }

        if (appSettings.isToolEnabled(WebBrowserTools.browserNavigateTool.schema.name)) {
            add(WebBrowserTools.browserNavigateTool)
        }
        if (appSettings.isToolEnabled(WebBrowserTools.browserExtractTool.schema.name)) {
            add(WebBrowserTools.browserExtractTool)
        }
        if (appSettings.isToolEnabled(WebBrowserTools.browserInteractTool.schema.name)) {
            add(WebBrowserTools.browserInteractTool)
        }
        if (appSettings.isToolEnabled(WebBrowserTools.browserScreenshotTool.schema.name)) {
            add(WebBrowserTools.browserScreenshotTool)
        }

        if (appSettings.isToolEnabled("send_notification")) {
            val notificationPermissionController: NotificationPermissionController by inject(NotificationPermissionController::class.java)
            val notificationHelper = NotificationHelper(context, notificationPermissionController)

            add(
                object : Tool {
                    override val schema = ToolSchema(
                        "send_notification",
                        "Send a push notification to the device",
                        mapOf(
                            "title" to ParameterSchema("string", "Notification title", false),
                            "message" to ParameterSchema("string", "Notification content/body", true),
                        ),
                    )

                    override suspend fun execute(args: Map<String, Any>): Any {
                        val title = args["title"] as? String ?: "Red"
                        val message = args["message"] as? String
                            ?: return mapOf("success" to false, "error" to "Message is required")

                        return when (val result = notificationHelper.sendNotification(title, message)) {
                            is NotificationResult.Success -> mapOf(
                                "success" to true,
                                "notification_id" to result.notificationId,
                                "message" to "Notification sent successfully",
                            )

                            is NotificationResult.Error -> mapOf(
                                "success" to false,
                                "error" to result.message,
                            )
                        }
                    }
                },
            )
        }

        if (appSettings.isToolEnabled("create_calendar_event")) {
            add(
                object : Tool {
                    override val schema = ToolSchema(
                        "create_calendar_event",
                        "Create a calendar event on the user's device",
                        mapOf(
                            "title" to ParameterSchema("string", "Event title", true),
                            "start_time" to ParameterSchema("string", "Start time as ISO 8601, e.g. '2024-03-15T14:30:00+02:00'. Naive (no offset) is treated as user's local time.", true),
                            "end_time" to ParameterSchema("string", "End time, same format as start_time. Defaults to 1 hour after start.", false),
                            "description" to ParameterSchema("string", "Event notes or description", false),
                            "location" to ParameterSchema("string", "Event location", false),
                            "all_day" to ParameterSchema("boolean", "Whether this is an all-day event", false),
                            "reminder_minutes" to ParameterSchema("integer", "Minutes before event to send reminder (default: 15)", false),
                        ),
                    )

                    override suspend fun execute(args: Map<String, Any>): Any {
                        val title = args["title"] as? String
                            ?: return mapOf("success" to false, "error" to "Title is required")
                        val startTime = args["start_time"] as? String
                            ?: return mapOf("success" to false, "error" to "Start time is required")
                        val endTime = args["end_time"] as? String
                        val description = args["description"] as? String
                        val location = args["location"] as? String
                        val allDay = (args["all_day"] as? Boolean) ?: false
                        val reminderMinutes = (args["reminder_minutes"] as? Number)?.toInt() ?: 15

                        return when (
                            val result = calendarRepository.createEvent(
                                title = title,
                                startTimeIso = startTime,
                                endTimeIso = endTime,
                                description = description,
                                location = location,
                                allDay = allDay,
                                reminderMinutes = reminderMinutes,
                            )
                        ) {
                            is CalendarResult.Success -> mapOf(
                                "success" to true,
                                "event_id" to result.eventId,
                                "title" to result.title,
                                "scheduled_for" to result.startTime,
                                "message" to "Event '${result.title}' created successfully for ${result.startTime}",
                            )

                            is CalendarResult.Error -> mapOf(
                                "success" to false,
                                "error" to result.message,
                            )
                        }
                    }
                },
            )
        }

        if (appSettings.isToolEnabled("set_alarm")) {
            add(
                object : Tool {
                    override val schema = ToolSchema(
                        "set_alarm",
                        "Set an alarm or countdown timer on the device. For alarms provide hour and minutes. For countdown timers provide duration_seconds.",
                        mapOf(
                            "hour" to ParameterSchema("integer", "Hour of the alarm in 24-hour format (0-23)", false),
                            "minutes" to ParameterSchema("integer", "Minutes of the alarm (0-59)", false),
                            "label" to ParameterSchema("string", "Label for the alarm or timer", false),
                            "duration_seconds" to ParameterSchema("integer", "Duration in seconds for a countdown timer", false),
                        ),
                    )

                    override suspend fun execute(args: Map<String, Any>): Any {
                        val hour = (args["hour"] as? Number)?.toInt()
                        val minutes = (args["minutes"] as? Number)?.toInt()
                        val label = args["label"] as? String
                        val durationSeconds = (args["duration_seconds"] as? Number)?.toInt()

                        val intent = if (durationSeconds != null) {
                            Intent(AlarmClock.ACTION_SET_TIMER).apply {
                                putExtra(AlarmClock.EXTRA_LENGTH, durationSeconds)
                                if (label != null) putExtra(AlarmClock.EXTRA_MESSAGE, label)
                                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                            }
                        } else if (hour != null && minutes != null) {
                            Intent(AlarmClock.ACTION_SET_ALARM).apply {
                                putExtra(AlarmClock.EXTRA_HOUR, hour)
                                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                                if (label != null) putExtra(AlarmClock.EXTRA_MESSAGE, label)
                                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                            }
                        } else {
                            return mapOf(
                                "success" to false,
                                "error" to "Provide either hour+minutes for an alarm or duration_seconds for a timer",
                            )
                        }

                        return try {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            if (durationSeconds != null) {
                                mapOf(
                                    "success" to true,
                                    "type" to "timer",
                                    "duration_seconds" to durationSeconds,
                                    "message" to "Timer set for $durationSeconds seconds",
                                )
                            } else {
                                mapOf(
                                    "success" to true,
                                    "type" to "alarm",
                                    "hour" to hour!!,
                                    "minutes" to minutes!!,
                                    "message" to "Alarm set for %02d:%02d".format(hour, minutes),
                                )
                            }
                        } catch (e: Exception) {
                            mapOf(
                                "success" to false,
                                "error" to (e.message ?: "Failed to set alarm"),
                            )
                        }
                    }
                },
            )
        }

        if (appSettings.isToolEnabled(CommonTools.openUrlTool.schema.name)) {
            add(CommonTools.openUrlTool)
        }

        if (appSettings.isToolEnabled(OpenFileTool.schema.name)) {
            add(OpenFileTool)
        }

        if (appSettings.isStorageEnabled() && appSettings.isToolEnabled("search_files", defaultEnabled = true)) {
            add(searchFilesTool)
        }
        if (appSettings.isStorageEnabled() && appSettings.isToolEnabled("read_file", defaultEnabled = true)) {
            add(readFileTool)
        }
        if (appSettings.isStorageEnabled() && appSettings.isToolEnabled("write_file", defaultEnabled = true)) {
            add(writeFileTool)
        }

        if (appSettings.isSandboxEnabled()) {
            val sandboxManager: LinuxSandboxManager by inject(LinuxSandboxManager::class.java)
            if (sandboxManager.state.value is SandboxState.Ready) {
                add(ShellCommandTool)
                add(ProcessManagerTool)
                add(SshConfigureHostTool)
            }
        }

        if (appSettings.isEmailEnabled()) {
            addAll(EmailTools.getEmailTools(emailStore))
        }

        // SMS read tools: triple-gated. `isSmsSupported` is only true on FOSS builds
        // (READ_SMS declared in merged manifest). `isSmsEnabled()` is the user toggle.
        // `hasPermission()` catches runtime revocation.
        val smsReaderForTools: SmsReader? = if (isSmsSupported) {
            val smsReader: SmsReader by inject(SmsReader::class.java)
            smsReader
        } else {
            null
        }
        if (smsReaderForTools != null && appSettings.isSmsEnabled() && smsReaderForTools.hasPermission()) {
            val smsStore: SmsStore by inject(SmsStore::class.java)
            addAll(SmsTools.getSmsReadTools(smsStore, smsReaderForTools))
        }

        // SMS send tools: independently gated on the Send toggle + SEND_SMS permission.
        // These only *stage* drafts — actual sending is user-triggered via the review banner.
        if (smsReaderForTools != null && appSettings.isSmsSendEnabled()) {
            val smsSender: SmsSender by inject(SmsSender::class.java)
            if (smsSender.hasPermission()) {
                val smsDraftStore: SmsDraftStore by inject(SmsDraftStore::class.java)
                addAll(SmsTools.getSmsSendTools(appSettings, smsDraftStore, smsReaderForTools, smsSender))
            }
        }

        // Notification tools: triple-gated. `isNotificationsSupported` is FOSS-only
        // (listener service declared in merged manifest). `isNotificationsEnabled()`
        // is the user toggle. `hasAccess()` catches system-level revocation.
        if (isNotificationsSupported && appSettings.isNotificationsEnabled()) {
            val notificationReader: NotificationReader by inject(NotificationReader::class.java)
            if (notificationReader.hasAccess()) {
                val notificationStore: NotificationStore by inject(NotificationStore::class.java)
                addAll(NotificationTools.getNotificationTools(notificationStore, notificationReader))
            }
        }

        if (appSettings.isContactsEnabled()) {
            val contactsReader: ContactsReader by inject(ContactsReader::class.java)
            if (contactsReader.hasPermission()) {
                addAll(ContactsTools.getContactsTools(contactsReader))
            }
        }

        val mcpServerManager: McpServerManager by inject(McpServerManager::class.java)
        addAll(mcpServerManager.getEnabledMcpTools())
    }
}

actual fun openUrl(url: String): Boolean = try {
    val context: Context by inject(Context::class.java)
    val parsedUri = url.toUri()
    val intent = if (parsedUri.scheme == "file") {
        val file = java.io.File(parsedUri.path!!)
        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val mimeType = android.webkit.MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension) ?: "*/*"
        Intent(Intent.ACTION_VIEW, contentUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, mimeType)
        }
    } else {
        Intent(Intent.ACTION_VIEW, parsedUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    context.startActivity(intent)
    true
} catch (_: Exception) {
    false
}

actual fun decodeToImageBitmap(bytes: ByteArray): ImageBitmap? = try {
    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
} catch (_: Exception) {
    null
}

@androidx.compose.runtime.Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

actual suspend fun saveFileToDevice(bytes: ByteArray, baseName: String, extension: String) {
    val file = FileKit.openFileSaver(suggestedName = baseName, defaultExtension = extension)
    file?.write(bytes)
}

@Composable
actual fun rememberPlatformCameraLauncher(onResult: (PlatformFile?) -> Unit): PlatformCameraLauncher {
    val launcher = rememberCameraPickerLauncher(onResult = onResult)
    return remember(launcher) {
        object : PlatformCameraLauncher {
            override fun launch() {
                launcher.launch()
            }
        }
    }
}

actual fun Modifier.platformHover(onEnter: () -> Unit, onExit: () -> Unit): Modifier = this

class LazySettings(private val loader: () -> Settings) : Settings {
    private val delegate by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { loader() }
    
    fun prewarm() {
        val dummy = delegate
    }

    override val keys: Set<String> get() = delegate.keys
    override val size: Int get() = delegate.size
    override fun clear() = delegate.clear()
    override fun remove(key: String) = delegate.remove(key)
    override fun hasKey(key: String): Boolean = delegate.hasKey(key)
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = delegate.getBoolean(key, defaultValue)
    override fun getBooleanOrNull(key: String): Boolean? = delegate.getBooleanOrNull(key)
    override fun putBoolean(key: String, value: Boolean) = delegate.putBoolean(key, value)
    
    override fun getDouble(key: String, defaultValue: Double): Double = delegate.getDouble(key, defaultValue)
    override fun getDoubleOrNull(key: String): Double? = delegate.getDoubleOrNull(key)
    override fun putDouble(key: String, value: Double) = delegate.putDouble(key, value)
    
    override fun getFloat(key: String, defaultValue: Float): Float = delegate.getFloat(key, defaultValue)
    override fun getFloatOrNull(key: String): Float? = delegate.getFloatOrNull(key)
    override fun putFloat(key: String, value: Float) = delegate.putFloat(key, value)
    
    override fun getInt(key: String, defaultValue: Int): Int = delegate.getInt(key, defaultValue)
    override fun getIntOrNull(key: String): Int? = delegate.getIntOrNull(key)
    override fun putInt(key: String, value: Int) = delegate.putInt(key, value)
    
    override fun getLong(key: String, defaultValue: Long): Long = delegate.getLong(key, defaultValue)
    override fun getLongOrNull(key: String): Long? = delegate.getLongOrNull(key)
    override fun putLong(key: String, value: Long) = delegate.putLong(key, value)
    
    override fun getString(key: String, defaultValue: String): String = delegate.getString(key, defaultValue)
    override fun getStringOrNull(key: String): String? = delegate.getStringOrNull(key)
    override fun putString(key: String, value: String) = delegate.putString(key, value)
}

actual fun PlatformFile.getPlatformPath(): String? = this.path

val searchFilesTool = object : Tool {
    override val schema = ToolSchema(
        name = "search_files",
        description = "Search for files inside a directory (e.g. `/sdcard` on Android or user home on desktop) matching a wildcard pattern.",
        parameters = mapOf(
            "directory" to ParameterSchema("string", "The directory path to search in (e.g. '/sdcard' or a specific subdirectory).", true),
            "pattern" to ParameterSchema("string", "The wildcard/filename search pattern, e.g. '*.md' or 'notes'.", true),
        ),
    )

    override suspend fun execute(args: Map<String, Any>): Any {
        val context: Context by inject(Context::class.java)
        val hasPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (!hasPermission) {
            return mapOf(
                "success" to false,
                "error" to "Storage permission not granted. Ask the user to grant storage/file permission in Settings -> Agent -> Access Local Storage."
            )
        }
        val dirPath = args["directory"]?.toString() ?: return mapOf("success" to false, "error" to "directory is required")
        val pattern = args["pattern"]?.toString() ?: return mapOf("success" to false, "error" to "pattern is required")
        return try {
            val dir = File(dirPath)
            if (!dir.exists() || !dir.isDirectory) {
                return mapOf("success" to false, "error" to "Directory does not exist or is not a directory: $dirPath")
            }
            val regex = pattern.replace(".", "\\.").replace("*", ".*").replace("?", ".").toRegex(RegexOption.IGNORE_CASE)
            val results = dir.walkTopDown()
                .maxDepth(4)
                .filter { it.isFile && regex.containsMatchIn(it.name) }
                .take(50)
                .map { mapOf("name" to it.name, "path" to it.absolutePath, "size" to it.length()) }
                .toList()
            mapOf("success" to true, "files" to results)
        } catch (e: Exception) {
            mapOf("success" to false, "error" to "Failed to search files: ${e.message}")
        }
    }
}

val readFileTool = object : Tool {
    override val schema = ToolSchema(
        name = "read_file",
        description = "Read the text content of a file on local storage.",
        parameters = mapOf(
            "path" to ParameterSchema("string", "The absolute path of the file to read.", true),
        ),
    )

    override suspend fun execute(args: Map<String, Any>): Any {
        val context: Context by inject(Context::class.java)
        val hasPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (!hasPermission) {
            return mapOf(
                "success" to false,
                "error" to "Storage permission not granted. Ask the user to grant storage/file permission in Settings -> Agent -> Access Local Storage."
            )
        }
        val path = args["path"]?.toString() ?: return mapOf("success" to false, "error" to "path is required")
        return try {
            val file = File(path)
            if (!file.exists()) {
                return mapOf("success" to false, "error" to "File not found: $path")
            }
            if (file.isDirectory) {
                return mapOf("success" to false, "error" to "Path is a directory, not a file: $path")
            }
            val maxBytes = 256 * 1024
            val size = file.length()
            val content = if (size > maxBytes) {
                file.inputStream().use { stream ->
                    val bytes = ByteArray(maxBytes)
                    stream.read(bytes)
                    bytes.decodeToString() + "\n... [TRUNCATED, FILE TOO LARGE]"
                }
            } else {
                file.readText()
            }
            mapOf("success" to true, "content" to content, "size" to size)
        } catch (e: Exception) {
            mapOf("success" to false, "error" to "Failed to read file: ${e.message}")
        }
    }
}

val writeFileTool = object : Tool {
    override val schema = ToolSchema(
        name = "write_file",
        description = "Write or edit the content of a file on local storage.",
        parameters = mapOf(
            "path" to ParameterSchema("string", "The absolute path of the file to write.", true),
            "content" to ParameterSchema("string", "The text content to write to the file.", true),
        ),
    )

    override suspend fun execute(args: Map<String, Any>): Any {
        val context: Context by inject(Context::class.java)
        val hasPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (!hasPermission) {
            return mapOf(
                "success" to false,
                "error" to "Storage permission not granted. Ask the user to grant storage/file permission in Settings -> Agent -> Access Local Storage."
            )
        }
        val path = args["path"]?.toString() ?: return mapOf("success" to false, "error" to "path is required")
        val content = args["content"]?.toString() ?: return mapOf("success" to false, "error" to "content is required")
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            mapOf("success" to true, "path" to path, "message" to "File written successfully")
        } catch (e: Exception) {
            mapOf("success" to false, "error" to "Failed to write file: ${e.message}")
        }
    }
}

actual fun saveTempScreenshot(bytes: ByteArray): String? = try {
    val context: Context by inject(Context::class.java)
    val file = java.io.File(context.cacheDir, "screenshot_${System.currentTimeMillis()}.png")
    file.outputStream().use { it.write(bytes) }
    file.absolutePath
} catch (_: Exception) {
    null
}
