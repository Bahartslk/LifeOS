package com.lifeos.app.features.ai.domain.repository

import com.lifeos.app.features.ai.domain.model.ChatReply
import com.lifeos.app.features.ai.domain.model.ChatTurn

interface ChatRepository {
    /**
     * Sends [message] with the earlier [history] (oldest first) and returns the
     * assistant's reply. Fails with
     * [com.lifeos.app.features.ai.domain.model.ChatDailyLimitReachedException]
     * when today's chat messages are used up, and with
     * [com.lifeos.app.features.ai.domain.model.AiQuotaExceededException] when
     * the AI provider's quota is.
     */
    suspend fun sendMessage(message: String, history: List<ChatTurn>): Result<ChatReply>
}
