package com.lifeos.app.features.ai.data.repository

import com.lifeos.app.features.ai.data.dto.ChatRequestDto
import com.lifeos.app.features.ai.data.dto.ChatTurnDto
import com.lifeos.app.features.ai.data.remote.ChatRemoteDataSource
import com.lifeos.app.features.ai.domain.model.ChatReply
import com.lifeos.app.features.ai.domain.model.ChatRole
import com.lifeos.app.features.ai.domain.model.ChatTurn
import com.lifeos.app.features.ai.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException

/** Nothing is cached or stored: the conversation lives in the chat screen's ViewModel. */
class ChatRepositoryImpl(private val remoteDataSource: ChatRemoteDataSource) : ChatRepository {

    override suspend fun sendMessage(message: String, history: List<ChatTurn>): Result<ChatReply> = try {
        val response = remoteDataSource.sendMessage(
            ChatRequestDto(message = message, history = history.map { it.toDto() }),
        )
        Result.success(ChatReply(reply = response.reply.trim(), remainingMessages = response.remainingMessages))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toAiFailure())
    }
}

private fun ChatTurn.toDto() = ChatTurnDto(
    role = when (role) {
        ChatRole.USER -> "user"
        ChatRole.ASSISTANT -> "assistant"
    },
    text = text,
)
