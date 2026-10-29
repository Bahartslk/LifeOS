package com.lifeos.app.features.ai.presentation.chat

import com.lifeos.app.features.ai.domain.model.ChatRole

enum class ChatMessageStatus { SENT, SENDING, FAILED }

data class ChatMessageUi(
    val id: Long,
    val role: ChatRole,
    val text: String,
    val status: ChatMessageStatus = ChatMessageStatus.SENT,
)

/**
 * The chat screen's state. The conversation lives only here (the backend
 * keeps none); [remainingMessages] is known after the first reply.
 * [isLimitReached] closes the input for the rest of the day.
 */
data class AiChatUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val remainingMessages: Int? = null,
    val isLimitReached: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSend: Boolean get() = input.isNotBlank() && !isSending && !isLimitReached
    val showSuggestions: Boolean get() = messages.isEmpty()

    /** The last message failed and can be sent again (not when today's limit is used up). */
    val canRetry: Boolean
        get() = !isSending && !isLimitReached && messages.lastOrNull()?.status == ChatMessageStatus.FAILED
}

sealed interface AiChatEvent {
    data class InputChanged(val value: String) : AiChatEvent
    data object SendClicked : AiChatEvent
    data class SuggestionClicked(val text: String) : AiChatEvent
    data object RetryClicked : AiChatEvent
}
