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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.PlatformBackHandler
import com.inspiredandroid.red.data.AppNotification
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgDeep
import com.inspiredandroid.red.ui.RedBgElevated
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.RedTextTertiary
import com.inspiredandroid.red.ui.alerts.AlertsTab
import com.inspiredandroid.red.ui.chat.ChatListTab
import com.inspiredandroid.red.ui.chat.ChatViewModel
import com.inspiredandroid.red.ui.chat.reference.ReferenceChatScreen
import com.inspiredandroid.red.ui.handCursor
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

    var activePopUpNotification by remember { mutableStateOf<AppNotification?>(null) }
    var lastSeenNotificationId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(chatState.appNotifications) {
        val newestUnread = chatState.appNotifications.firstOrNull { !it.isRead }
        if (newestUnread != null && newestUnread.id != lastSeenNotificationId) {
            lastSeenNotificationId = newestUnread.id
            activePopUpNotification = newestUnread
            kotlinx.coroutines.delay(5000)
            if (activePopUpNotification?.id == newestUnread.id) {
                activePopUpNotification = null
            }
        }
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
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, top = 4.dp, bottom = 14.dp),
            )
        }

        // Pop Up Alert Banner
        AnimatedVisibility(
            visible = activePopUpNotification != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .zIndex(10f),
        ) {
            activePopUpNotification?.let { notification ->
                PopUpAlertBanner(
                    notification = notification,
                    onDismiss = { activePopUpNotification = null },
                    onClick = {
                        activePopUpNotification = null
                        scope.launch {
                            pagerState.animateScrollToPage(NavTab.Alerts.ordinal)
                        }
                    },
                )
            }
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

@Composable
private fun PopUpAlertBanner(
    notification: AppNotification,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RedBgElevated)
            .border(1.dp, RedAccent.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .handCursor()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(RedAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = RedAccent,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                color = RedTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = notification.content,
                color = RedTextSecondary,
                fontSize = 12.5.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp).handCursor(),
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Dismiss",
                tint = RedTextTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
