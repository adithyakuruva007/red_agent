# Red App - Core Features Extract

This directory contains the standalone core feature code extracted from the Red project. These components are designed to be easily portable to other Kotlin Multiplatform (KMP) or Android projects.

---

## Table of Contents
1. [Email Control of Red](#1-email-control-of-red)
2. [SMS Control of Red](#2-sms-control-of-red)
3. [Web Services & Navigation](#3-web-services--navigation)
4. [Memory System](#4-memory-system)
5. [OpenRouter API Integration](#5-openrouter-api-integration)
6. [Learning System & Reinforcement](#6-learning-system--reinforcement)
7. [Background Daemon Mode](#7-background-daemon-mode)
8. [Context Files & Multimodal Attachments](#8-context-files--multimodal-attachments)

---

## 1. Email Control of Red
**Files:**
- [EmailConnection.kt](file:///home/aditya/Documents/Red/Features/EmailControl/EmailConnection.kt)
- [EmailConnection.android.kt](file:///home/aditya/Documents/Red/Features/EmailControl/EmailConnection.android.kt)
- [ImapClient.kt](file:///home/aditya/Documents/Red/Features/EmailControl/ImapClient.kt)

### Technical Details & Flow:
- **`EmailConnection`**: Multiplatform TCP/TLS stream reader/writer. The Android implementation uses standard `java.net.Socket` and upgradeable TLS via `SSLSocketFactory`.
- **`ImapClient`**: A custom, low-dependency IMAP client implementing tagged commands (`A001 LOGIN`, `A002 SELECT`, etc.).
  - **MIME parsing**: Custom multipart parser splitting headers and boundaries, decoding raw base64 or quoted-printable content to text and HTML.
  - **HTML stripping**: Gracefully removes scripts, styles, and tags, decoding HTML entities to feed text content to the model.

---

## 2. SMS Control of Red
**Files:**
- [SmsReader.kt](file:///home/aditya/Documents/Red/Features/SmsControl/SmsReader.kt)
- [SmsReader.android.kt](file:///home/aditya/Documents/Red/Features/SmsControl/SmsReader.android.kt)
- [SmsSender.android.kt](file:///home/aditya/Documents/Red/Features/SmsControl/SmsSender.android.kt)

### Technical Details & Flow:
- **`SmsReader`**: Android implementation reads the system SMS content provider (`content://sms/inbox`).
  - Gated by Manifest check `Manifest.permission.READ_SMS` (only active in FOSS flavor builds).
  - Fetches inbox messages since a specific `lastSeenId`, handles text searches, and tracks the max ID.
- **`SmsSender`**: Sends SMS via Android's `SmsManager`. Handles multi-part messages (`divideMessage`) for long text payloads.

---

## 3. Web Services & Navigation
**Files:**
- [BrowserController.kt](file:///home/aditya/Documents/Red/Features/WebServicesAndNavigation/BrowserController.kt)
- [BrowserController.android.kt](file:///home/aditya/Documents/Red/Features/WebServicesAndNavigation/BrowserController.android.kt)
- [WebSearchTool.kt](file:///home/aditya/Documents/Red/Features/WebServicesAndNavigation/WebSearchTool.kt)
- [FetchUrlTool.kt](file:///home/aditya/Documents/Red/Features/WebServicesAndNavigation/FetchUrlTool.kt)

### Technical Details & Flow:
- **`BrowserController`**: Headless web browser client using Android's `WebView`.
  - Injects JS to assign sequential `data-red-id` to visible, interactive elements (buttons, inputs, links).
  - Exposes actions: click, type text, scroll, capture screenshot, and extract page structure back into DTOs.
- **`WebSearchTool`**: Custom DuckDuckGo Lite HTML parser. Emits query, parses result HTML via regex for titles, URLs, and snippets.
- **`FetchUrlTool`**: Safe URL client via Ktor. Prevents SSRF attacks by blocking loopbacks (e.g. `localhost`, `127.x.x.x`, `10.x.x.x`, etc.) and parses body text.

---

## 4. Memory System
**Files:**
- [MemoryStore.kt](file:///home/aditya/Documents/Red/Features/MemorySystem/MemoryStore.kt)

### Technical Details & Flow:
- **`MemoryStore`**: Local long-term semantic store.
  - Saves/loads records inside localized JSON strings stored in `AppSettings`.
  - Supports categories: `GENERAL`, `LEARNING`, `ERROR`, and `PREFERENCE`.
  - Manages `hitCount` incrementing (used for learning reinforcement).

---

## 5. OpenRouter API Integration
**Files:**
- [OpenRouterClient.kt](file:///home/aditya/Documents/Red/Features/OpenRouterApi/OpenRouterClient.kt)

### Technical Details & Flow:
- **`OpenRouterClient`**: Standalone API client for OpenRouter/OpenAI-compatible models.
  - Implements chat completion payloads containing messages, tool declarations, and results.
  - Maps tool execution states (`History.Role.TOOL`) to OpenAI DTO structures.
  - Provides a lightweight `validateApiKey` call to verify key credentials via `/v1/auth/key`.

---

## 6. Learning System & Reinforcement
**Files:**
- [LearningTools.kt](file:///home/aditya/Documents/Red/Features/LearningSystem/LearningTools.kt)

### Technical Details & Flow:
- **`LearningTools`**: Integrates the custom reinforcement and prompt modification loop.
  - **`memory_learn`**: Stores structured corrections and feedback under `LEARNING`, `ERROR`, or `PREFERENCE` keys.
  - **`memory_reinforce`**: Increments hit count when the memory was useful.
  - **`promote_learning`**: Promotes memories exceeding hit threshold (e.g., 5 hits) by appending them directly to the system prompt (`soulText`) and deleting the raw memory entry.

---

## 7. Background Daemon Mode
**Files:**
- [DaemonController.kt](file:///home/aditya/Documents/Red/Features/BackgroundDaemon/DaemonController.kt)
- [DaemonController.android.kt](file:///home/aditya/Documents/Red/Features/BackgroundDaemon/DaemonController.android.kt)
- [DaemonService.kt](file:///home/aditya/Documents/Red/Features/BackgroundDaemon/DaemonService.kt)

### Technical Details & Flow:
- **`DaemonController`**: Simple start/stop daemon service manager.
- **`DaemonService`**: Android Foreground Service (`START_STICKY`) which runs in the background.
  - Keeps the app process alive and runs KMP task scheduling loop continuously.
  - Displays a low-priority ongoing notification.

---

## 8. Context Files & Multimodal Attachments
**Files:**
- [AttachmentProcessor.kt](file:///home/aditya/Documents/Red/Features/ContextFiles/AttachmentProcessor.kt)

### Technical Details & Flow:
- **`AttachmentProcessor`**: Translates multi-attachment inputs for LLM requests.
  - Classifies uploaded files based on MIME type and extension (Text vs Binary vs PDF).
  - Encodes attachments to Base64 data.
  - **`splitForMessage`**: Dynamically decodes text attachments (like logs or source files) and prepends them as a text prefix inside the main user prompt block. Binary formats (like images/PDFs) are sent as native multimodal blocks in the payload.

---

## 9. Heartbeat Feature
**Files:**
- [HeartbeatManager.kt](file:///home/aditya/Documents/Red/Features/HeartbeatFeature/HeartbeatManager.kt)
- [HeartbeatPromptBuilder.kt](file:///home/aditya/Documents/Red/Features/HeartbeatFeature/HeartbeatPromptBuilder.kt)
- [HeartbeatNotifier.kt](file:///home/aditya/Documents/Red/Features/HeartbeatFeature/HeartbeatNotifier.kt)
- [HeartbeatNotifier.android.kt](file:///home/aditya/Documents/Red/Features/HeartbeatFeature/HeartbeatNotifier.android.kt)
- [HeartbeatNotifier.desktop.kt](file:///home/aditya/Documents/Red/Features/HeartbeatFeature/HeartbeatNotifier.desktop.kt)

### Technical Details & Flow:
- **`HeartbeatManager`**: Orchestrates autonomous background self-checks.
  - Checks if a heartbeat is due based on elapsed time (`intervalMinutes`) and configured active hours window.
  - Assembles contextual data (unread emails, unread SMS, pending system notifications, memory promotion candidates) and passes them to the prompt builder.
  - Records a running log of success/failure runs.
- **`HeartbeatPromptBuilder`**: Constructs the structured markdown user prompt containing sections like `## New Emails`, `## New SMS`, `## New Notifications`, `## Pending Tasks`, and `## Promotion Candidates`.
- **`HeartbeatNotifier`**: Dispatches system notifications to the user whenever the background heartbeat produces an actionable response.
  - The Android implementation registers a notification channel and binds a launch intent with `EXTRA_OPEN_HEARTBEAT = true` to allow tapping the notification to deep-link straight to the heartbeat chat.
  - The Desktop implementation uses platform-specific hooks: AppleScript on macOS, `notify-send` on Linux, and `java.awt.SystemTray` balloon alerts on Windows.

---

## 10. Schedule Task Feature
**Files:**
- [ScheduledTask.kt](file:///home/aditya/Documents/Red/Features/ScheduleTask/ScheduledTask.kt)
- [CronExpression.kt](file:///home/aditya/Documents/Red/Features/ScheduleTask/CronExpression.kt)
- [TaskStore.kt](file:///home/aditya/Documents/Red/Features/ScheduleTask/TaskStore.kt)
- [TaskScheduler.kt](file:///home/aditya/Documents/Red/Features/ScheduleTask/TaskScheduler.kt)

### Technical Details & Flow:
- **`ScheduledTask`**: Represents a time-gated, recurring, or heartbeat-triggered instruction.
  - Keeps track of execution statistics (`consecutiveFailures`, `recentExecutions`, `lastResult`) and task states (`PENDING`, `COMPLETED`).
- **`CronExpression`**: Custom, zero-dependency 5-field crontab evaluator.
  - Parses standard crontabs (e.g. `*/10 * * * *` or `0 9-18 * * 1-5`).
  - Iteratively searches calendar dates to compute the next valid epoch millisecond execution point.
- **`TaskStore`**: Translates, saves, and updates tasks inside localized JSON formats stored inside AppSettings.
  - Partitions pending items into time/cron tasks or heartbeat prompt additions.
- **`TaskScheduler`**: The background runner running tasks when execution limits are crossed.
  - Interlocks with foreground activities (`isLoadingCheck`) to avoid racing with active chat loops.
  - Routes task execution contexts to match the persistent heartbeat conversation sandboxes, preserving terminal session states.
  - Handles task outcomes: computes next cron times on success, and calculates exponential backoff retry timings on failure.


