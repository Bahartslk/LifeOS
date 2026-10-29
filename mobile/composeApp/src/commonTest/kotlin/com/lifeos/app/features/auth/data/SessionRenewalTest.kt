package com.lifeos.app.features.auth.data

import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.core.storage.PreferencesStorage
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.AuthUser
import com.lifeos.app.features.auth.domain.model.SignInNotice
import com.lifeos.app.testing.InMemoryPreferencesDataStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Session renewal end to end through the real authenticated [io.ktor.client.HttpClient]
 * ([HttpClientFactory.create], Ktor's bearer `Auth` plugin) and [AuthTokenProviderImpl],
 * with HTTP served by [MockEngine]. Like the real backend, the mock's 401s carry no
 * `WWW-Authenticate` header.
 */
class SessionRenewalTest {

    private val tokenLocalDataSource = AuthTokenLocalDataSource(PreferencesStorage(InMemoryPreferencesDataStore()))
    private val sessionExpiry = SessionExpiryRepositoryImpl()
    private val recordedRequests = mutableListOf<HttpRequestData>()
    private var refreshHandler: MockRequestHandleScope.() -> HttpResponseData = {
        respondJson("""{"data":{"accessToken":"access-new","refreshToken":"refresh-new"}}""")
    }

    private val engine = MockEngine { request ->
        recordedRequests += request
        when {
            request.url.encodedPath.endsWith("auth/refresh") -> refreshHandler()
            // Only the renewed access token is still valid.
            request.headers[HttpHeaders.Authorization] == "Bearer access-new" -> respondJson("""{"data":{}}""")
            else -> respondJson(
                """{"statusCode":401,"error":"UNAUTHORIZED","message":"Unauthorized"}""",
                HttpStatusCode.Unauthorized,
            )
        }
    }

    private val tokenProvider = AuthTokenProviderImpl(
        tokenLocalDataSource = tokenLocalDataSource,
        authRemoteDataSource = AuthRemoteDataSource(HttpClientFactory.createUnauthenticated(engine)),
        sessionExpiry = sessionExpiry,
    )
    private val client = HttpClientFactory.create(engine = engine, tokenProvider = tokenProvider)

    @Test
    fun expiredAccessToken_validRefreshToken_renewsSilentlyWithOneRefresh() = runTest {
        tokenLocalDataSource.saveSession(session(accessToken = "access-expired", refreshToken = "refresh-old"))
        val expiries = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            sessionExpiry.observeSessionExpired().toList(expiries)
        }

        // Several screens loading at once, like the app on launch.
        val statuses = List(3) { async { client.get("https://api.test/planner/dashboard").status } }.awaitAll()

        assertEquals(List(3) { HttpStatusCode.OK }, statuses)
        assertEquals(1, recordedRequests.count { it.url.encodedPath.endsWith("auth/refresh") })
        val saved = tokenLocalDataSource.getSession()
        assertEquals("access-new", saved?.accessToken)
        assertEquals("refresh-new", saved?.refreshToken)
        assertTrue(expiries.isEmpty())
        assertNull(sessionExpiry.consumeSignInNotice())
    }

    @Test
    fun rejectedRefreshToken_clearsTheSession_andSignalsExpiry() = runTest {
        tokenLocalDataSource.saveSession(session(accessToken = "access-expired", refreshToken = "refresh-revoked"))
        refreshHandler = {
            respondJson(
                """{"statusCode":401,"error":"UNAUTHORIZED","message":"Refresh token has been revoked."}""",
                HttpStatusCode.Unauthorized,
            )
        }
        val expired = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            sessionExpiry.observeSessionExpired().first()
        }

        val status = client.get("https://api.test/planner/dashboard").status

        assertEquals(HttpStatusCode.Unauthorized, status)
        assertNull(tokenLocalDataSource.getSession())
        expired.await()
        assertEquals(SignInNotice.SESSION_EXPIRED, sessionExpiry.consumeSignInNotice())
    }

    @Test
    fun networkErrorDuringRefresh_keepsTheSession_andTriesAgainNextTime() = runTest {
        tokenLocalDataSource.saveSession(session(accessToken = "access-expired", refreshToken = "refresh-old"))
        refreshHandler = { throw IOException("network down") }

        val first = client.get("https://api.test/planner/dashboard").status

        assertEquals(HttpStatusCode.Unauthorized, first)
        assertEquals("refresh-old", tokenLocalDataSource.getSession()?.refreshToken)
        assertNull(sessionExpiry.consumeSignInNotice())

        // The network is back: the next request renews and succeeds.
        refreshHandler = { respondJson("""{"data":{"accessToken":"access-new","refreshToken":"refresh-new"}}""") }
        val second = client.get("https://api.test/planner/dashboard").status

        assertEquals(HttpStatusCode.OK, second)
        assertEquals("refresh-new", tokenLocalDataSource.getSession()?.refreshToken)
    }

    @Test
    fun serverErrorDuringRefresh_keepsTheSession() = runTest {
        tokenLocalDataSource.saveSession(session(accessToken = "access-expired", refreshToken = "refresh-old"))
        refreshHandler = {
            respondJson("""{"statusCode":503,"error":"SERVICE_UNAVAILABLE","message":"x"}""", HttpStatusCode.ServiceUnavailable)
        }

        client.get("https://api.test/planner/dashboard")

        assertEquals("refresh-old", tokenLocalDataSource.getSession()?.refreshToken)
        assertNull(sessionExpiry.consumeSignInNotice())
    }

    private fun session(accessToken: String, refreshToken: String) = AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = AuthUser(email = "user@example.com", fullName = "Test User"),
    )

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )
}
