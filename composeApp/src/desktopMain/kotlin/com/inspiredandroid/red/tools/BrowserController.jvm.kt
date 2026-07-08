package com.inspiredandroid.red.tools

import com.inspiredandroid.red.data.SharedJson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.net.URI
import java.net.http.HttpClient as JdkHttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.WebSocket
import java.nio.file.Files
import java.util.Base64
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

actual class BrowserController actual constructor() {
    private var process: Process? = null
    private var tempDir: File? = null
    private var webSocket: WebSocket? = null
    private val nextMessageId = AtomicInteger(1)
    private val pendingRequests = ConcurrentHashMap<Int, CompletableDeferred<String>>()
    private val json = SharedJson

    private fun findChromePath(): String? {
        val os = System.getProperty("os.name").lowercase()
        val paths = when {
            os.contains("mac") -> listOf(
                "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                "/Applications/Chromium.app/Contents/MacOS/Chromium"
            )
            os.contains("win") -> listOf(
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                System.getenv("USERPROFILE") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe"
            )
            else -> listOf(
                "/usr/bin/google-chrome",
                "/usr/bin/chromium-browser",
                "/usr/bin/chromium",
                "/usr/bin/chrome"
            )
        }
        return paths.find { File(it).exists() }
    }

    private suspend fun ensureBrowserSession() {
        if (webSocket != null) return

        val chromePath = findChromePath() ?: throw Exception("Google Chrome or Chromium was not detected on this system. Please install Chrome to use the Web Browser tools.")
        tempDir = Files.createTempDirectory("red-chrome").toFile()
        
        val pb = ProcessBuilder(
            chromePath,
            "--headless",
            "--remote-debugging-port=9222",
            "--disable-gpu",
            "--user-data-dir=" + tempDir!!.absolutePath
        )
        process = pb.start()
        
        // Wait for Chrome DevTools server to start
        delay(1500)

        val client = JdkHttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://127.0.0.1:9222/json/list"))
            .GET()
            .build()

        var jsonResponse = ""
        for (i in 1..5) {
            try {
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                jsonResponse = response.body()
                if (jsonResponse.isNotBlank()) break
            } catch (e: Exception) {
                delay(1000)
            }
        }

        if (jsonResponse.isBlank()) {
            throw Exception("Failed to connect to Google Chrome DevTools server.")
        }

        // Find webSocketDebuggerUrl in response using regex
        val wsUrl = "\"webSocketDebuggerUrl\":\\s*\"(ws://[^\"]+)\"".toRegex()
            .find(jsonResponse)?.groupValues?.get(1)
            ?: throw Exception("No debugging target found in Google Chrome. Response: $jsonResponse")

        val listener = object : WebSocket.Listener {
            private val textAccumulator = StringBuilder()

            override fun onOpen(webSocket: WebSocket) {
                webSocket.request(1)
            }

            override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
                textAccumulator.append(data)
                if (last) {
                    val fullText = textAccumulator.toString()
                    textAccumulator.setLength(0)
                    handleIncomingMessage(fullText)
                }
                webSocket.request(1)
                return null
            }

            override fun onError(webSocket: WebSocket?, error: Throwable?) {
                error?.printStackTrace()
            }
        }

        webSocket = client.newWebSocketBuilder()
            .buildAsync(URI.create(wsUrl), listener)
            .join()

        // Enable Page domain to get lifecycle events
        sendCommand("Page.enable", emptyMap())
    }

    private fun handleIncomingMessage(text: String) {
        val idMatch = "\"id\":\\s*(\\d+)".toRegex().find(text)
        if (idMatch != null) {
            val id = idMatch.groupValues[1].toInt()
            pendingRequests.remove(id)?.complete(text)
        }
    }

    private suspend fun sendCommand(method: String, params: Map<String, Any>): String {
        val socket = webSocket ?: throw Exception("WebSocket connection is not established")
        val id = nextMessageId.getAndIncrement()
        
        val escapedParams = params.entries.joinToString(",") { (key, value) ->
            val serializedValue = when (value) {
                is String -> "\"${value.replace("\"", "\\\"").replace("\n", "\\n")}\""
                is Boolean -> value.toString()
                is Number -> value.toString()
                else -> "\"$value\""
            }
            "\"$key\":$serializedValue"
        }
        
        val payload = "{\"id\":$id,\"method\":\"$method\",\"params\":{$escapedParams}}"
        val deferred = CompletableDeferred<String>()
        pendingRequests[id] = deferred
        
        socket.sendText(payload, true)
        
        return withTimeoutOrNull(10000) { deferred.await() }
            ?: throw Exception("CDP command timed out: $method")
    }

    actual suspend fun navigate(url: String): BrowserResult {
        return try {
            ensureBrowserSession()
            sendCommand("Page.navigate", mapOf("url" to url))
            // Wait for load and rendering
            delay(2000)
            getPageStructure()
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun click(elementId: Int): BrowserResult {
        return try {
            ensureBrowserSession()
            val js = """
                (function() {
                    const el = document.querySelector('[data-red-id="${elementId}"]');
                    if (el) {
                        el.click();
                        return true;
                    }
                    return false;
                })()
            """.trimIndent()
            val response = sendCommand("Runtime.evaluate", mapOf("expression" to js, "returnByValue" to true))
            val clicked = response.contains("\"value\":true")
            if (!clicked) {
                return BrowserResult(success = false, error = "Element with ID $elementId not found or not clickable")
            }
            delay(1500)
            getPageStructure()
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun typeText(elementId: Int, text: String): BrowserResult {
        return try {
            ensureBrowserSession()
            val escapedText = text.replace("'", "\\'")
            val js = """
                (function() {
                    const el = document.querySelector('[data-red-id="${elementId}"]');
                    if (el) {
                        el.value = '${escapedText}';
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        return true;
                    }
                    return false;
                })()
            """.trimIndent()
            val response = sendCommand("Runtime.evaluate", mapOf("expression" to js, "returnByValue" to true))
            val typed = response.contains("\"value\":true")
            if (!typed) {
                return BrowserResult(success = false, error = "Element with ID $elementId not found or not editable")
            }
            getPageStructure()
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun scroll(down: Boolean): BrowserResult {
        return try {
            ensureBrowserSession()
            val amount = if (down) 600 else -600
            val js = "window.scrollBy(0, $amount); true;"
            sendCommand("Runtime.evaluate", mapOf("expression" to js, "returnByValue" to true))
            delay(500)
            getPageStructure()
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun getPageStructure(): BrowserResult {
        return try {
            ensureBrowserSession()
            val js = """
                (function() {
                    const elements = [];
                    let idCounter = 1;
                    const walk = document.createTreeWalker(document.body, NodeFilter.SHOW_ELEMENT);
                    let el;
                    while (el = walk.nextNode()) {
                        const style = window.getComputedStyle(el);
                        if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') continue;
                        const rect = el.getBoundingClientRect();
                        if (rect.width === 0 || rect.height === 0) continue;

                        const tag = el.tagName.toLowerCase();
                        const isInteractive = ['button', 'input', 'a', 'select', 'textarea'].includes(tag) || 
                                              el.getAttribute('role') === 'button' ||
                                              el.onclick != null ||
                                              el.hasAttribute('onclick');

                        if (isInteractive) {
                            let selector = tag;
                            if (el.id) {
                                selector += '#' + el.id;
                            } else if (el.className) {
                                selector += '.' + el.className.trim().split(/\s+/).join('.');
                            }
                            
                            let text = el.innerText || el.ariaLabel || el.placeholder || el.getAttribute('placeholder') || el.value || '';
                            text = text.trim().substring(0, 100);

                            let checked = null;
                            if (tag === 'input' && (el.type === 'checkbox' || el.type === 'radio')) {
                                checked = el.checked;
                            }

                            el.setAttribute('data-red-id', String(idCounter));
                            elements.push({
                                id: idCounter++,
                                tag: tag,
                                text: text,
                                selector: selector,
                                type: el.getAttribute('type'),
                                value: el.value || null,
                                checked: checked
                            });
                        }
                    }
                    return JSON.stringify({
                        title: document.title,
                        url: window.location.href,
                        elements: elements
                    });
                })()
            """.trimIndent()
            
            val response = sendCommand("Runtime.evaluate", mapOf("expression" to js, "returnByValue" to true))
            
            // Extract the result value from CDP JSON-RPC response
            val valueMatch = "\"value\":\\s*\"(\\{.+?\\})\"".toRegex().find(response)
            val jsonStr = valueMatch?.groupValues?.get(1)?.replace("\\\"", "\"")?.replace("\\\\", "\\")
                ?: throw Exception("Failed to extract page structure response. Raw response: $response")

            val payload = json.decodeFromString<BrowserResponsePayload>(jsonStr)
            BrowserResult(
                success = true,
                pageTitle = payload.title,
                currentUrl = payload.url,
                elements = payload.elements
            )
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun getScreenshot(): ByteArray? {
        return try {
            ensureBrowserSession()
            val response = sendCommand("Page.captureScreenshot", mapOf("format" to "png"))
            val dataMatch = "\"data\":\\s*\"([^\"]+)\"".toRegex().find(response)
            val base64Data = dataMatch?.groupValues?.get(1) ?: return null
            Base64.getDecoder().decode(base64Data)
        } catch (_: Exception) {
            null
        }
    }

    actual suspend fun close() {
        try {
            webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "Close session")
        } catch (_: Exception) {}
        webSocket = null
        
        try {
            process?.destroy()
        } catch (_: Exception) {}
        process = null
        
        try {
            tempDir?.deleteRecursively()
        } catch (_: Exception) {}
        tempDir = null
        
        pendingRequests.clear()
    }
}

@kotlinx.serialization.Serializable
private data class BrowserResponsePayload(
    val title: String,
    val url: String,
    val elements: List<InteractiveElement>
)
