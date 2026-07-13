package com.inspiredandroid.red.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.chat.ChatUiState
import com.inspiredandroid.red.ui.handCursor
import com.inspiredandroid.red.ui.redAdaptiveCardSurface
import com.inspiredandroid.red.BackIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: ChatUiState,
    onNavigateBack: () -> Unit,
    onNotificationClicked: (String) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme

    // Mark as read when screen is shown
    LaunchedEffect(Unit) {
        state.actions.markNotificationsAsRead()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.handCursor()) {
                        Icon(BackIcon, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.appNotifications.isNotEmpty()) {
                        TextButton(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), 
                            onClick = { state.actions.clearAllAppNotifications() },
                            modifier = Modifier.handCursor()
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = scheme.background,
                    titleContentColor = scheme.onBackground,
                    navigationIconContentColor = scheme.onBackground
                )
            )
        },
        containerColor = scheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (state.appNotifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notifications yet",
                        color = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.appNotifications.reversed(), key = { it.id }) { notification ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .redAdaptiveCardSurface()
                                .let { modifier ->
                                    val cid = notification.conversationId
                                    if (cid != null) {
                                        modifier
                                            .clickable {
                                                state.actions.loadConversation(cid)
                                                onNotificationClicked(cid)
                                            }
                                            .handCursor()
                                    } else {
                                        modifier
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notification.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = scheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = notification.content,
                                    fontSize = 13.sp,
                                    color = scheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            IconButton(
                                onClick = { state.actions.deleteAppNotification(notification.id) },
                                modifier = Modifier.size(32.dp).handCursor()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete notification",
                                    tint = scheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
