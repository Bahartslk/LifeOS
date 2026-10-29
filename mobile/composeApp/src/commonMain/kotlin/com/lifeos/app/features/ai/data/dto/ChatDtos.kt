package com.lifeos.app.features.ai.data.dto

import kotlinx.serialization.Serializable

/** Body of the backend's `POST /ai/chat` (`backend/src/modules/assistant-chat/dto/chat-request.dto.ts`). */
@Serializable
data class ChatRequestDto(
    val message: String,
    val history: List<ChatTurnDto>,
)

/** `role` is `"user"` or `"assistant"`. */
@Serializable
data class ChatTurnDto(
    val role: String,
    val text: String,
)

@Serializable
data class ChatResponseDto(
    val reply: String,
    val remainingMessages: Int,
)
