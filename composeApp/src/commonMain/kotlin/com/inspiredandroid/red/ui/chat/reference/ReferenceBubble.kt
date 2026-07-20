package com.inspiredandroid.red.ui.chat.reference

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.handCursor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReferenceUserBubble(
    content: String,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp, 18.dp, 5.dp, 18.dp))
                .background(
                    Brush.linearGradient(listOf(RedAccent, Color(0xFF4A78D9))),
                )
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(content))
                        onCopy?.invoke()
                    },
                )
                .handCursor()
                .padding(horizontal = 13.dp, vertical = 10.dp),
        ) {
            SelectionContainer {
                Text(
                    text = content,
                    color = Color.White,
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReferenceAgentBubble(
    content: String,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    val document = androidx.compose.runtime.remember(content) { com.inspiredandroid.red.ui.markdown.parseMarkdown(content) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp))
                .background(RedBgPanel)
                .border(1.dp, RedBorderHairline, RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(content))
                        onCopy?.invoke()
                    },
                )
                .handCursor()
                .padding(horizontal = 13.dp, vertical = 10.dp),
        ) {
            SelectionContainer {
                com.inspiredandroid.red.ui.markdown.MarkdownContent(document = document)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReferenceAgentBubbleWithAvatar(
    content: String,
    avatarLabel: String,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF8B6CF2), Color(0xFF5E3FCB))),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = avatarLabel.firstOrNull()?.uppercase() ?: "A",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp))
                .background(RedBgPanel)
                .border(1.dp, RedBorderHairline, RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(content))
                        onCopy?.invoke()
                    },
                )
                .handCursor()
                .padding(horizontal = 13.dp, vertical = 10.dp),
        ) {
            SelectionContainer {
                Text(
                    text = content,
                    color = RedTextPrimary,
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}
