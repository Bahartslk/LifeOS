package com.lifeos.app.features.ai.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.ai.domain.model.AiQuotaExceededException
import com.lifeos.app.features.ai.domain.model.ChatDailyLimitReachedException
import com.lifeos.app.features.ai.domain.model.ChatRole
import com.lifeos.app.features.ai.domain.model.ChatTurn
import com.lifeos.app.features.ai.domain.usecase.SendChatMessageUseCase
import com.lifeos.app.features.ai.presentation.AiAssistantStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A short chat with the assistant. The conversation is kept only here, for
 * as long as the screen lives; every message sends the recent successful
 * turns along (the use case keeps the last
 * [SendChatMessageUseCase.MAX_HISTORY_TURNS]).
 *
 * A failed message stays in the list marked as failed and can be sent again.
 * The two kinds of `429` are told apart: the daily chat limit closes the
 * input for today, while the AI provider's quota shows the same message as
 * the Daily Brief and still allows a retry.
 */
class AiChatViewModel(
    private val sendChatMessage: SendChatMessageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var nextMessageId = 0L

    fun onEvent(event: AiChatEvent) {
        when (event) {
            is AiChatEvent.InputChanged -> _uiState.update {
                it.copy(input = event.value.take(SendChatMessageUseCase.MAX_MESSAGE_LENGTH))
            }
            AiChatEvent.SendClicked -> send(_uiState.value.input)
            is AiChatEvent.SuggestionClicked -> send(event.text)
            AiChatEvent.RetryClicked -> retry()
        }
    }

    private fun send(text: String) {
        val state = _uiState.value
        val message = text.trim()
        if (message.isEmpty() || state.isSending || state.isLimitReached) return

        val pending = ChatMessageUi(
            id = nextMessageId++,
            role = ChatRole.USER,
            text = message,
            status = ChatMessageStatus.SENDING,
        )
        _uiState.update {
            it.copy(messages = it.messages + pending, input = "", isSending = true, errorMessage = null)
        }
        request(pending)
    }

    private fun retry() {
        val failed = _uiState.value.messages.lastOrNull() ?: return
        if (!_uiState.value.canRetry) return
        _uiState.update {
            it.copy(
                messages = it.messages.withStatus(failed.id, ChatMessageStatus.SENDING),
                isSending = true,
                errorMessage = null,
            )
        }
        request(failed)
    }

    /** Sends [message] with every earlier successful turn as history. */
    private fun request(message: ChatMessageUi) {
        val history = _uiState.value.messages
            .filter { it.status == ChatMessageStatus.SENT && it.id != message.id }
            .map { ChatTurn(role = it.role, text = it.text) }

        viewModelScope.launch {
            sendChatMessage(message.text, history)
                .onSuccess { reply ->
                    _uiState.update {
                        val answer = ChatMessageUi(id = nextMessageId++, role = ChatRole.ASSISTANT, text = reply.reply)
                        it.copy(
                            messages = it.messages.withStatus(message.id, ChatMessageStatus.SENT) + answer,
                            isSending = false,
                            remainingMessages = reply.remainingMessages,
                            // Nothing left today: close the input now instead of failing the next message.
                            isLimitReached = reply.remainingMessages <= 0,
                            errorMessage = if (reply.remainingMessages <= 0) AiChatStrings.LIMIT_REACHED_MESSAGE else null,
                        )
                    }
                }
                .onFailure { error ->
                    val limitReached = error is ChatDailyLimitReachedException
                    _uiState.update {
                        it.copy(
                            messages = it.messages.withStatus(message.id, ChatMessageStatus.FAILED),
                            isSending = false,
                            isLimitReached = it.isLimitReached || limitReached,
                            remainingMessages = if (limitReached) 0 else it.remainingMessages,
                            errorMessage = when (error) {
                                is ChatDailyLimitReachedException -> AiChatStrings.LIMIT_REACHED_MESSAGE
                                is AiQuotaExceededException -> AiAssistantStrings.AI_QUOTA_EXCEEDED_MESSAGE
                                else -> AiChatStrings.SEND_ERROR_MESSAGE
                            },
                        )
                    }
                }
        }
    }

    private fun List<ChatMessageUi>.withStatus(id: Long, status: ChatMessageStatus) =
        map { if (it.id == id) it.copy(status = status) else it }
}
