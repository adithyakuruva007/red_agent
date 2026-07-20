package com.inspiredandroid.red.ui.chat.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgDeep
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.RedTextTertiary
import com.inspiredandroid.red.ui.handCursor

@Composable
fun ReferenceComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit = {},
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var showAttachMenu by remember { mutableStateOf(false) }

    val handleSend = {
        if (text.trim().isNotBlank() && !isLoading) {
            onSend()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(RedBgDeep)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        // Soft Rectangular Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(RedBgPanel)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            RedAccent.copy(alpha = 0.4f),
                            Color(0xFF6B4CFF).copy(alpha = 0.25f),
                            RedBorderHairline,
                        ),
                    ),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Plus Button (+) with Attachment Options Menu
            Box {
                IconButton(
                    onClick = { showAttachMenu = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .handCursor(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add attachment",
                        tint = RedTextSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                }

                DropdownMenu(
                    expanded = showAttachMenu,
                    onDismissRequest = { showAttachMenu = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    DropdownMenuItem(
                        text = { Text("Files", color = RedTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = "Files",
                                tint = RedTextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = { showAttachMenu = false },
                        modifier = Modifier.handCursor(),
                    )
                    DropdownMenuItem(
                        text = { Text("Camera", color = RedTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = RedTextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = { showAttachMenu = false },
                        modifier = Modifier.handCursor(),
                    )
                    DropdownMenuItem(
                        text = { Text("Photos", color = RedTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photos",
                                tint = RedTextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = { showAttachMenu = false },
                        modifier = Modifier.handCursor(),
                    )
                }
            }

            // Text Input Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "Message Agent…",
                        color = RedTextTertiary,
                        fontSize = 14.5.sp,
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(
                        color = RedTextPrimary,
                        fontSize = 14.5.sp,
                    ),
                    cursorBrush = SolidColor(RedAccent),
                    singleLine = false,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { handleSend() },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPreviewKeyEvent { event ->
                            if (event.key.keyCode == Key.Enter.keyCode && event.type == KeyEventType.KeyDown) {
                                if (event.isShiftPressed) {
                                    false
                                } else {
                                    handleSend()
                                    true
                                }
                            } else {
                                false
                            }
                        },
                )
            }

            // Action Button (Send / Square Cancel Stop - Soft Square & Matching Color Scheme)
            IconButton(
                onClick = {
                    if (isLoading) {
                        onCancel()
                    } else {
                        handleSend()
                    }
                },
                enabled = isLoading || text.trim().isNotBlank(),
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isLoading || text.trim().isNotBlank()) {
                            Brush.linearGradient(listOf(RedAccent, Color(0xFF5E3FCB)))
                        } else {
                            SolidColor(Color.White.copy(alpha = 0.1f))
                        },
                    )
                    .handCursor(),
            ) {
                Icon(
                    imageVector = if (isLoading) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                    contentDescription = if (isLoading) "Cancel" else "Send",
                    tint = if (isLoading || text.trim().isNotBlank()) Color.White else RedTextTertiary,
                    modifier = Modifier.size(if (isLoading) 20.dp else 18.dp),
                )
            }
        }
    }
}
