package com.phishlens.app.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    @SerialName("model") val model: String = "llama-3.1-8b-instant",
    @SerialName("messages") val messages: List<ChatMessage>,
    @SerialName("temperature") val temperature: Double = 0.3,
    @SerialName("max_tokens") val maxTokens: Int = 300
)

@Serializable
data class ChatMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@Serializable
data class ChatCompletionResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("choices") val choices: List<Choice> = emptyList()
)

@Serializable
data class Choice(
    @SerialName("index") val index: Int = 0,
    @SerialName("message") val message: ChatMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)
