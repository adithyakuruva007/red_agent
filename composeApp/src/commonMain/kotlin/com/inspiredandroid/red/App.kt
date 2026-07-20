@file:OptIn(ExperimentalMaterial3Api::class, nl.marc_apps.tts.experimental.ExperimentalVoiceApi::class)

package com.inspiredandroid.red

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Density
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.tools.CalendarPermissionController
import com.inspiredandroid.red.tools.ContactsPermissionController
import com.inspiredandroid.red.tools.NotificationPermissionController
import com.inspiredandroid.red.tools.SetupCalendarPermissionHandler
import com.inspiredandroid.red.tools.SetupContactsPermissionHandler
import com.inspiredandroid.red.tools.SetupNotificationPermissionHandler
import com.inspiredandroid.red.tools.SetupSmsPermissionHandler
import com.inspiredandroid.red.tools.SetupSmsSendPermissionHandler
import com.inspiredandroid.red.tools.SmsPermissionController
import com.inspiredandroid.red.tools.SmsSendPermissionController
import com.inspiredandroid.red.ui.RedColorScheme
import com.inspiredandroid.red.ui.rememberSandboxAwareUriHandler
import com.inspiredandroid.red.ui.Theme
import com.inspiredandroid.red.ui.navigation.AppShell
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.experimental.ExperimentalVoiceApi
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

@Composable
fun App(
    navController: androidx.navigation.NavHostController? = null,
    lightColorScheme: ColorScheme = RedColorScheme,
    darkColorScheme: ColorScheme = RedColorScheme,
    textToSpeech: TextToSpeechInstance? = null,
    isKoinStarted: Boolean = false,
    onAppOpens: ((Int) -> Unit)? = null,
) {
    setSingletonImageLoaderFactory { context: PlatformContext ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .build()
    }

    if (isKoinStarted) {
        AppContent(textToSpeech, onAppOpens)
    } else {
        KoinApplication(
            configuration = koinConfiguration {
                modules(appModule)
            },
        ) {
            AppContent(textToSpeech, onAppOpens)
        }
    }
}

@Composable
private fun AppContent(
    textToSpeech: TextToSpeechInstance?,
    onAppOpens: ((Int) -> Unit)?,
) {
    val appSettings = koinInject<AppSettings>()

    onAppOpens?.let { callback ->
        LaunchedEffect(Unit) {
            callback(appSettings.trackAppOpen())
        }
    }

    val calendarPermissionController = koinInject<CalendarPermissionController>()
    SetupCalendarPermissionHandler(calendarPermissionController)

    val notificationPermissionController = koinInject<NotificationPermissionController>()
    SetupNotificationPermissionHandler(notificationPermissionController)

    val smsPermissionController = koinInject<SmsPermissionController>()
    SetupSmsPermissionHandler(smsPermissionController)

    val smsSendPermissionController = koinInject<SmsSendPermissionController>()
    SetupSmsSendPermissionHandler(smsSendPermissionController)

    val contactsPermissionController = koinInject<ContactsPermissionController>()
    SetupContactsPermissionHandler(contactsPermissionController)

    LaunchedEffect(textToSpeech) {
        val tts = textToSpeech ?: return@LaunchedEffect
        val systemLanguage = Locale.current.language
        val matchingVoice = tts.voices
            .firstOrNull { it.languageTag.startsWith(systemLanguage) }
        if (matchingVoice != null) {
            tts.currentVoice = matchingVoice
        }
    }

    val uiScale by appSettings.uiScaleFlow.collectAsStateWithLifecycle()
    val defaultDensity = LocalDensity.current
    val scaledDensity = remember(defaultDensity, uiScale) {
        Density(defaultDensity.density * uiScale, defaultDensity.fontScale)
    }

    val sandboxController = koinInject<SandboxController>()
    val sandboxAwareUriHandler = rememberSandboxAwareUriHandler(sandboxController)

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalUriHandler provides sandboxAwareUriHandler,
    ) {
        Theme(colorScheme = RedColorScheme) {
            com.inspiredandroid.red.ui.components.FullScreenImageHost {
                AppShell(
                    textToSpeech = textToSpeech,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
