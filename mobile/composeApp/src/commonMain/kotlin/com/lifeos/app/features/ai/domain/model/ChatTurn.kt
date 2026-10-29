package com.lifeos.app.features.ai.domain.model

enum class ChatRole { USER, ASSISTANT }

/** One message of a chat with the assistant. */
data class ChatTurn(
    val role: ChatRole,
    val text: String,
)

/** The assistant's answer and how many chat messages the user can still send today. */
data class ChatReply(
    val reply: String,
    val remainingMessages: Int,
)

/**
 * The user has used all of today's chat messages (the backend's `429` with
 * `code: "CHAT_DAILY_LIMIT_REACHED"`). Different from [AiQuotaExceededException],
 * which means the AI provider's own quota is used up.
 */
class ChatDailyLimitReachedException(cause: Throwable? = null) : Exception("Daily chat limit reached.", cause)
