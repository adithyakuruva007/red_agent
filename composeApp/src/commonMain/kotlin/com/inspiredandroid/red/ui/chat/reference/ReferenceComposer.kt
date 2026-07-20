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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        // Floating Bubbly Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
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
                    shape = RoundedCornerShape(26.dp),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Bubbly Attach Button
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .handCursor(),
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = RedTextSecondary,
                    modifier = Modifier.size(19.dp),
                )
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

            // Bubbly Voice Button
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .handCursor(),
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = RedTextSecondary,
                    modifier = Modifier.size(19.dp),
                )
            }

            // Action Button (Send / Square Cancel Stop)
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
                    .clip(if (isLoading) RoundedCornerShape(10.dp) else CircleShape)
                    .background(
                        when {
                            isLoading -> Brush.linearGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828)))
                            text.trim().isNotBlank() -> Brush.linearGradient(listOf(RedAccent, Color(0xFF5E3FCB)))
                            else -> SolidColor(Color.White.copy(alpha = 0.1f))
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
