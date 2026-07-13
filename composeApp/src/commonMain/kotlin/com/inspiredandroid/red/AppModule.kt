package com.inspiredandroid.red

import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.data.ConversationStorage
import com.inspiredandroid.red.data.DataRepository
import com.inspiredandroid.red.data.EmailStore
import com.inspiredandroid.red.data.HeartbeatManager
import com.inspiredandroid.red.data.MemoryStore
import com.inspiredandroid.red.data.NotificationStore
import com.inspiredandroid.red.data.RemoteDataRepository
import com.inspiredandroid.red.data.SmsDraftStore
import com.inspiredandroid.red.data.SmsStore
import com.inspiredandroid.red.data.TaskScheduler
import com.inspiredandroid.red.data.TaskStore
import com.inspiredandroid.red.data.ToolExecutor
import com.inspiredandroid.red.data.runMigrations
import com.inspiredandroid.red.email.EmailPoller
import com.inspiredandroid.red.inference.createLocalInferenceEngine
import com.inspiredandroid.red.mcp.McpServerManager
import com.inspiredandroid.red.network.Requests
import com.inspiredandroid.red.notifications.NotificationReader
import com.inspiredandroid.red.contacts.ContactsReader
import com.inspiredandroid.red.skills.SkillManager
import com.inspiredandroid.red.sms.SmsPoller
import com.inspiredandroid.red.sms.SmsReader
import com.inspiredandroid.red.sms.SmsSender
import com.inspiredandroid.red.tools.CalendarPermissionController
import com.inspiredandroid.red.tools.ContactsPermissionController
import com.inspiredandroid.red.tools.NotificationListenerController
import com.inspiredandroid.red.tools.NotificationPermissionController
import com.inspiredandroid.red.tools.SmsPermissionController
import com.inspiredandroid.red.tools.SmsSendPermissionController
import com.inspiredandroid.red.ui.chat.ChatViewModel
import com.inspiredandroid.red.ui.sandbox.SandboxFileBrowserViewModel
import com.inspiredandroid.red.ui.sandbox.SandboxPackagesViewModel
import com.inspiredandroid.red.ui.sandbox.SandboxSessionViewModel
import com.inspiredandroid.red.ui.settings.SandboxViewModel
import com.inspiredandroid.red.ui.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<CalendarPermissionController> { CalendarPermissionController() }
    single<ContactsPermissionController> { ContactsPermissionController() }
    single<NotificationPermissionController> { NotificationPermissionController() }
    single<SmsPermissionController> { SmsPermissionController() }
    single<SmsSendPermissionController> { SmsSendPermissionController() }
    single<SmsReader> { SmsReader() }
    single<SmsSender> { SmsSender() }
    single<NotificationListenerController> { NotificationListenerController() }
    single<NotificationReader> { NotificationReader() }
    single<ContactsReader> { ContactsReader() }
    single<AppSettings> {
        AppSettings(createSecureSettings()).also {
            it.runMigrations(createLegacySettings())
        }
    }
    single<Requests> {
        Requests()
    }
    single<ConversationStorage> {
        ConversationStorage(get())
    }
    single<ToolExecutor> {
        ToolExecutor()
    }
    single<MemoryStore> {
        MemoryStore(get())
    }
    single<TaskStore> {
        TaskStore(get())
    }
    single<EmailStore> {
        EmailStore(get())
    }
    single<EmailPoller> {
        EmailPoller(get<EmailStore>())
    }
    single<SmsStore> {
        SmsStore(get())
    }
    single<SmsPoller> {
        SmsPoller(get<SmsStore>(), get<SmsReader>())
    }
    single<SmsDraftStore> {
        SmsDraftStore(get())
    }
    single<NotificationStore> {
        NotificationStore(get())
    }

    single<HeartbeatManager> {
        HeartbeatManager(get(), get(), get(), get())
    }
    single<McpServerManager> {
        McpServerManager(get())
    }
    single<SkillManager> {
        SkillManager(get<SandboxController>())
    }
    single<RemoteDataRepository> {
        RemoteDataRepository(
            requests = get(),
            appSettings = get(),
            conversationStorage = get(),
            toolExecutor = get(),
            memoryStore = get(),
            taskStore = get(),
            heartbeatManager = get(),
            emailStore = get(),
            emailPoller = get(),
            smsStore = get(),
            smsPoller = get(),
            smsReader = get(),
            smsPermissionController = get(),
            smsSendPermissionController = get(),
            contactsPermissionController = get(),
            smsSender = get(),
            smsDraftStore = get(),
            notificationStore = get(),
            notificationListenerController = get(),
            mcpServerManager = get(),
            skillManager = get(),
            sandboxController = get(),
            localInferenceEngine = createLocalInferenceEngine(),
        )
    }
    single<DataRepository> { get<RemoteDataRepository>() }

    single<TaskScheduler> {
        TaskScheduler(
            get<DataRepository>(),
            get(),
            get(),
            get(),
            get(),
            get<EmailPoller>(),
            get<SmsStore>(),
            get<SmsPoller>(),
            get<NotificationStore>(),
        )
    }
    single<DaemonController> { createDaemonController() }
    single<SandboxController> { createSandboxController() }
    viewModel { SettingsViewModel(get<DataRepository>(), get<DaemonController>(), get<NotificationPermissionController>(), get<TaskScheduler>()) }
    viewModel { SandboxViewModel(get<DataRepository>(), get<SandboxController>()) }
    viewModel { SandboxFileBrowserViewModel(get<SandboxController>()) }
    viewModel { SandboxPackagesViewModel(get<SandboxController>()) }
    viewModel { SandboxSessionViewModel(get<SandboxController>(), get<DataRepository>()) }

    viewModel { ChatViewModel(get<DataRepository>(), get<TaskScheduler>()) }
}
