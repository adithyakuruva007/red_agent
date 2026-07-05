package com.inspiredandroid.red.ui.chat.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inspiredandroid.red.ui.chat.ChatActions
import com.inspiredandroid.red.ui.handCursor
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.chat_history_content_description
import red.composeapp.generated.resources.ic_add
import red.composeapp.generated.resources.ic_history
import red.composeapp.generated.resources.ic_settings
import red.composeapp.generated.resources.ic_volume_off
import red.composeapp.generated.resources.ic_volume_up
import red.composeapp.generated.resources.new_chat_content_description
import red.composeapp.generated.resources.sandbox_content_description
import red.composeapp.generated.resources.settings_content_description
import red.composeapp.generated.resources.toggle_speech_output_content_description
import nl.marc_apps.tts.TextToSpeechInstance
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
internal fun TopBar(
    actions: ChatActions,
    isChatHistoryEmpty: Boolean,
    hasSavedConversations: Boolean,
    onNavigateToSettings: () -> Unit,
    isSandboxAvailable: Boolean,
    isSandboxOpen: Boolean,
    isShellExecuting: Boolean,
    onToggleSandbox: () -> Unit,
    onShowHistory: () -> Unit,
    navigationTabBar: (@Composable () -> Unit)? = null,
    onToggleSidebar: () -> Unit = {},
    isSidebarExpanded: Boolean = true,
    conversationTitle: String = "",
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 16.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sidebar Toggle Menu Icon (Left)
        if (!isSidebarExpanded) {
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier
                    .handCursor()
                    .align(Alignment.Top)
                    .padding(start = 2.dp, top = 2.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Expand sidebar",
                    tint = scheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        // Sandbox/Terminal Toggle Button
        SandboxToggleButton(
            isSandboxAvailable = isSandboxAvailable,
            isSandboxOpen = isSandboxOpen,
            isShellExecuting = isShellExecuting,
            onToggleSandbox = onToggleSandbox
        )

        Spacer(Modifier.width(8.dp))


        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SandboxToggleButton(
    isSandboxAvailable: Boolean,
    isSandboxOpen: Boolean,
    isShellExecuting: Boolean,
    onToggleSandbox: () -> Unit,
) {
    if (!isSandboxAvailable) return
    val flashAlpha = remember { Animatable(0f) }
    LaunchedEffect(isShellExecuting) {
        if (isShellExecuting) {
            flashAlpha.snapTo(0.4f)
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            )
        }
    }
    val primary = MaterialTheme.colorScheme.primary
    val checkedContainer = primary.copy(alpha = 0.2f)
    val flashContainer = primary.copy(alpha = flashAlpha.value)
    IconToggleButton(
        checked = isSandboxOpen,
        onCheckedChange = { onToggleSandbox() },
        modifier = Modifier.size(32.dp).handCursor(),
        colors = IconButtonDefaults.iconToggleButtonColors(
            containerColor = flashContainer,
            checkedContainerColor = if (flashAlpha.value > 0f) flashContainer else checkedContainer,
            checkedContentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(
            imageVector = Icons.Filled.Dns,
            contentDescription = stringResource(Res.string.sandbox_content_description),
            tint = if (isSandboxOpen) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            modifier = Modifier.size(20.dp),
        )
    }
}
