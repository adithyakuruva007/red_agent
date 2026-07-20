package com.inspiredandroid.red.ui.chat.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedBgDeep
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedTextTertiary
import com.inspiredandroid.red.ui.chat.ChatUiState
import com.inspiredandroid.red.ui.chat.History
import kotlinx.coroutines.launch

@Composable
fun ReferenceChatScreen(
    chatState: ChatUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val conversationTitle = remember(chatState.currentConversationId, chatState.savedConversations) {
        chatState.savedConversations.find { it.id == chatState.currentConversationId }?.title ?: ""
    }

    val onCopyMessage: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar("Copied to clipboard")
        }
    }

    LaunchedEffect(chatState.history.size, chatState.isLoading) {
        if (chatState.history.isNotEmpty()) {
            listState.animateScrollToItem(chatState.history.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(RedBgDeep)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReferenceHeader(
                title = conversationTitle,
                isLoading = chatState.isLoading,
                onBack = onBack,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(RedBorderHairline),
            )

            if (chatState.history.isEmpty() && !chatState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Start a conversation",
                        color = RedTextTertiary,
                        fontSize = 15.sp,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(chatState.history, key = { it.id }) { entry ->
                        when (entry.role) {
                            History.Role.USER -> {
                                ReferenceUserBubble(
                                    content = entry.content,
                                    onCopy = onCopyMessage,
                                )
                            }

                            History.Role.ASSISTANT -> {
                                if (entry.content.isNotEmpty() && !entry.isThinking) {
                                    ReferenceAgentBubbleWithAvatar(
                                        content = entry.content,
                                        avatarLabel = conversationTitle,
                                        onCopy = onCopyMessage,
                                    )
                                }
                            }

                            History.Role.TOOL_EXECUTING -> {
                                val toolName = entry.toolName ?: "tool"
                                val label = when {
                                    entry.content == "execute_shell_command" -> "Running shell command…"
                                    entry.content.startsWith("web_search") -> "Searching the web…"
                                    else -> "Running $toolName…"
                                }
                                ReferenceToolTrace(
                                    completed = if (entry.toolCallId != null) listOf(label) else emptyList(),
                                    inProgress = if (entry.toolCallId == null) listOf(label) else emptyList(),
                                )
                            }

                            History.Role.TOOL -> {
                                // Don't show completed tool results in the UI
                            }
                        }
                    }

                    if (chatState.isLoading) {
                        item(key = "typing") {
                            ReferenceTypingIndicator(avatarLabel = conversationTitle)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(RedBorderHairline),
            )

            ReferenceComposer(
                text = chatState.inputText.text,
                onTextChange = { chatState.actions.updateInputText(androidx.compose.ui.text.input.TextFieldValue(it)) },
                onSend = { chatState.actions.ask(chatState.inputText.text) },
                onCancel = { chatState.actions.cancel() },
                isLoading = chatState.isLoading,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
        )
    }
}
