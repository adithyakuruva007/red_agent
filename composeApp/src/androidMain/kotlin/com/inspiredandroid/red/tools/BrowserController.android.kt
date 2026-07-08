package com.inspiredandroid.red.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import com.inspiredandroid.red.data.SharedJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

actual class BrowserController actual constructor() {
    private val context: Context by inject(Context::class.java)
    private var webView: WebView? = null
    private val json = SharedJson

    private suspend fun getWebView(): WebView = withContext(Dispatchers.Main) {
        var view = webView
        if (view == null) {
            view = WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }
                // Size it like a standard viewport so layout is correct
                layout(0, 0, 1080, 1920)
            }
            webView = view
        }
        view
    }

    actual suspend fun navigate(url: String): BrowserResult = withContext(Dispatchers.Main) {
        try {
            val view = getWebView()
            suspendCancellableCoroutine<Unit> { continuation ->
                view.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        // Allow brief moment for dynamic JS to render
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (continuation.isActive) continuation.resume(Unit)
                        }, 1500)
                    }
                }
                view.loadUrl(url)
            }
            getPageStructureInternal(view)
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun click(elementId: Int): BrowserResult = withContext(Dispatchers.Main) {
        val view = webView ?: return@withContext BrowserResult(success = false, error = "Browser session not initialized")
        try {
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
            val clicked = evaluateJavascript(view, js).toBoolean()
            if (!clicked) {
                return@withContext BrowserResult(success = false, error = "Element with ID $elementId not found or not clickable")
            }
            // Wait for any navigation or updates
            suspendCancellableCoroutine<Unit> { continuation ->
                Handler(Looper.getMainLooper()).postDelayed({
                    continuation.resume(Unit)
                }, 1500)
            }
            getPageStructureInternal(view)
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun typeText(elementId: Int, text: String): BrowserResult = withContext(Dispatchers.Main) {
        val view = webView ?: return@withContext BrowserResult(success = false, error = "Browser session not initialized")
        try {
            // Escape single quotes in text for JS embedding
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
            val typed = evaluateJavascript(view, js).toBoolean()
            if (!typed) {
                return@withContext BrowserResult(success = false, error = "Element with ID $elementId not found or not editable")
            }
            getPageStructureInternal(view)
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun scroll(down: Boolean): BrowserResult = withContext(Dispatchers.Main) {
        val view = webView ?: return@withContext BrowserResult(success = false, error = "Browser session not initialized")
        try {
            val amount = if (down) 600 else -600
            val js = "window.scrollBy(0, $amount); true;"
            evaluateJavascript(view, js)
            // Wait briefly for scroll rendering
            suspendCancellableCoroutine<Unit> { continuation ->
                Handler(Looper.getMainLooper()).postDelayed({
                    continuation.resume(Unit)
                }, 500)
            }
            getPageStructureInternal(view)
        } catch (e: Exception) {
            BrowserResult(success = false, error = e.message)
        }
    }

    actual suspend fun getPageStructure(): BrowserResult = withContext(Dispatchers.Main) {
        val view = webView ?: return@withContext BrowserResult(success = false, error = "Browser session not initialized")
        getPageStructureInternal(view)
    }

    private suspend fun getPageStructureInternal(view: WebView): BrowserResult {
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

        val jsonStr = evaluateJavascript(view, js)
        // Clean up returning quote formatting from evaluateJavascript raw string
        val cleanedJson = if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
            // WebView evaluation can escape the inner JSON string returned, unescape it
            jsonParserUnescape(jsonStr)
        } else {
            jsonStr
        }

        return try {
            val response = json.decodeFromString<BrowserResponsePayload>(cleanedJson)
            BrowserResult(
                success = true,
                pageTitle = response.title,
                currentUrl = response.url,
                elements = response.elements
            )
        } catch (e: Exception) {
            BrowserResult(success = false, error = "Failed to parse elements structure: ${e.message}. Raw: $cleanedJson")
        }
    }

    actual suspend fun getScreenshot(): ByteArray? = withContext(Dispatchers.Main) {
        val view = webView ?: return@withContext null
        try {
            val width = view.width.takeIf { it > 0 } ?: 1080
            val height = view.height.takeIf { it > 0 } ?: 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream)
            bitmap.recycle()
            outputStream.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

    actual suspend fun close() = withContext(Dispatchers.Main) {
        webView?.destroy()
        webView = null
    }

    private suspend fun evaluateJavascript(view: WebView, script: String): String = suspendCancellableCoroutine { continuation ->
        view.evaluateJavascript(script, object : ValueCallback<String> {
            override fun onReceiveValue(value: String?) {
                continuation.resume(value ?: "")
            }
        })
    }

    private fun jsonParserUnescape(escaped: String): String {
        // Simple unescape helper for standard JSON responses wrapped inside a JS-eval string
        val s = escaped.substring(1, escaped.length - 1)
        return s.replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\r", "\r")
    }
}

@kotlinx.serialization.Serializable
private data class BrowserResponsePayload(
    val title: String,
    val url: String,
    val elements: List<InteractiveElement>
)
