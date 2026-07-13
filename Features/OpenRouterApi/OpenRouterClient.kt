package com.inspiredandroid.red.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ServiceCredentials(
    val apiKey: String,
    val modelId: String,
)

@Serializable
data class OpenAICompatibleChatRequestDto(
    val messages: List<Message>,
    val model: String?,
    val tools: List<Tool>? = null,
) {
    @Serializable
    data class Message(
        val role: String,
        val content: JsonElement?,
        val tool_calls: List<ToolCall>? = null,
        val tool_call_id: String? = null,
        val reasoningContent: String? = null,
    )

    @Serializable
    data class ToolCall(
        val id: String,
        val function: FunctionCall,
    )

    @Serializable
    data class FunctionCall(
        val name: String,
        val arguments: String,
    )

    @Serializable
    data class Tool(
        val type: String = "function",
        val function: FunctionInfo,
    )

    @Serializable
    data class FunctionInfo(
        val name: String,
        val description: String,
        val parameters: JsonElement,
    )
}

@Serializable
data class OpenAICompatibleChatResponseDto(
    val choices: List<Choice>,
) {
    @Serializable
    data class Choice(
        val message: Message,
    )

    @Serializable
    data class Message(
        val role: String,
        val content: String?,
        val tool_calls: List<ToolCall>? = null,
        val reasoning: String? = null,
    )

    @Serializable
    data class ToolCall(
        val id: String,
        val function: FunctionCall,
    )

    @Serializable
    data class FunctionCall(
        val name: String,
        val arguments: String,
    )
}

class OpenRouterClient(
    private val client: HttpClient,
) {
    private val chatUrl = "https://openrouter.ai/api/v1/chat/completions"

    /**
     * Send chat completions to OpenRouter or any OpenAI-compatible API endpoint.
     */
    suspend fun chat(
        credentials: ServiceCredentials,
        messages: List<OpenAICompatibleChatRequestDto.Message>,
        tools: List<OpenAICompatibleChatRequestDto.Tool>? = null,
        customHeaders: Map<String, String> = emptyMap(),
    ): Result<OpenAICompatibleChatResponseDto> = try {
        val apiKey = credentials.apiKey.ifEmpty { throw Exception("API Key is empty") }
        val model = credentials.modelId.ifEmpty { "openai/gpt-4o-mini" }

        val response: HttpResponse = client.post(chatUrl) {
            contentType(ContentType.Application.Json)
            bearerAuth(apiKey)
            customHeaders.forEach { (k, v) -> header(k, v) }
            setBody(
                OpenAICompatibleChatRequestDto(
                    messages = messages,
                    model = model,
                    tools = tools,
                ),
            )
        }

        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("OpenRouter API error: ${response.status.value} - ${response.status.description}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Validate the configured OpenRouter API key.
     */
    suspend fun validateApiKey(apiKey: String): Result<Unit> = try {
        if (apiKey.isEmpty()) throw Exception("API Key is empty")
        val response: HttpResponse = client.get("https://openrouter.ai/api/v1/auth/key") {
            bearerAuth(apiKey)
        }
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid OpenRouter API Key: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
