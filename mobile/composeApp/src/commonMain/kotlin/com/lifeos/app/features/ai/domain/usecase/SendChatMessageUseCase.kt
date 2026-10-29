package com.lifeos.app.features.ai.domain.usecase

import com.lifeos.app.features.ai.domain.model.ChatReply
import com.lifeos.app.features.ai.domain.model.ChatTurn
import com.lifeos.app.features.ai.domain.repository.ChatRepository

/**
 * Sends one chat message. The chat is stateless on the server, so the
 * recent conversation goes with every message, limited to the last
 * [MAX_HISTORY_TURNS] turns (the backend accepts at most that many).
 */
class SendChatMessageUseCase(private val chatRepository: ChatRepository) {

    suspend operator fun invoke(message: String, history: List<ChatTurn>): Result<ChatReply> =
        chatRepository.sendMessage(message.trim(), history.takeLast(MAX_HISTORY_TURNS))

    companion object {
        const val MAX_HISTORY_TURNS = 8
        const val MAX_MESSAGE_LENGTH = 500
    }
}
