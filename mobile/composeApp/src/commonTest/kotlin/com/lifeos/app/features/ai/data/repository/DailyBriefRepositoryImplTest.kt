package com.lifeos.app.features.ai.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.features.ai.data.remote.DailyBriefRemoteDataSource
import com.lifeos.app.features.ai.domain.model.AiQuotaExceededException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** [DailyBriefRepositoryImpl] against the real remote data source and Ktor client, with HTTP served by [MockEngine]. */
class DailyBriefRepositoryImplTest {

    private val recordedRequests = mutableListOf<HttpRequestData>()
    private var respondWith: MockRequestHandleScope.() -> HttpResponseData = { overview(ALL_PARTS) }
    private var today = LocalDate(2026, 10, 28)

    private val engine = MockEngine { request ->
        recordedRequests += request
        val path = request.url.encodedPath.substringAfter("/api/v1/")
        check(path == "daily-brief/overview") { "Unexpected request: ${request.method.value} $path" }
        respondWith()
    }

    private val repository = DailyBriefRepositoryImpl(
        remoteDataSource = DailyBriefRemoteDataSource(
            HttpClientFactory.create(
                engine = engine,
                tokenProvider = object : AuthTokenProvider {
                    override suspend fun currentAccessToken(): String = ACCESS_TOKEN
                    override suspend fun refreshAccessToken(): String? = null
                },
            ),
        ),
        today = { today },
    )

    @Test
    fun getDailyBrief_sendsOneRequestAndMapsAllThreeParts() = runTest {
        val brief = repository.getDailyBrief().getOrThrow()

        assertEquals(1, recordedRequests.size)
        assertEquals(HttpMethod.Post, recordedRequests.single().method)
        assertEquals("Bearer $ACCESS_TOKEN", recordedRequests.single().headers[HttpHeaders.Authorization])
        assertEquals("Sunumu bitir", brief.summary?.suggestedFocus)
        assertEquals(listOf("14:30 toplantı"), brief.summary?.keyEvents)
        assertEquals(listOf("Slaytlar", "Otel"), brief.priorities?.map { it.taskTitle })
        assertEquals(listOf(1, 2), brief.priorities?.map { it.rank })
        assertEquals("Harika bir gün olacak!", brief.motivation)
        assertTrue(brief.isComplete)
    }

    @Test
    fun getDailyBrief_completeBrief_isCachedForTheSameDay() = runTest {
        repository.getDailyBrief().getOrThrow()
        repository.getDailyBrief().getOrThrow()

        assertEquals(1, recordedRequests.size)
    }

    @Test
    fun getDailyBrief_forceRefreshOrNewDay_callsBackendAgain() = runTest {
        repository.getDailyBrief().getOrThrow()
        repository.getDailyBrief(forceRefresh = true).getOrThrow()
        assertEquals(2, recordedRequests.size)

        today = LocalDate(2026, 10, 29)
        repository.getDailyBrief().getOrThrow()
        assertEquals(3, recordedRequests.size)
    }

    @Test
    fun getDailyBrief_nullPart_isMappedToNullAndTheBriefIsNotCached() = runTest {
        respondWith = { overview("""{"summary":$SUMMARY,"priorities":null,"motivation":$MOTIVATION}""") }

        val brief = repository.getDailyBrief().getOrThrow()

        assertNull(brief.priorities)
        assertEquals("Sunumu bitir", brief.summary?.suggestedFocus)
        assertEquals("Harika bir gün olacak!", brief.motivation)
        assertFalse(brief.isComplete)

        repository.getDailyBrief().getOrThrow()
        assertEquals(2, recordedRequests.size)
    }

    @Test
    fun getDailyBrief_missingPartsAndBlankMotivation_areMappedToNull() = runTest {
        respondWith = { overview("""{"priorities":$PRIORITIES,"motivation":{"message":"  "}}""") }

        val brief = repository.getDailyBrief().getOrThrow()

        assertNull(brief.summary)
        assertNull(brief.motivation)
        assertEquals(listOf("Slaytlar", "Otel"), brief.priorities?.map { it.taskTitle })
    }

    @Test
    fun getDailyBrief_emptyRanking_isAValidPartAndIsCached() = runTest {
        respondWith = { overview("""{"summary":$SUMMARY,"priorities":[],"motivation":$MOTIVATION}""") }

        val brief = repository.getDailyBrief().getOrThrow()
        repository.getDailyBrief().getOrThrow()

        assertEquals(emptyList(), brief.priorities)
        assertTrue(brief.isComplete)
        assertEquals(1, recordedRequests.size)
    }

    @Test
    fun getDailyBrief_allPartsNull_returnsFailure() = runTest {
        respondWith = { overview("""{"summary":null,"priorities":null,"motivation":null}""") }

        assertIs<IllegalStateException>(repository.getDailyBrief().exceptionOrNull())
    }

    @Test
    fun getDailyBrief_requestFails_returnsFailureAndIsNotCached() = runTest {
        respondWith = {
            respondJson(
                """{"statusCode":503,"error":"Service Unavailable","message":"No AI provider is available"}""",
                HttpStatusCode.ServiceUnavailable,
            )
        }

        val error = repository.getDailyBrief().exceptionOrNull()

        assertIs<ApiException>(error)
        assertEquals(503, error.statusCode)

        respondWith = { overview(ALL_PARTS) }
        assertTrue(repository.getDailyBrief().getOrThrow().isComplete)
        assertEquals(2, recordedRequests.size)
    }

    @Test
    fun getDailyBrief_quotaExceeded429_returnsAiQuotaExceededException() = runTest {
        respondWith = {
            respondJson(
                """{"statusCode":429,"error":"TOO_MANY_REQUESTS","message":"AI provider \"gemini\" rate limit exceeded."}""",
                HttpStatusCode.TooManyRequests,
            )
        }

        assertIs<AiQuotaExceededException>(repository.getDailyBrief().exceptionOrNull())
    }

    private fun MockRequestHandleScope.overview(result: String): HttpResponseData = respondJson(
        """{"data":{"provider":"gemini","model":"m","usage":{"inputTokens":1,"outputTokens":1,"totalTokens":2},"latencyMs":10,
        "result":$result}}""",
    )

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private companion object {
        const val ACCESS_TOKEN = "test-access-token"
        const val SUMMARY =
            """{"todayPriorities":["Slaytlar"],"keyEvents":[" 14:30 toplantı ",""],"suggestedFocus":" Sunumu bitir "}"""
        const val PRIORITIES =
            """[{"taskTitle":"Otel","suggestedRank":2,"reason":"3 gün kaldı"},
            {"taskTitle":"Slaytlar","suggestedRank":1,"reason":"Yarın teslim"}]"""
        const val MOTIVATION = """{"message":"Harika bir gün olacak!"}"""
        const val ALL_PARTS = """{"summary":$SUMMARY,"priorities":$PRIORITIES,"motivation":$MOTIVATION}"""
    }
}
