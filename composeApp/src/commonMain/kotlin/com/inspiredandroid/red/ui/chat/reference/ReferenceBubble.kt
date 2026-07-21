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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
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
    reasoningContent: String? = null,
    isThinking: Boolean = false,
    onCopy: (() -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    var isThinkingExpanded by remember { mutableStateOf(false) }

    val document = remember(content) {
        if (content.isNotEmpty()) com.inspiredandroid.red.ui.markdown.parseMarkdown(content) else null
    }

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
                        val textToCopy = content.ifEmpty { reasoningContent.orEmpty() }
                        if (textToCopy.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            onCopy?.invoke()
                        }
                    },
                )
                .handCursor()
                .padding(horizontal = 13.dp, vertical = 10.dp),
            Column {
                val displayReasoning = reasoningContent.takeIf { !it.isNullOrBlank() } ?: if (isThinking) content else null

                if (displayReasoning != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isThinkingExpanded = !isThinkingExpanded }
                            .handCursor()
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(RedAccent),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isThinking && content.isEmpty()) "Thinking…" else "Thought Process",
                            color = RedTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    if (isThinkingExpanded || (isThinking && content.isEmpty())) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = displayReasoning,
                            color = RedTextSecondary.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                        )
                    }
                    if (content.isNotEmpty() && !isThinking) {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (document != null && !isThinking) {
                    SelectionContainer {
                        com.inspiredandroid.red.ui.markdown.MarkdownContent(document = document)
                    }
                }
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
