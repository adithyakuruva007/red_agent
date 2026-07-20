package com.inspiredandroid.red.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.PlatformBackHandler
import com.inspiredandroid.red.ui.RedBgDeep
import com.inspiredandroid.red.ui.alerts.AlertsTab
import com.inspiredandroid.red.ui.chat.ChatListTab
import com.inspiredandroid.red.ui.chat.ChatViewModel
import com.inspiredandroid.red.ui.chat.reference.ReferenceChatScreen
import com.inspiredandroid.red.ui.sandbox.SandboxTab
import com.inspiredandroid.red.ui.settings.SettingsTab
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechInstance
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppShell(
    textToSpeech: TextToSpeechInstance? = null,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })
    var isChatOpen by remember { mutableStateOf(false) }
    val activeTab = remember(pagerState.currentPage) { NavTab.entries[pagerState.currentPage] }
    val chatViewModel: ChatViewModel = koinViewModel()
    val chatState by chatViewModel.state.collectAsStateWithLifecycle()

    val chatBadgeCount = remember(chatState.hasUnreadHeartbeat) {
        if (chatState.hasUnreadHeartbeat) 1 else 0
    }
    val alertBadgeCount = remember(chatState.appNotifications) {
        chatState.appNotifications.count { !it.isRead }
    }

    PlatformBackHandler(enabled = isChatOpen, onBack = { isChatOpen = false })

    Box(modifier = modifier.fillMaxSize().background(RedBgDeep)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier.weight(1f),
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (NavTab.entries[page]) {
                        NavTab.Chats -> ChatListTab(
                            onOpenChat = { isChatOpen = true },
                        )
                        NavTab.Sandbox -> SandboxTab()
                        NavTab.Alerts -> AlertsTab(
                            notifications = chatState.appNotifications,
                            actions = chatState.actions,
                        )
                        NavTab.Settings -> SettingsTab()
                    }
                }
            }

            BottomNavBar(
                activeTab = activeTab,
                onTabSelected = { tab ->
                    scope.launch {
                        pagerState.animateScrollToPage(tab.ordinal)
                    }
                },
                chatBadgeCount = chatBadgeCount,
                alertBadgeCount = alertBadgeCount,
                sandboxOnline = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        // Chat overlay
        AnimatedContent(
            targetState = isChatOpen,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it / 5 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 5 } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "chatOverlay",
        ) { open ->
            if (open) {
                ConversationScreen(
                    chatState = chatState,
                    textToSpeech = textToSpeech,
                    onBack = { isChatOpen = false },
                )
            }
        }
    }
}

@Composable
private fun ConversationScreen(
    chatState: com.inspiredandroid.red.ui.chat.ChatUiState,
    textToSpeech: TextToSpeechInstance?,
    onBack: () -> Unit,
) {
    ReferenceChatScreen(
        chatState = chatState,
        onBack = onBack,
        modifier = Modifier.fillMaxSize(),
    )
}
