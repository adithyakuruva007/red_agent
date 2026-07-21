package com.inspiredandroid.red.ui.chat

import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.inspiredandroid.red.ui.RedOutlinedTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgDeep
import com.inspiredandroid.red.ui.RedBgElevated
import com.inspiredandroid.red.ui.RedBgElevated2
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.RedTextTertiary
import com.inspiredandroid.red.ui.foundation.EmptyState
import com.inspiredandroid.red.ui.handCursor
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape

@Composable
fun ChatListTab(
    onOpenChat: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val conversations = remember(state.savedConversations) {
        state.savedConversations.toImmutableList()
    }
    var showContextDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(RedBgDeep)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Chats",
                color = RedTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Button(
                onClick = { showContextDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.08f),
                    contentColor = RedTextPrimary,
                ),
                modifier = Modifier.handCursor(),
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Context",
                    tint = RedAccent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Context", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (conversations.isEmpty()) {
                EmptyState(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = RedTextSecondary,
                            modifier = Modifier.size(48.dp),
                        )
                    },
                    title = "No conversations yet",
                    subtitle = "Start a new chat to begin",
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        ConversationRow(
                            conversation = conv,
                            isActive = conv.id == state.currentConversationId,
                            onClick = {
                                state.actions.loadConversation(conv.id)
                                onOpenChat(conv.id)
                            },
                            onDelete = { state.actions.deleteConversation(conv.id) },
                            onRename = { newTitle -> state.actions.renameConversation(conv.id, newTitle) },
                        )
                    }
                }
            }
        }
    }

    // FAB
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 20.dp, bottom = 4.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        IconButton(
            onClick = {
                state.actions.startNewChat()
                onOpenChat(null)
            },
            modifier = Modifier
                .size(56.dp)
                .background(
                    Brush.linearGradient(listOf(RedAccent, Color(0xFF3E5FCB))),
                    RoundedCornerShape(16.dp),
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New chat",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }

    if (showContextDialog) {
        com.inspiredandroid.red.ui.chat.reference.ReferenceContextDialog(
            onDismissRequest = { showContextDialog = false },
        )
    }
}

@Composable
private fun ConversationRow(
    conversation: ConversationSummary,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
) {
    val bg = if (isActive) RedBgElevated else RedBgPanel
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .handCursor()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) RedAccent else RedTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val dateStr = try {
                val dt = Instant.fromEpochMilliseconds(conversation.updatedAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                "${dt.year}-${dt.monthNumber.toString().padStart(2, '0')}-${dt.dayOfMonth.toString().padStart(2, '0')}"
            } catch (_: Exception) { "" }
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = RedTextTertiary,
            )
        }

        if (conversation.isStarred) {
            Text(
                text = "★",
                color = Color(0xFFFFC107),
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 4.dp),
            )
        }

        // 3 Dots Menu Button
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(32.dp).handCursor(),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = RedTextTertiary,
                    modifier = Modifier.size(20.dp),
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(12.dp),
            ) {
                DropdownMenuItem(
                    text = { Text("Rename", color = RedTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = RedTextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        showRenameDialog = true
                    },
                    modifier = Modifier.handCursor(),
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        showDeleteDialog = true
                    },
                    modifier = Modifier.handCursor(),
                )
            }
        }
    }

    // Rename Modal Dialog
    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(conversation.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Conversation") },
            text = {
                RedOutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.trim().isNotBlank()) {
                            onRename(newTitle.trim())
                        }
                        showRenameDialog = false
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete Confirmation Modal Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Conversation") },
            text = { Text("Are you sure you want to delete this conversation? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
