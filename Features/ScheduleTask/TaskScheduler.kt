package com.inspiredandroid.red.data

import com.inspiredandroid.red.getBackgroundDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TaskScheduler(
    private val dataRepository: DataRepository,
    private val taskStore: TaskStore? = null,
    private val appSettings: AppSettings? = null,
    private val heartbeatManager: HeartbeatManager? = null,
    private val enabled: Boolean = true,
    private val backgroundDispatcher: CoroutineContext = getBackgroundDispatcher(),
) {
    private companion object {
        const val POLL_INTERVAL_MS = 60_000L
        const val MAX_BACKOFF_MS = 3_600_000L // 1 hour
        const val MAX_TASK_LOG_ENTRIES = 10
    }

    private val schedulerScope = CoroutineScope(
        SupervisorJob() + backgroundDispatcher + CoroutineName("TaskScheduler"),
    )

    private var activeJob: Job? = null

    @Volatile
    var isLoadingCheck: () -> Boolean = { false }

    fun start() {
        if (!enabled || taskStore == null || appSettings == null) return
        if (activeJob?.isActive == true) return
        activeJob = schedulerScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS.milliseconds)
                if (!appSettings.isSchedulingEnabled()) continue

                val dueTasks = taskStore.getDueTasks()
                for (task in dueTasks) {
                    if (isLoadingCheck()) break

                    try {
                        val taskConversationId = dataRepository.getOrCreateHeartbeatConversationId()
                        val response = dataRepository.askWithTools(task.prompt, conversationIdOverride = taskConversationId)
                        if (response.isNotBlank()) {
                            val header = task.description.ifBlank { "Scheduled task" }
                            dataRepository.addAssistantMessage("**$header**\n\n$response")
                        }
                        handleTaskCompletion(task)
                    } catch (e: Exception) {
                        handleTaskFailure(task, formatException(e))
                    }
                }
            }
        }
    }

    private fun formatException(e: Exception): String {
        val type = e::class.simpleName ?: "Exception"
        val msg = e.message?.takeIf { it.isNotBlank() } ?: return type
        return "$type: $msg"
    }

    private fun appendExecution(task: ScheduledTask, success: Boolean, message: String?): List<TaskExecutionLogEntry> {
        val entry = TaskExecutionLogEntry(
            timestampEpochMs = Clock.System.now().toEpochMilliseconds(),
            success = success,
            message = message,
        )
        return (listOf(entry) + task.recentExecutions).take(MAX_TASK_LOG_ENTRIES)
    }

    private suspend fun handleTaskFailure(task: ScheduledTask, error: String? = null) {
        val now = Clock.System.now()
        val failures = task.consecutiveFailures + 1
        val reason = error ?: "unknown error"
        val log = appendExecution(task, success = false, message = reason)

        if (task.cron != null) {
            val nextExecution = try {
                CronExpression(task.cron).nextAfter(now)
            } catch (_: Exception) {
                null
            }
            if (nextExecution != null) {
                taskStore!!.updateTask(
                    task.copy(
                        scheduledAtEpochMs = nextExecution.toEpochMilliseconds(),
                        lastResult = "Failed at $now: $reason (next retry at $nextExecution)",
                        consecutiveFailures = failures,
                        recentExecutions = log,
                    ),
                )
            } else {
                taskStore!!.updateTask(
                    task.copy(
                        status = TaskStatus.COMPLETED,
                        lastResult = "Failed at $now: $reason (no next schedule)",
                        consecutiveFailures = failures,
                        recentExecutions = log,
                    ),
                )
            }
        } else {
            val backoffMs = min(POLL_INTERVAL_MS * (1L shl min(failures, 10)), MAX_BACKOFF_MS)
            taskStore!!.updateTask(
                task.copy(
                    scheduledAtEpochMs = now.toEpochMilliseconds() + backoffMs,
                    lastResult = "Failed at $now: $reason (retry after ${backoffMs / 1000}s backoff)",
                    consecutiveFailures = failures,
                    recentExecutions = log,
                ),
            )
        }
    }

    private suspend fun handleTaskCompletion(task: ScheduledTask) {
        val now = Clock.System.now()
        val log = appendExecution(task, success = true, message = null)
        if (task.cron != null) {
            val nextExecution = try {
                CronExpression(task.cron).nextAfter(now)
            } catch (e: Exception) {
                println("TaskScheduler: failed to compute next cron time for task ${task.id}: ${e.message}")
                taskStore!!.updateTask(
                    task.copy(
                        status = TaskStatus.PENDING,
                        lastResult = "Executed at $now (next schedule computation failed, will retry)",
                        consecutiveFailures = 0,
                        recentExecutions = log,
                    ),
                )
                return
            }
            if (nextExecution != null) {
                taskStore!!.updateTask(
                    task.copy(
                        scheduledAtEpochMs = nextExecution.toEpochMilliseconds(),
                        lastResult = "Executed at $now",
                        status = TaskStatus.PENDING,
                        consecutiveFailures = 0,
                        recentExecutions = log,
                    ),
                )
            } else {
                taskStore!!.updateTask(
                    task.copy(
                        status = TaskStatus.COMPLETED,
                        lastResult = "Executed at $now (no next schedule)",
                        consecutiveFailures = 0,
                        recentExecutions = log,
                    ),
                )
            }
        } else {
            taskStore!!.updateTask(
                task.copy(
                    status = TaskStatus.COMPLETED,
                    lastResult = "Executed at $now",
                    consecutiveFailures = 0,
                    recentExecutions = log,
                ),
            )
        }
    }
}
