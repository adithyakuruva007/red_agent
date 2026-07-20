@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.inspiredandroid.red.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.inspiredandroid.red.data.ConversationIdElement
import com.inspiredandroid.red.data.Conversation
import com.inspiredandroid.red.data.AppNotification
import com.inspiredandroid.red.data.DataRepository
import com.inspiredandroid.red.data.Service
import com.inspiredandroid.red.data.ServiceEntry
import com.inspiredandroid.red.data.TaskScheduler
import com.inspiredandroid.red.data.UiSubmission
import com.inspiredandroid.red.getBackgroundDispatcher
import com.inspiredandroid.red.network.toUiError
import com.inspiredandroid.red.ui.markdown.RedUiBlock
import com.inspiredandroid.red.ui.markdown.RedUiError
import com.inspiredandroid.red.ui.markdown.parseMarkdown
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.conversation_untitled
import red.composeapp.generated.resources.error_unsupported_file_type
import red.composeapp.generated.resources.litert_no_model_warning
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class ChatViewModel(
    private val dataRepository: DataRepository,
    private val taskScheduler: TaskScheduler,
    private val backgroundDispatcher: CoroutineContext = getBackgroundDispatcher(),
) : ViewModel() {

    private val conversationDrafts = mutableMapOf<String, TextFieldValue>()

    private fun updateInputText(value: TextFieldValue) {
        val convId = dataRepository.currentConversationId.value ?: ""
        conversationDrafts[convId] = value
        _state.update { it.copy(inputText = value) }
    }

    private val actions = ChatActions(
        ask = ::ask,
        retry = ::retry,
        toggleSpeechOutput = ::toggleSpeechOutput,
        clearHistory = ::clearHistory,
        setIsSpeaking = ::setIsSpeaking,
        addFile = ::addFile,
        removeFile = ::removeFile,
        startNewChat = ::startNewChat,
        regenerate = ::regenerate,
        cancel = ::cancel,
        selectService = ::selectService,
        loadConversation = ::loadConversation,
        deleteConversation = ::deleteConversation,
        renameConversation = ::renameConversation,
        updateConversationAvatar = ::updateConversationAvatar,
        toggleStarConversation = ::toggleStarConversation,
        clearUnreadHeartbeat = ::clearUnreadHeartbeat,
        clearSnackbar = ::clearSnackbar,
        undoDeleteConversation = ::undoDeleteConversation,
        submitUiCallback = ::submitUiCallback,
        resubmit = ::resubmit,
        enterInteractiveMode = {},
        exitInteractiveMode = {},
        goBackInteractiveMode = {},
        sendSmsDraft = ::sendSmsDraft,
        discardSmsDraft = ::discardSmsDraft,
        deleteAppNotification = { dataRepository.deleteAppNotification(it) },
        clearAllAppNotifications = { dataRepository.clearAllAppNotifications() },
        markNotificationsAsRead = { dataRepository.markNotificationsAsRead() },
        updateInputText = ::updateInputText,
    )
    private val runningJobs = mutableMapOf<String, Job>()
    private var pendingConversationDeleteJob: Job? = null
    private val _state = MutableStateFlow(
        ChatUiState(
            actions = actions,
            showPrivacyInfo = dataRepository.isUsingSharedKey(),
        ),
    )

    val navigateToNotificationsRequested = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        updateAvailableServices()

        // Keep restoreCurrentConversation off the main thread; see issue #197 (large persisted
        // tool outputs caused ANRs when JSON-decoded synchronously during VM construction).
        // ChatScreen gates the interactive-mode branch on !isRestoring to avoid a flash.
        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.loadConversations()
            dataRepository.restoreCurrentConversation()
            _state.update { it.copy(isRestoring = false) }
        }

        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.connectEnabledMcpServers()
        }
        viewModelScope.launch {
            dataRepository.fallbackStatus.collect { status ->
                _state.update { it.copy(fallbackStatus = status) }
            }
        }
        taskScheduler.isLoadingCheck = { _state.value.isLoading }
        taskScheduler.start()

        viewModelScope.launch {
            dataRepository.currentConversationId.collect {
                updateAvailableServices()
            }
        }

        viewModelScope.launch {
            dataRepository.smsDrafts.collect { drafts ->
                _state.update { it.copy(smsDrafts = drafts.toImmutableList()) }
            }
        }

        viewModelScope.launch {
            dataRepository.openHeartbeatRequested
                .filter { it }
                .collect {
                    navigateToNotificationsRequested.tryEmit(Unit)
                    clearUnreadHeartbeat()
                    dataRepository.consumeOpenHeartbeatRequest()
                }
        }
    }

    val state = combine(
        combine(_state, dataRepository.chatHistory, dataRepository.savedConversations) { s, h, c -> Triple(s, h, c) },
        combine(dataRepository.currentConversationId, dataRepository.hasUnreadHeartbeat, dataRepository.appNotifications) { id, unread, notifs -> Triple(id, unread, notifs) }
    ) { t1, t2 ->
        val stateVal = t1.first
        val history = t1.second
        val conversations = t1.third
        val conversationId = t2.first
        val hasUnreadHeartbeat = t2.second
        val appNotifications = t2.third

        val summaries = conversations
            .sortedByDescending { it.updatedAt }
            .map {
                val isHeartbeat = it.type == Conversation.TYPE_HEARTBEAT
                val isInteractive = it.type == Conversation.TYPE_INTERACTIVE
                ConversationSummary(
                    id = it.id,
                    title = if (isHeartbeat) "" else it.title.ifEmpty { getString(Res.string.conversation_untitled) },
                    updatedAt = it.updatedAt,
                    isHeartbeat = isHeartbeat,
                    isInteractive = isInteractive,
                    isStarred = it.isStarred,
                    avatarPath = it.avatarPath,
                )
            }
        stateVal.copy(
            history = history.toImmutableList(),
            supportedFileExtensions = dataRepository.supportedFileExtensions().toImmutableList(),
            savedConversations = summaries.toImmutableList(),
            currentConversationId = conversationId,
            hasUnreadHeartbeat = hasUnreadHeartbeat,
            appNotifications = appNotifications.toImmutableList(),
            installedSkills = dataRepository.getInstalledSkills().toImmutableList(),
        )
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _state.value,
    )

    private fun submitUiCallback(event: String, data: Map<String, String>) {
        val message = if (data.isNotEmpty()) {
            val formattedData = data.entries.joinToString(", ") { "${it.key}: ${it.value}" }
            "Responded with: $formattedData"
        } else {
            "Pressed: $event"
        }
        val lastAssistant = dataRepository.chatHistory.value.lastRenderedAssistant()
        val submission = lastAssistant?.let {
            UiSubmission(sourceContent = it.content, values = data, pressedEvent = event)
        }
        askInternal(message, submission)
    }

    private fun ask(question: String?) {
        askInternal(question, null)
    }

    private fun askInternal(question: String?, uiSubmission: UiSubmission?) {
        val conversationId = dataRepository.currentConversationId.value
            ?: Uuid.random().toString().also { dataRepository.loadConversation(it) }
        // Prevent concurrent requests for the same conversation
        if (runningJobs[conversationId]?.isActive == true) return

        if (question != null) {
            val convId = dataRepository.currentConversationId.value ?: ""
            conversationDrafts.remove(convId)
            conversationDrafts.remove("")
            _state.update { it.copy(inputText = TextFieldValue("")) }
        }

        // Capture files before launching coroutine to avoid race with files being cleared
        val files = _state.value.files

        val (strippedQuestion, activeSkillId) = parseSkillInvocation(question)

        val job = viewModelScope.launch(backgroundDispatcher + ConversationIdElement(conversationId)) {
            _state.update {
                val activeId = dataRepository.currentConversationId.value
                val isActive = activeId == null || activeId == conversationId
                it.copy(
                    isLoading = isActive,
                    error = if (isActive) null else it.error,
                    files = persistentListOf(),
                )
            }
            try {
                dataRepository.ask(strippedQuestion, files, uiSubmission, activeSkillId)

                // Auto-retry in interactive mode if the response has no valid red-ui
                val activeId = dataRepository.currentConversationId.value
                val isActive = activeId == null || activeId == conversationId
                if (_state.value.isInteractiveMode && isActive) {
                    retryIfNoValidRedUi()
                }

                // If finished and we switched conversations in the meantime, post notification
                if (activeId != null && activeId != conversationId) {
                    val conversationTitle = dataRepository.savedConversations.value.find { it.id == conversationId }?.title ?: "Chat"
                    dataRepository.addAppNotification(
                        title = "Background Chat Finished",
                        content = "Chat '$conversationTitle' has finished thinking.",
                        conversationId = conversationId
                    )
                }

                if (isActive) {
                    _state.update {
                        it.copy(isLoading = false)
                    }
                }
            } catch (exception: Exception) {
                // CancellationException must be re-thrown to properly propagate coroutine cancellation
                if (exception is CancellationException) throw exception

                val activeId = dataRepository.currentConversationId.value
                val isActive = activeId == null || activeId == conversationId
                if (isActive) {
                    _state.update {
                        it.copy(
                            error = exception.toUiError(),
                            isLoading = false,
                        )
                    }
                }
            } finally {
                runningJobs.remove(conversationId)
            }
        }
        runningJobs[conversationId] = job
    }

    private suspend fun retryIfNoValidRedUi(maxRetries: Int = 2) {
        repeat(maxRetries) {
            currentCoroutineContext().ensureActive()
            val lastAssistant = dataRepository.chatHistory.value.lastRenderedAssistant() ?: return

            val blocks = parseMarkdown(lastAssistant.content).blocks
            val hasValidUi = blocks.any { it is RedUiBlock }
            if (hasValidUi) return

            // Build error feedback for the AI
            val errorBlock = blocks.filterIsInstance<RedUiError>().firstOrNull()
            val errorDetail = if (errorBlock != null) {
                "JSON parse error in: ${errorBlock.rawJson.take(200)}"
            } else {
                "No red-ui code fence found in your response."
            }
            val retryMessage = "[SYSTEM] Your previous response failed to render as interactive UI. $errorDetail " +
                "Remember: respond with ONLY a single ```red-ui code fence containing valid JSON. No text outside the fence."

            dataRepository.ask(retryMessage, emptyList())
        }
    }

    private fun clearHistory() {
        dataRepository.clearHistory()
        _state.update {
            it.copy(error = null)
        }
    }

    /**
     * If [text] begins with `/<skill-id>`, look up the skill among the currently-
     * installed-and-enabled skills and return its id alongside the verbatim user
     * text. The text is sent unchanged so the conversation visibly reflects what
     * the user typed; the skill's instructions in the system prompt tell the model
     * how to parse the args after the slash command. Falls through with null skill
     * id when no match — slash commands are opt-in.
     */
    private fun parseSkillInvocation(text: String?): Pair<String?, String?> {
        if (text == null) return null to null
        val trimmed = text.trimStart()
        if (!trimmed.startsWith('/')) return text to null
        val firstSpace = trimmed.indexOfFirst { it.isWhitespace() }
        val rawId = if (firstSpace < 0) trimmed.substring(1) else trimmed.substring(1, firstSpace)
        if (rawId.isEmpty()) return text to null
        val skill = dataRepository.getInstalledSkills().firstOrNull { it.id.equals(rawId, ignoreCase = true) }
            ?: return text to null
        return text to skill.id
    }

    private fun setIsSpeaking(isSpeaking: Boolean, contentId: String) {
        _state.update {
            it.copy(
                isSpeaking = isSpeaking,
                isSpeakingContentId = if (isSpeaking) {
                    contentId
                } else {
                    it.isSpeakingContentId
                },
            )
        }
    }

    private fun addFile(file: PlatformFile) {
        val ext = file.extension.lowercase()
        val supported = dataRepository.supportedFileExtensions()
        if (ext.isEmpty() || ext !in supported) {
            _state.update {
                it.copy(snackbarMessage = Res.string.error_unsupported_file_type)
            }
            return
        }
        _state.update {
            it.copy(files = (it.files + file).toImmutableList())
        }
    }

    private fun removeFile(file: PlatformFile) {
        _state.update {
            it.copy(files = it.files.filterNot { f -> f == file }.toImmutableList())
        }
    }

    private fun clearSnackbar() {
        _state.update {
            it.copy(snackbarMessage = null)
        }
    }

    private fun retry() {
        ask(null)
    }

    private fun toggleSpeechOutput() {
        _state.update {
            it.copy(
                isSpeechOutputEnabled = !it.isSpeechOutputEnabled,
            )
        }
    }

    private fun cancel() {
        val currentConvId = state.value.currentConversationId
        if (currentConvId != null) {
            runningJobs[currentConvId]?.cancel()
            runningJobs.remove(currentConvId)
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun selectService(instanceId: String) {
        dataRepository.setFreeServicePrimary(false)
        val currentConvId = state.value.currentConversationId
        if (currentConvId != null) {
            viewModelScope.launch {
                dataRepository.setConversationServiceInstanceId(currentConvId, instanceId)
                updateAvailableServices()
            }
        } else {
            val instances = dataRepository.getConfiguredServiceInstances()
            val currentIds = instances.map { it.instanceId }
            if (instanceId in currentIds) {
                val reordered = listOf(instanceId) + currentIds.filter { it != instanceId }
                dataRepository.reorderConfiguredServices(reordered)
            }
            dataRepository.setSelectedServiceInstanceId(instanceId)
            updateAvailableServices()
        }
    }

    private fun updateAvailableServices() {
        val configuredEntries = dataRepository.getServiceEntries()
        val entries = configuredEntries.toImmutableList()

        val primaryService = entries.firstOrNull()?.let { Service.fromId(it.serviceId) }
        val warning = if (primaryService?.isOnDevice == true && dataRepository.getLocalDownloadedModels().isEmpty()) {
            Res.string.litert_no_model_warning
        } else {
            null
        }
        _state.update { it.copy(availableServices = entries, warning = warning, showPrivacyInfo = dataRepository.isUsingSharedKey()) }
    }

    companion object {
    }

    private fun regenerate() {
        dataRepository.regenerate()
        ask(null)
    }

    private fun loadConversation(id: String) {
        val conversation = dataRepository.savedConversations.value.find { it.id == id }
        val isInteractive = conversation?.type == Conversation.TYPE_INTERACTIVE
        dataRepository.loadConversation(id)
        val draft = conversationDrafts[id] ?: TextFieldValue("")
        _state.update {
            it.copy(
                error = null,
                isInteractiveMode = isInteractive,
                isLoading = runningJobs[id]?.isActive == true,
                inputText = draft
            )
        }
        updateAvailableServices()
    }

    private fun deleteConversation(id: String) {
        commitPendingConversationDeletion()
        _state.update { it.copy(pendingConversationDeletion = id) }
        pendingConversationDeleteJob = viewModelScope.launch(backgroundDispatcher) {
            delay(4.seconds)
            dataRepository.deleteConversation(id)
            _state.update { it.copy(pendingConversationDeletion = null) }
        }
    }

    private fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.renameConversation(id, newTitle)
        }
    }

    private fun updateConversationAvatar(id: String, avatarPath: String?) {
        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.updateConversationAvatar(id, avatarPath)
        }
    }

    private fun toggleStarConversation(id: String) {
        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.toggleStarConversation(id)
        }
    }

    private fun undoDeleteConversation() {
        pendingConversationDeleteJob?.cancel()
        pendingConversationDeleteJob = null
        _state.update { it.copy(pendingConversationDeletion = null) }
    }

    private fun commitPendingConversationDeletion() {
        pendingConversationDeleteJob?.cancel()
        pendingConversationDeleteJob = null
        val pendingId = _state.value.pendingConversationDeletion ?: return
        _state.update { it.copy(pendingConversationDeletion = null) }
        runningJobs[pendingId]?.cancel()
        runningJobs.remove(pendingId)
        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.deleteConversation(pendingId)
        }
    }

    override fun onCleared() {
        commitPendingConversationDeletion()
        // The scheduler lives longer than this ViewModel (it's a singleton driving the
        // Android foreground service). Reset the predicate so the daemon path keeps
        // running without a stale reference to a dead state flow. The foreground-visible
        // signal (`appInForeground`) is tracked separately via `ProcessLifecycleOwner`
        // on Android — ViewModel lifecycle is too narrow (survives backgrounding).
        taskScheduler.isLoadingCheck = { false }
        super.onCleared()
    }

    private fun clearUnreadHeartbeat() {
        dataRepository.clearUnreadHeartbeat()
    }

    private fun sendSmsDraft(draftId: String) {
        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.sendSmsDraft(draftId)
        }
    }

    private fun discardSmsDraft(draftId: String) {
        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.discardSmsDraft(draftId)
        }
    }

    private fun startNewChat() {
        dataRepository.setSelectedServiceInstanceId(null)
        dataRepository.startNewChat()
        val draft = conversationDrafts[""] ?: TextFieldValue("")
        _state.update {
            it.copy(error = null, isInteractiveMode = false, isLoading = false, inputText = draft)
        }
        updateAvailableServices()
    }

    private fun resubmit(messageId: String, event: String, data: Map<String, String>) {
        if (_state.value.isLoading) return
        dataRepository.truncateFrom(messageId)
        submitUiCallback(event, data)
    }

    fun refreshSettings() {
        updateAvailableServices()
        viewModelScope.launch(backgroundDispatcher) {
            dataRepository.restoreCurrentConversation()
        }
    }
}
