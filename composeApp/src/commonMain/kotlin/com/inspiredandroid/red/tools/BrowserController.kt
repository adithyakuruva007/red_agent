package com.inspiredandroid.red.tools

import kotlinx.serialization.Serializable

@Serializable
enum class BrowserActionType {
    CLICK,
    TYPE,
    SCROLL_DOWN,
    SCROLL_UP,
}

@Serializable
data class InteractiveElement(
    val id: Int,
    val tag: String,
    val text: String,
    val selector: String,
    val type: String? = null,
    val value: String? = null,
    val checked: Boolean? = null,
)

@Serializable
data class BrowserResult(
    val success: Boolean,
    val error: String? = null,
    val pageTitle: String? = null,
    val currentUrl: String? = null,
    val elements: List<InteractiveElement> = emptyList(),
)

expect class BrowserController() {
    suspend fun navigate(url: String): BrowserResult
    suspend fun click(elementId: Int): BrowserResult
    suspend fun typeText(elementId: Int, text: String): BrowserResult
    suspend fun scroll(down: Boolean): BrowserResult
    suspend fun getPageStructure(): BrowserResult
    suspend fun getScreenshot(): ByteArray?
    suspend fun close()
}
