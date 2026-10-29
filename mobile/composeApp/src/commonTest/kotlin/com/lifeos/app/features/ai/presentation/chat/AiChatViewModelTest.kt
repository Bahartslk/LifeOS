package com.lifeos.app.features.ai.presentation.chat

import com.lifeos.app.features.ai.domain.model.AiQuotaExceededException
import com.lifeos.app.features.ai.domain.model.ChatDailyLimitReachedException
import com.lifeos.app.features.ai.domain.model.ChatReply
import com.lifeos.app.features.ai.domain.model.ChatRole
import com.lifeos.app.features.ai.domain.model.ChatTurn
import com.lifeos.app.features.ai.domain.repository.ChatRepository
import com.lifeos.app.features.ai.domain.usecase.SendChatMessageUseCase
import com.lifeos.app.features.ai.presentation.AiAssistantStrings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AiChatViewModelTest {

    private val repository = FakeChatRepository()
    private lateinit var viewModel: AiChatViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = AiChatViewModel(SendChatMessageUseCase(repository))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun send_success_addsBothMessages_clearsInput_andShowsRemaining() = runTest {
        repository.results += Result.success(ChatReply("Önce raporu bitir.", remainingMessages = 4))

        viewModel.onEvent(AiChatEvent.InputChanged("  Bugün neye odaklanmalıyım?  "))
        viewModel.onEvent(AiChatEvent.SendClicked)

        val state = viewModel.uiState.value
        assertEquals(
            listOf(
                ChatRole.USER to "Bugün neye odaklanmalıyım?",
                ChatRole.ASSISTANT to "Önce raporu bitir.",
            ),
            state.messages.map { it.role to it.text },
        )
        assertTrue(state.messages.all { it.status == ChatMessageStatus.SENT })
        assertEquals("", state.input)
        assertEquals(4, state.remainingMessages)
        assertFalse(state.isSending)
        assertNull(state.errorMessage)
        assertEquals(emptyList(), repository.requests.single().history)
    }

    @Test
    fun suggestion_isSentDirectly() = runTest {
        repository.results += Result.success(ChatReply("Cevap", remainingMessages = 4))

        viewModel.onEvent(AiChatEvent.SuggestionClicked("Bugün neye odaklanmalıyım?"))

        assertEquals("Bugün neye odaklanmalıyım?", repository.requests.single().message)
    }

    @Test
    fun blankInput_andTapsWhileSending_sendNothing() = runTest {
        viewModel.onEvent(AiChatEvent.InputChanged("   "))
        viewModel.onEvent(AiChatEvent.SendClicked)
        assertTrue(repository.requests.isEmpty())

        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        repository.results += Result.success(ChatReply("Cevap", remainingMessages = 4))
        viewModel.onEvent(AiChatEvent.SuggestionClicked("Birinci"))
        assertTrue(viewModel.uiState.value.isSending)
        viewModel.onEvent(AiChatEvent.SuggestionClicked("İkinci"))
        gate.complete(Unit)

        assertEquals(listOf("Birinci"), repository.requests.map { it.message })
    }

    @Test
    fun input_isLimitedTo500Characters() = runTest {
        viewModel.onEvent(AiChatEvent.InputChanged("a".repeat(520)))

        assertEquals(500, viewModel.uiState.value.input.length)
    }

    @Test
    fun failure_marksTheMessageFailed_andRetrySendsItAgainWithoutItInTheHistory() = runTest {
        repository.results += Result.success(ChatReply("İlk cevap", remainingMessages = 4))
        viewModel.onEvent(AiChatEvent.SuggestionClicked("İlk soru"))
        repository.results += Result.failure(RuntimeException("timeout"))

        viewModel.onEvent(AiChatEvent.SuggestionClicked("İkinci soru"))

        var state = viewModel.uiState.value
        assertEquals(ChatMessageStatus.FAILED, state.messages.last().status)
        assertEquals(AiChatStrings.SEND_ERROR_MESSAGE, state.errorMessage)
        assertTrue(state.canRetry)

        repository.results += Result.success(ChatReply("İkinci cevap", remainingMessages = 3))
        viewModel.onEvent(AiChatEvent.RetryClicked)

        state = viewModel.uiState.value
        assertEquals(listOf("İlk soru", "İlk cevap", "İkinci soru", "İkinci cevap"), state.messages.map { it.text })
        assertTrue(state.messages.all { it.status == ChatMessageStatus.SENT })
        val retried = repository.requests.last()
        assertEquals("İkinci soru", retried.message)
        assertEquals(listOf("İlk soru", "İlk cevap"), retried.history.map { it.text })
    }

    @Test
    fun history_sendsOnlyTheLast8Turns() = runTest {
        repeat(5) { i ->
            repository.results += Result.success(ChatReply("cevap $i", remainingMessages = 10))
            viewModel.onEvent(AiChatEvent.SuggestionClicked("soru $i"))
        }
        repository.results += Result.success(ChatReply("cevap 5", remainingMessages = 10))

        viewModel.onEvent(AiChatEvent.SuggestionClicked("soru 5"))

        val history = repository.requests.last().history
        assertEquals(SendChatMessageUseCase.MAX_HISTORY_TURNS, history.size)
        assertEquals("soru 1", history.first().text)
        assertEquals("cevap 4", history.last().text)
        assertEquals(ChatRole.ASSISTANT, history.last().role)
    }

    @Test
    fun dailyChatLimit_closesTheInputForToday() = runTest {
        repository.results += Result.failure(ChatDailyLimitReachedException())

        viewModel.onEvent(AiChatEvent.SuggestionClicked("Bir soru"))

        val state = viewModel.uiState.value
        assertTrue(state.isLimitReached)
        assertEquals(0, state.remainingMessages)
        assertEquals(AiChatStrings.LIMIT_REACHED_MESSAGE, state.errorMessage)
        assertFalse(state.canRetry)

        viewModel.onEvent(AiChatEvent.InputChanged("Yine de"))
        viewModel.onEvent(AiChatEvent.SendClicked)
        viewModel.onEvent(AiChatEvent.RetryClicked)
        assertEquals(1, repository.requests.size)
    }

    @Test
    fun aiQuota_showsTheAiQuotaMessage_andKeepsTheInputOpen() = runTest {
        repository.results += Result.failure(AiQuotaExceededException())

        viewModel.onEvent(AiChatEvent.SuggestionClicked("Bir soru"))

        val state = viewModel.uiState.value
        assertFalse(state.isLimitReached)
        assertEquals(AiAssistantStrings.AI_QUOTA_EXCEEDED_MESSAGE, state.errorMessage)
        assertTrue(state.canRetry)
    }

    @Test
    fun lastAllowedMessage_closesTheInputWithoutAnotherRequest() = runTest {
        repository.results += Result.success(ChatReply("Son cevap", remainingMessages = 0))

        viewModel.onEvent(AiChatEvent.SuggestionClicked("Son soru"))

        val state = viewModel.uiState.value
        assertTrue(state.isLimitReached)
        assertEquals("Son cevap", state.messages.last().text)
        assertEquals(AiChatStrings.LIMIT_REACHED_MESSAGE, state.errorMessage)
    }

    private class FakeChatRepository : ChatRepository {
        data class Request(val message: String, val history: List<ChatTurn>)

        val requests = mutableListOf<Request>()
        val results = ArrayDeque<Result<ChatReply>>()
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun sendMessage(message: String, history: List<ChatTurn>): Result<ChatReply> {
            requests += Request(message, history)
            gate?.await()
            return results.removeFirst()
        }
    }
}
