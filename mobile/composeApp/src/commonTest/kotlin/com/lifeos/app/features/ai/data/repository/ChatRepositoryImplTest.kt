package com.lifeos.app.features.ai.data.repository

import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.features.ai.data.remote.ChatRemoteDataSource
import com.lifeos.app.features.ai.domain.model.AiQuotaExceededException
import com.lifeos.app.features.ai.domain.model.ChatDailyLimitReachedException
import com.lifeos.app.features.ai.domain.model.ChatReply
import com.lifeos.app.features.ai.domain.model.ChatRole
import com.lifeos.app.features.ai.domain.model.ChatTurn
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChatRepositoryImplTest {

    private val recordedRequests = mutableListOf<HttpRequestData>()
    private var respondWith: MockRequestHandleScope.() -> HttpResponseData = {
        respondJson("""{"data":{"reply":" Önce raporu bitir. ","remainingMessages":4}}""")
    }

    private val repository = ChatRepositoryImpl(
        ChatRemoteDataSource(
            HttpClientFactory.create(
                engine = MockEngine { request ->
                    recordedRequests += request
                    respondWith()
                },
                tokenProvider = object : AuthTokenProvider {
                    override suspend fun currentAccessToken(): String = "token"
                    override suspend fun refreshAccessToken(): String? = null
                },
            ),
        ),
    )

    @Test
    fun sendMessage_postsMessageAndHistory_andMapsTheReply() = runTest {
        val reply = repository.sendMessage(
            "Yarınki toplantıya nasıl hazırlanayım?",
            listOf(ChatTurn(ChatRole.USER, "Merhaba"), ChatTurn(ChatRole.ASSISTANT, "Selam!")),
        ).getOrThrow()

        assertEquals(ChatReply("Önce raporu bitir.", remainingMessages = 4), reply)
        val request = recordedRequests.single()
        assertTrue(request.url.encodedPath.endsWith("api/v1/ai/chat"))
        val body = Json.parseToJsonElement((request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()).jsonObject
        assertEquals("Yarınki toplantıya nasıl hazırlanayım?", body["message"]?.jsonPrimitive?.content)
        assertEquals(
            listOf("user" to "Merhaba", "assistant" to "Selam!"),
            body["history"]!!.jsonArray.map {
                it.jsonObject["role"]!!.jsonPrimitive.content to it.jsonObject["text"]!!.jsonPrimitive.content
            },
        )
    }

    @Test
    fun dailyChatLimit429_withItsCode_becomesChatDailyLimitReachedException() = runTest {
        respondWith = {
            respondJson(
                """{"statusCode":429,"error":"TOO_MANY_REQUESTS","code":"CHAT_DAILY_LIMIT_REACHED","message":"Daily chat limit of 5 messages reached."}""",
                HttpStatusCode.TooManyRequests,
            )
        }

        assertIs<ChatDailyLimitReachedException>(repository.sendMessage("Soru", emptyList()).exceptionOrNull())
    }

    @Test
    fun aiQuota429_withoutACode_becomesAiQuotaExceededException() = runTest {
        respondWith = {
            respondJson(
                """{"statusCode":429,"error":"TOO_MANY_REQUESTS","message":"AI provider \"gemini\" rate limit exceeded."}""",
                HttpStatusCode.TooManyRequests,
            )
        }

        assertIs<AiQuotaExceededException>(repository.sendMessage("Soru", emptyList()).exceptionOrNull())
    }

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
}
