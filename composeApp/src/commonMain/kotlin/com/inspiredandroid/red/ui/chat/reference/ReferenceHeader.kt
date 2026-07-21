package com.inspiredandroid.red.ui.chat.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgDeep
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.foundation.ThinkingDots
import com.inspiredandroid.red.ui.handCursor

@Composable
fun ReferenceHeader(
    title: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRename: (String) -> Unit = {},
    onOpenContext: () -> Unit = {},
    customAvatarLabel: String? = null,
    onUpdateAvatar: (String) -> Unit = {},
    onPickAvatarFile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var renameText by remember(title) { mutableStateOf(title) }
    var avatarText by remember(customAvatarLabel) { mutableStateOf(customAvatarLabel ?: "") }

    val displayAvatarLabel = (customAvatarLabel?.takeIf { it.isNotBlank() }
        ?: title.firstOrNull()?.toString()
        ?: "R").uppercase()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RedBgDeep)
            .statusBarsPadding()
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(36.dp).handCursor(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = RedTextPrimary,
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.ifEmpty { "Agent" },
                color = RedTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }

        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "Thinking",
                    color = RedAccent,
                    fontSize = 12.sp,
                )
                ThinkingDots(dotSize = 3.dp, color = RedAccent)
            }
            Spacer(Modifier.width(4.dp))
        }

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(34.dp).handCursor(),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = RedTextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                shape = RoundedCornerShape(12.dp),
            ) {
                DropdownMenuItem(
                    text = { Text("Rename Chat", color = RedTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename Chat",
                            tint = RedTextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = {
                        showMenu = false
                        renameText = title
                        showRenameDialog = true
                    },
                    modifier = Modifier.handCursor(),
                )
                DropdownMenuItem(
                    text = { Text("Context Settings", color = RedTextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Context Settings",
                            tint = RedTextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    onClick = {
                        showMenu = false
                        onOpenContext()
                    },
                    modifier = Modifier.handCursor(),
                )
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Chat", color = RedTextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Chat Title") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRenameDialog = false
                        if (renameText.isNotBlank()) {
                            onRename(renameText.trim())
                        }
                    },
                    modifier = Modifier.handCursor(),
                ) {
                    Text("Save", color = RedAccent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRenameDialog = false },
                    modifier = Modifier.handCursor(),
                ) {
                    Text("Cancel", color = RedTextSecondary)
                }
            },
        )
    }

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Custom Profile Pic / Avatar", color = RedTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select a photo from your device or enter custom initials/label:", color = RedTextSecondary, fontSize = 13.sp)
                    
                    androidx.compose.material3.Button(
                        onClick = {
                            showAvatarDialog = false
                            onPickAvatarFile()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = RedAccent,
                            contentColor = Color.White,
                        ),
                        modifier = Modifier.fillMaxWidth().handCursor(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Choose Picture",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Choose Picture File", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Text("Or enter text label:", color = RedTextSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = avatarText,
                        onValueChange = { avatarText = it },
                        singleLine = true,
                        label = { Text("Avatar Label / Path") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAvatarDialog = false
                        onUpdateAvatar(avatarText.trim())
                    },
                    modifier = Modifier.handCursor(),
                ) {
                    Text("Save", color = RedAccent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAvatarDialog = false },
                    modifier = Modifier.handCursor(),
                ) {
                    Text("Cancel", color = RedTextSecondary)
                }
            },
        )
    }
}
