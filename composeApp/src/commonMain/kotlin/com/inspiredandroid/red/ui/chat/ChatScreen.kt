@file:OptIn(
    ExperimentalFoundationApi::class,
)

package com.inspiredandroid.red.ui.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.BackIcon
import com.inspiredandroid.red.TerminalLine
import com.inspiredandroid.red.data.Service
import com.inspiredandroid.red.data.supportsAgenticFlows
import com.inspiredandroid.red.getBackgroundDispatcher
import com.inspiredandroid.red.onDragAndDropEventDropped
import com.inspiredandroid.red.ui.chat.composables.BotMessage
import com.inspiredandroid.red.ui.chat.composables.ChatHistorySheet
import com.inspiredandroid.red.ui.chat.composables.CircleIconButton
import com.inspiredandroid.red.ui.chat.composables.EmptyState
import com.inspiredandroid.red.ui.chat.composables.ErrorMessage
import com.inspiredandroid.red.ui.chat.composables.HeartbeatBanner
import com.inspiredandroid.red.ui.chat.composables.PendingSmsBanners
import com.inspiredandroid.red.ui.chat.composables.QuestionInput
import com.inspiredandroid.red.ui.chat.composables.ServiceSelector
import com.inspiredandroid.red.ui.chat.composables.TopBar
import com.inspiredandroid.red.ui.chat.composables.TrailingIcon
import com.inspiredandroid.red.ui.chat.composables.UserMessage
import com.inspiredandroid.red.ui.chat.composables.WaitingResponseRow
import com.inspiredandroid.red.ui.chat.composables.uiErrorText
import com.inspiredandroid.red.ui.components.LogoAnimation
import com.inspiredandroid.red.ui.components.VerticalScrollbarForList
import com.inspiredandroid.red.ui.dynamicui.FrozenSubmission
import com.inspiredandroid.red.ui.dynamicui.RedUiRenderer
import com.inspiredandroid.red.ui.dynamicui.toSpeakableText
import com.inspiredandroid.red.ui.handCursor

import com.inspiredandroid.red.ui.markdown.RedUiBlock
import com.inspiredandroid.red.ui.markdown.parseMarkdown
import com.inspiredandroid.red.ui.sandbox.SandboxTabsContent
import com.inspiredandroid.red.ui.settings.SandboxUiState
import com.inspiredandroid.red.ui.settings.SandboxViewModel
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.fallback_answered_by
import red.composeapp.generated.resources.fallback_service_failed
import red.composeapp.generated.resources.fallback_trying_next
import red.composeapp.generated.resources.ic_stop
import red.composeapp.generated.resources.interactive_back_content_description
import red.composeapp.generated.resources.interactive_exit_content_description
import red.composeapp.generated.resources.interactive_title
import red.composeapp.generated.resources.interactive_ui_parsing_failed
import red.composeapp.generated.resources.interactive_welcome_subtitle
import red.composeapp.generated.resources.interactive_welcome_title
import red.composeapp.generated.resources.scroll_to_bottom_content_description
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeechInstance
import nl.marc_apps.tts.errors.TextToSpeechSynthesisInterruptedError
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    textToSpeech: TextToSpeechInstance?,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    isSandboxAvailable: Boolean = false,
    navigationTabBar: (@Composable () -> Unit)? = null,
    onToggleSidebar: () -> Unit = {},
    isSidebarExpanded: Boolean = true,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    ChatScreenContent(
        uiState = uiState,
        textToSpeech = textToSpeech,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToNotifications = onNavigateToNotifications,
        isSandboxAvailable = isSandboxAvailable,
        navigationTabBar = navigationTabBar,
        onToggleSidebar = onToggleSidebar,
        isSidebarExpanded = isSidebarExpanded,
    )
}

@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    textToSpeech: TextToSpeechInstance? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    isSandboxAvailable: Boolean = false,
    navigationTabBar: (@Composable () -> Unit)? = null,
    initialSandboxOpen: Boolean = false,
    previewSandboxState: SandboxUiState? = null,
    previewSandboxLines: ImmutableList<TerminalLine> = persistentListOf(),
    onToggleSidebar: () -> Unit = {},
    isSidebarExpanded: Boolean = true,
) {
    ChatModeScreen(
        uiState = uiState,
        textToSpeech = textToSpeech,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToNotifications = onNavigateToNotifications,
        isSandboxAvailable = isSandboxAvailable,
        navigationTabBar = navigationTabBar,
        initialSandboxOpen = initialSandboxOpen,
        previewSandboxState = previewSandboxState,
        previewSandboxLines = previewSandboxLines,
        onToggleSidebar = onToggleSidebar,
        isSidebarExpanded = isSidebarExpanded,
    )
}

// --- Regular Chat Mode ---

@Composable
private fun ChatModeScreen(
    uiState: ChatUiState,
    textToSpeech: TextToSpeechInstance?,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    isSandboxAvailable: Boolean,
    navigationTabBar: (@Composable () -> Unit)?,
    initialSandboxOpen: Boolean = false,
    previewSandboxState: SandboxUiState? = null,
    previewSandboxLines: ImmutableList<TerminalLine> = persistentListOf(),
    onToggleSidebar: () -> Unit = {},
    isSidebarExpanded: Boolean = true,
) {
    var showHistorySheet by remember { mutableStateOf(false) }
    var isSandboxOpen by rememberSaveable { mutableStateOf(initialSandboxOpen) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    // When the active conversation changes (e.g. user starts a new chat from the
    // top bar or taps the heartbeat banner), collapse the sandbox view so the
    // user lands on the chat they just opened. Tracking the previous id avoids
    // firing on the initial composition — important when returning from Settings,
    // where rememberSaveable has just restored isSandboxOpen.
    var lastConversationId by remember { mutableStateOf(uiState.currentConversationId) }
    LaunchedEffect(uiState.currentConversationId) {
        if (lastConversationId != uiState.currentConversationId) {
            if (isSandboxOpen) isSandboxOpen = false
            lastConversationId = uiState.currentConversationId
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val resource = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(getString(resource))
        uiState.actions.clearSnackbar()
    }

    val filteredConversations = remember(uiState.savedConversations, uiState.pendingConversationDeletion) {
        val pendingId = uiState.pendingConversationDeletion
        if (pendingId != null) uiState.savedConversations.filter { it.id != pendingId }.toImmutableList() else uiState.savedConversations
    }

    val historyState = rememberUpdatedState(uiState.history)
    val isShellExecuting by remember {
        derivedStateOf {
            historyState.value.any { it.role == History.Role.TOOL_EXECUTING && it.content == "execute_shell_command" }
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).navigationBarsPadding().statusBarsPadding().imePadding()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                actions = uiState.actions,
                isChatHistoryEmpty = uiState.history.isEmpty(),
                hasSavedConversations = filteredConversations.any { it.id != uiState.currentConversationId },
                onNavigateToSettings = onNavigateToSettings,
                isSandboxAvailable = isSandboxAvailable,
                isSandboxOpen = isSandboxOpen,
                isShellExecuting = isShellExecuting,
                onToggleSandbox = { isSandboxOpen = !isSandboxOpen },
                onShowHistory = {
                    keyboardController?.hide()
                    showHistorySheet = true
                },
                navigationTabBar = navigationTabBar,
                onToggleSidebar = onToggleSidebar,
                isSidebarExpanded = isSidebarExpanded,
                conversationTitle = uiState.savedConversations.find { it.id == uiState.currentConversationId }?.title ?: "",
            )

            HeartbeatBanner(
                visible = uiState.hasUnreadHeartbeat,
                onTap = {
                    uiState.actions.clearUnreadHeartbeat()
                    onNavigateToNotifications()
                    isSandboxOpen = false
                },
                onDismiss = {
                    uiState.actions.clearUnreadHeartbeat()
                },
            )

            PendingSmsBanners(
                drafts = uiState.smsDrafts,
                onSend = uiState.actions.sendSmsDraft,
                onDiscard = uiState.actions.discardSmsDraft,
            )

            uiState.warning?.let { warning ->
                Text(
                    text = stringResource(warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (isSandboxOpen) {
                val isPreview = LocalInspectionMode.current
                val sandboxViewModel = if (!isPreview) koinViewModel<SandboxViewModel>() else null
                val liveState = sandboxViewModel?.state?.collectAsStateWithLifecycle()?.value
                val sandboxState = liveState ?: previewSandboxState ?: SandboxUiState()
                SandboxTabsContent(
                    sandboxState = sandboxState,
                    onSetupSandbox = sandboxViewModel?.let { { it.onSetupSandbox() } } ?: {},
                    onCancelSandbox = sandboxViewModel?.let { { it.onCancelSandbox() } } ?: {},
                    previewLines = previewSandboxLines,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            } else {
                Box(Modifier.weight(1f)) {
                    var isDropping by remember {
                        mutableStateOf(false)
                    }
                    val addFile by rememberUpdatedState(uiState.actions.addFile)
                    val canAcceptDrop by rememberUpdatedState(uiState.supportedFileExtensions.isNotEmpty())
                    val shouldStartDragAndDrop = remember { { _: DragAndDropEvent -> canAcceptDrop } }
                    val dropTarget = remember {
                        object : DragAndDropTarget {
                            override fun onEntered(event: DragAndDropEvent) {
                                super.onEntered(event)
                                isDropping = true
                            }
                            override fun onExited(event: DragAndDropEvent) {
                                super.onExited(event)
                                isDropping = false
                            }
                            override fun onDrop(event: DragAndDropEvent): Boolean {
                                val file = onDragAndDropEventDropped(event)
                                if (file != null) addFile(file)
                                isDropping = false
                                return file != null
                            }
                        }
                    }
                    Column(
                        Modifier
                            .fillMaxSize()
                            .blur(radius = if (isDropping) 4.dp else 0.dp)
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = shouldStartDragAndDrop,
                                target = dropTarget,
                            ),
                    ) {
                        if (uiState.history.isEmpty()) {
                            // Interactive UI mode isn't offered on on-device LiteRT: the red-ui
                            // component schema is too large for small Gemma models to coherently
                            // attend to, and even the minimal variant we tried was unreliable.
                            val primaryIsOnDevice = uiState.availableServices
                                .firstOrNull()
                                ?.let { Service.fromId(it.serviceId).isOnDevice } == true
                            EmptyState(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                isUsingSharedKey = uiState.showPrivacyInfo,
                            )
                        } else {
                            val listState = rememberLazyListState()
                            val componentScope = rememberCoroutineScope()

                            LaunchedEffect(uiState.history.size) {
                                // Capture history at effect start to prevent race conditions
                                val history = uiState.history
                                if (history.isNotEmpty()) {
                                    listState.scrollToItem(history.lastIndex)
                                    val lastMessage = history.last()
                                    if (uiState.isSpeechOutputEnabled && lastMessage.role == History.Role.ASSISTANT) {
                                        componentScope.launch(getBackgroundDispatcher()) {
                                            textToSpeech?.stop()
                                            uiState.actions.setIsSpeaking(true, lastMessage.id)
                                            try {
                                                textToSpeech?.say(lastMessage.content.toSpeakableText())
                                            } catch (_: TextToSpeechSynthesisInterruptedError) {
                                                // Speech was interrupted by user
                                            } catch (_: Exception) {
                                                // Handle TTS errors gracefully (service failure, audio issues, etc.)
                                            } finally {
                                                uiState.actions.setIsSpeaking(false, lastMessage.id)
                                            }
                                        }
                                    }
                                }
                            }

                            val lastAssistantId = remember(uiState.history) { uiState.history.lastRenderedAssistant()?.id }
                            // Pair every user submission with its originating assistant so the red-ui
                            // renders once (on the assistant side) with a frozen snapshot — never as a
                            // separate user-side card. pressedEvent + values persist across the loading
                            // transition; isPending is only set for the latest in-flight submission.
                            val pairings = remember(uiState.history, uiState.isLoading) {
                                val history = uiState.history
                                val lastUserIdx = history.indexOfLast { it.role == History.Role.USER }
                                val frozen = mutableMapOf<String, FrozenSubmission>()
                                val userIdByAssistant = mutableMapOf<String, String>()
                                for ((i, h) in history.withIndex()) {
                                    if (h.role != History.Role.USER) continue
                                    val sub = h.uiSubmission ?: continue
                                    val originId = (i - 1 downTo 0).firstNotNullOfOrNull { j ->
                                        history[j].takeIf {
                                            it.role == History.Role.ASSISTANT &&
                                                it.content.isNotEmpty() && !it.isThinking &&
                                                it.content == sub.sourceContent
                                        }?.id
                                    } ?: (i - 1 downTo 0).firstNotNullOfOrNull { j ->
                                        history[j].takeIf {
                                            it.role == History.Role.ASSISTANT &&
                                                it.content.isNotEmpty() && !it.isThinking
                                        }?.id
                                    } ?: continue
                                    frozen[originId] = FrozenSubmission(
                                        values = sub.values,
                                        pressedEvent = sub.pressedEvent,
                                        isPending = uiState.isLoading && i == lastUserIdx,
                                    )
                                    userIdByAssistant[originId] = h.id
                                }
                                frozen.toMap() to userIdByAssistant.toMap()
                            }
                            val frozenByAssistantId = pairings.first
                            val userIdByAssistantId = pairings.second
                            val executingToolsState = rememberExecutingTools(uiState.history)

                            val fallbackStatusText = uiState.fallbackStatus?.let { status ->
                                val failed = stringResource(Res.string.fallback_service_failed, status.serviceName, uiErrorText(status.errorReason))
                                val next = status.nextServiceName?.let { stringResource(Res.string.fallback_trying_next, it) }
                                if (next != null) "$failed\n$next" else failed
                            }

                            // Group every reasoning segment in a response (intermediate tool-call /
                            // thinking-only turns plus the final answer's own reasoning) under the
                            // answer-bearing assistant message, so each response shows a single
                            // collapsible "Thinking" section instead of N standalone ones.
                            val (reasoningSegmentsByAssistantId, suppressedThinkingIds) = remember(uiState.history) {
                                val byAnswerId = mutableMapOf<String, ImmutableList<String>>()
                                val suppressed = mutableSetOf<String>()
                                val pending = mutableListOf<String>()
                                val pendingThinkingIds = mutableListOf<String>()
                                for (entry in uiState.history) {
                                    when {
                                        entry.role == History.Role.USER -> {
                                            pending.clear()
                                            pendingThinkingIds.clear()
                                        }

                                        entry.role == History.Role.ASSISTANT &&
                                            entry.isThinking &&
                                            entry.content.isNotEmpty() -> {
                                            pending.add(entry.content)
                                            pendingThinkingIds.add(entry.id)
                                        }

                                        entry.role == History.Role.ASSISTANT &&
                                            !entry.isThinking &&
                                            entry.content.isNotEmpty() -> {
                                            val combined = buildList {
                                                addAll(pending)
                                                entry.reasoningContent?.takeIf { it.isNotBlank() }?.let { add(it) }
                                            }
                                            if (combined.isNotEmpty()) byAnswerId[entry.id] = combined.toImmutableList()
                                            suppressed.addAll(pendingThinkingIds)
                                            pending.clear()
                                            pendingThinkingIds.clear()
                                        }

                                        entry.role == History.Role.ASSISTANT &&
                                            entry.toolCalls != null -> {
                                            // Assistant turn with tool calls but no answer text yet —
                                            // capture its reasoning, attach to the eventual answer.
                                            entry.reasoningContent
                                                ?.takeIf { it.isNotBlank() }
                                                ?.let { pending.add(it) }
                                        }
                                    }
                                }
                                // In-flight: the user is still waiting for the answer but earlier
                                // thinking turns are already in history. Collapse them into the most
                                // recent thinking entry so the user sees ONE growing Thinking section
                                // instead of a separate bubble per tool-loop iteration.
                                if (pendingThinkingIds.isNotEmpty()) {
                                    val lastId = pendingThinkingIds.last()
                                    byAnswerId[lastId] = pending.toImmutableList()
                                    for (i in 0 until pendingThinkingIds.size - 1) {
                                        suppressed.add(pendingThinkingIds[i])
                                    }
                                }
                                byAnswerId to suppressed
                            }

                            val showScrollToBottom by remember {
                                derivedStateOf {
                                    val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                                    lastVisibleItem != null && lastVisibleItem.index < listState.layoutInfo.totalItemsCount - 1
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = listState,
                                    horizontalAlignment = CenterHorizontally,
                                ) {
                                    items(uiState.history, key = { it.id }, contentType = { it.role }) { history ->
                                        when (history.role) {
                                            History.Role.USER -> {
                                                // Submissions are shown by the paired assistant's frozen red-ui card
                                                // above; the "Responded with: …" text bubble would be redundant.
                                                if (history.uiSubmission == null) {
                                                    UserMessage(
                                                        message = history.content,
                                                        attachments = history.attachments,
                                                    )
                                                }
                                            }

                                            History.Role.ASSISTANT -> {
                                                if (history.content.isNotEmpty() && !history.isThinking) {
                                                    val isLastAssistant = history.id == lastAssistantId
                                                    val frozen = frozenByAssistantId[history.id]
                                                    val pairedUserId = userIdByAssistantId[history.id]
                                                    BotMessage(
                                                        message = history.content,
                                                        textToSpeech = textToSpeech,
                                                        isSpeaking = uiState.isSpeaking && uiState.isSpeakingContentId == history.id,
                                                        setIsSpeaking = {
                                                            uiState.actions.setIsSpeaking(it, history.id)
                                                        },
                                                        onRegenerate = if (isLastAssistant) uiState.actions.regenerate else null,
                                                        isInteractive = isLastAssistant && !uiState.isLoading && frozen == null,
                                                        onUiCallback = { event, data ->
                                                            uiState.actions.submitUiCallback(event, data)
                                                        },
                                                        frozen = frozen,
                                                        onResubmit = if (pairedUserId != null && !uiState.isLoading) {
                                                            { event, data -> uiState.actions.resubmit(pairedUserId, event, data) }
                                                        } else {
                                                            null
                                                        },
                                                        reasoningSegments = reasoningSegmentsByAssistantId[history.id] ?: persistentListOf(),
                                                    )
                                                    if (history.fallbackServiceName != null) {
                                                        androidx.compose.material3.Text(
                                                            text = stringResource(Res.string.fallback_answered_by, history.fallbackServiceName),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                                                        )
                                                    }
                                                } else if (history.isThinking &&
                                                    history.content.isNotEmpty() &&
                                                    history.id !in suppressedThinkingIds
                                                ) {
                                                    // Thinking-only turn still in flight — render as a standalone
                                                    // reasoning bubble. The precomputation above has already gathered
                                                    // every earlier thinking segment in this cycle under this id.
                                                    BotMessage(
                                                        message = "",
                                                        textToSpeech = null,
                                                        isSpeaking = false,
                                                        setIsSpeaking = {},
                                                        reasoningSegments = reasoningSegmentsByAssistantId[history.id]
                                                            ?: persistentListOf(history.content),
                                                    )
                                                }
                                            }

                                            History.Role.TOOL_EXECUTING -> {
                                                // Rendered in WaitingResponseRow below
                                            }

                                            History.Role.TOOL -> {
                                                // Don't show completed tool results in UI
                                            }
                                        }
                                    }
                                    // Skip the generic "thinking" row during a pending red-ui submission — the
                                    // pressed button's pulse already signals work in flight. Keep it for tool
                                    // activity so tool feedback isn't lost.
                                    val showWaitingRow = uiState.isLoading &&
                                        (frozenByAssistantId.values.none { it.isPending } || executingToolsState.tools.isNotEmpty())
                                    if (showWaitingRow) {
                                        item(key = "loading") {
                                            WaitingResponseRow(
                                                executingTools = executingToolsState.tools,
                                                isStatusOnly = executingToolsState.isStatusOnly,
                                                statusText = fallbackStatusText,
                                            )
                                        }
                                    }
                                    uiState.error?.let { error ->
                                        item(key = "error") {
                                            ErrorMessage(error = error, retry = uiState.actions.retry)
                                        }
                                    }
                                }

                                VerticalScrollbarForList(
                                    listState = listState,
                                    modifier = Modifier.align(CenterEnd).fillMaxHeight(),
                                )

                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showScrollToBottom,
                                    modifier = Modifier.align(BottomCenter).padding(bottom = 8.dp),
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut(),
                                ) {
                                    SmallFloatingActionButton(
                                        modifier = Modifier
                                            .handCursor(),
                                        onClick = {
                                            componentScope.launch {
                                                val totalItems = listState.layoutInfo.totalItemsCount
                                                if (totalItems > 0) {
                                                    listState.animateScrollToItem(totalItems - 1)
                                                }
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(Res.string.scroll_to_bottom_content_description))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isSandboxOpen) {
                QuestionInput(
                    files = uiState.files,
                    addFile = uiState.actions.addFile,
                    removeFile = uiState.actions.removeFile,
                    ask = uiState.actions.ask,
                    supportedFileExtensions = uiState.supportedFileExtensions,
                    textState = uiState.inputText,
                    onTextStateChange = uiState.actions.updateInputText,
                    isLoading = uiState.isLoading,
                    cancel = uiState.actions.cancel,
                    availableServices = uiState.availableServices,
                    onSelectService = uiState.actions.selectService,
                    installedSkills = uiState.installedSkills,
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(BottomCenter).padding(bottom = 80.dp),
        ) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showHistorySheet) {
        ChatHistorySheet(
            conversations = filteredConversations,
            currentConversationId = uiState.currentConversationId,
            pendingConversationDeletion = uiState.pendingConversationDeletion,
            actions = uiState.actions,
            onDismiss = { showHistorySheet = false },
            onConversationSelected = { isSandboxOpen = false },
        )
    }
}

private data class ExecutingToolsState(
    val tools: ImmutableList<Pair<String, String>>,
    val isStatusOnly: Boolean,
)

@Composable
private fun rememberExecutingTools(history: ImmutableList<History>): ExecutingToolsState {
    // Wrap the history parameter in State so derivedStateOf can observe it, then
    // only recompute (and only emit) when the executing-tools subset actually changes.
    // Streaming tokens mutate `history` on every frame but rarely change this derived slice.
    val historyState = rememberUpdatedState(history)
    val state by remember {
        derivedStateOf {
            val executing = historyState.value.filter { it.role == History.Role.TOOL_EXECUTING }
            ExecutingToolsState(
                tools = executing.map { it.id to (it.toolName ?: "tool") }.toImmutableList(),
                isStatusOnly = executing.any { it.isStatusMessage },
            )
        }
    }
    return state
}
