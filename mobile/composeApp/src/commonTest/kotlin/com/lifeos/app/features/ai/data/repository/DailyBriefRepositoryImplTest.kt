package com.lifeos.app.features.ai.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.features.ai.data.remote.DailyBriefRemoteDataSource
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
    private val routes = mutableMapOf<String, MockRequestHandleScope.() -> HttpResponseData>()
    private var today = LocalDate(2026, 10, 28)

    private val engine = MockEngine { request ->
        recordedRequests += request
        val path = request.url.encodedPath.substringAfter("/api/v1/")
        val handler = routes[path] ?: error("Unexpected request: ${request.method.value} $path")
        handler()
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
    fun getDailyBrief_mapsAllThreePartsAndSortsPriorities() = runTest {
        respondWithAllParts()

        val brief = repository.getDailyBrief().getOrThrow()

        assertEquals("Sunumu bitir", brief.summary?.suggestedFocus)
        assertEquals(listOf("14:30 toplantı"), brief.summary?.keyEvents)
        assertEquals(listOf("Slaytlar", "Otel"), brief.priorities?.map { it.taskTitle })
        assertEquals(listOf(1, 2), brief.priorities?.map { it.rank })
        assertEquals("Harika bir gün olacak!", brief.motivation)
        assertTrue(brief.isComplete)
        assertTrue(recordedRequests.all { it.method == HttpMethod.Post })
        assertTrue(recordedRequests.all { it.headers[HttpHeaders.Authorization] == "Bearer $ACCESS_TOKEN" })
    }

    @Test
    fun getDailyBrief_completeBrief_isCachedForTheSameDay() = runTest {
        respondWithAllParts()

        repository.getDailyBrief().getOrThrow()
        repository.getDailyBrief().getOrThrow()

        assertEquals(3, recordedRequests.size)
    }

    @Test
    fun getDailyBrief_forceRefreshOrNewDay_callsBackendAgain() = runTest {
        respondWithAllParts()

        repository.getDailyBrief().getOrThrow()
        repository.getDailyBrief(forceRefresh = true).getOrThrow()
        assertEquals(6, recordedRequests.size)

        today = LocalDate(2026, 10, 29)
        repository.getDailyBrief().getOrThrow()
        assertEquals(9, recordedRequests.size)
    }

    @Test
    fun getDailyBrief_onePartFails_returnsTheOthersAndIsNotCached() = runTest {
        respondWithAllParts()
        routes["daily-brief/motivation"] = { serviceUnavailable() }

        val brief = repository.getDailyBrief().getOrThrow()

        assertNull(brief.motivation)
        assertEquals("Sunumu bitir", brief.summary?.suggestedFocus)
        assertFalse(brief.isComplete)

        repository.getDailyBrief().getOrThrow()
        assertEquals(6, recordedRequests.size)
    }

    @Test
    fun getDailyBrief_allPartsFail_returnsFailure() = runTest {
        listOf("summary", "priorities", "motivation").forEach { routes["daily-brief/$it"] = { serviceUnavailable() } }

        val error = repository.getDailyBrief().exceptionOrNull()

        assertIs<ApiException>(error)
        assertEquals(503, error.statusCode)
    }

    private fun respondWithAllParts() {
        routes["daily-brief/summary"] = {
            respondJson(
                """{"data":{"provider":"gemini","model":"m","usage":{"inputTokens":1,"outputTokens":1,"totalTokens":2},"latencyMs":10,
                "result":{"todayPriorities":["Slaytlar"],"keyEvents":[" 14:30 toplantı ",""],"suggestedFocus":" Sunumu bitir "}}}""",
            )
        }
        routes["daily-brief/priorities"] = {
            respondJson(
                """{"data":{"provider":"gemini","model":"m","result":{"priorities":[
                {"taskTitle":"Otel","suggestedRank":2,"reason":"3 gün kaldı"},
                {"taskTitle":"Slaytlar","suggestedRank":1,"reason":"Yarın teslim"}]}}}""",
            )
        }
        routes["daily-brief/motivation"] = {
            respondJson("""{"data":{"provider":"gemini","model":"m","result":{"message":"Harika bir gün olacak!"}}}""")
        }
    }

    private fun MockRequestHandleScope.serviceUnavailable(): HttpResponseData = respondJson(
        """{"statusCode":503,"error":"Service Unavailable","message":"No AI provider is available"}""",
        HttpStatusCode.ServiceUnavailable,
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
    }
}
