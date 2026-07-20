package com.inspiredandroid.red.ui.chat.reference

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.data.Conversation
import com.inspiredandroid.red.data.DataRepository
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.handCursor
import org.koin.compose.koinInject

@Composable
fun ReferenceContextDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dataRepository = koinInject<DataRepository>()
    val contextFiles by dataRepository.contextFiles.collectAsState()
    val referencedIds by dataRepository.referencedConversationIds.collectAsState()
    val savedConversations by dataRepository.savedConversations.collectAsState()
    val currentConversationId by dataRepository.currentConversationId.collectAsState()

    var showAddFileModal by remember { mutableStateOf(false) }
    var fileNameInput by remember { mutableStateOf("") }
    var fileContentInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = "Context Settings",
                color = RedTextPrimary,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Section 1: External Context Files & Info
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "External Context Files & Info",
                            color = RedTextPrimary,
                            fontSize = 14.sp,
                        )
                        Button(
                            shape = RoundedCornerShape(8.dp),
                            onClick = { showAddFileModal = true },
                            modifier = Modifier.handCursor(),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add File", fontSize = 12.sp)
                        }
                    }

                    if (contextFiles.isEmpty()) {
                        Text(
                            text = "No custom context files added.",
                            color = RedTextSecondary,
                            fontSize = 13.sp,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                                .border(1.dp, RedBorderHairline, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(contextFiles) { file ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = file.name,
                                        color = RedTextPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(
                                        onClick = { dataRepository.removeContextFile(file.name) },
                                        modifier = Modifier.size(24.dp).handCursor(),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove file",
                                            tint = RedAccent,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = RedBorderHairline)

                // Section 2: Referenced Chats in Context
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Referenced Chats in Context",
                        color = RedTextPrimary,
                        fontSize = 14.sp,
                    )

                    val otherConversations = remember(savedConversations, currentConversationId) {
                        savedConversations.filter { it.id != currentConversationId && it.type != "heartbeat" }
                    }

                    if (otherConversations.isEmpty()) {
                        Text(
                            text = "No other active conversations available.",
                            color = RedTextSecondary,
                            fontSize = 13.sp,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .border(1.dp, RedBorderHairline, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(otherConversations) { conv: Conversation ->
                                val isChecked = referencedIds.contains(conv.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { dataRepository.toggleReferencedConversation(conv.id) }
                                        .handCursor()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { dataRepository.toggleReferencedConversation(conv.id) },
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = conv.title.ifEmpty { "Chat ${conv.id.take(6)}" },
                                        color = RedTextPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.handCursor(),
            ) {
                Text("Close", color = RedAccent)
            }
        },
    )

    if (showAddFileModal) {
        AlertDialog(
            onDismissRequest = { showAddFileModal = false },
            title = { Text("Add Context File", color = RedTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("File Name (e.g. notes.txt)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = fileContentInput,
                        onValueChange = { fileContentInput = it },
                        label = { Text("Context Content") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (fileNameInput.isNotBlank() && fileContentInput.isNotBlank()) {
                            dataRepository.addContextFile(fileNameInput.trim(), fileContentInput)
                            fileNameInput = ""
                            fileContentInput = ""
                            showAddFileModal = false
                        }
                    },
                    modifier = Modifier.handCursor(),
                ) {
                    Text("Add", color = RedAccent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddFileModal = false },
                    modifier = Modifier.handCursor(),
                ) {
                    Text("Cancel", color = RedTextSecondary)
                }
            },
        )
    }
}
