package com.lifeos.app.features.auth.data.repository

import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.BearerTokenCache
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.core.storage.PreferencesStorage
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.AuthUser
import com.lifeos.app.testing.InMemoryPreferencesDataStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.io.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplLogoutTest {

    private val tokenLocalDataSource = AuthTokenLocalDataSource(PreferencesStorage(InMemoryPreferencesDataStore()))
    private val recordedRequests = mutableListOf<HttpRequestData>()
    private var logoutHandler: MockRequestHandleScope.() -> HttpResponseData = {
        respondJson("""{"data":{"success":true}}""")
    }

    private val engine = MockEngine { request ->
        recordedRequests += request
        if (request.url.encodedPath.endsWith("auth/logout")) logoutHandler() else respondJson("""{"data":{}}""")
    }

    private val authenticatedClient = HttpClientFactory.create(
        engine = engine,
        tokenProvider = object : AuthTokenProvider {
            override suspend fun currentAccessToken(): String? = tokenLocalDataSource.getSession()?.accessToken
            override suspend fun refreshAccessToken(): String? = null
        },
    )

    private val repository = AuthRepositoryImpl(
        remoteDataSource = AuthRemoteDataSource(HttpClientFactory.createUnauthenticated(engine)),
        tokenLocalDataSource = tokenLocalDataSource,
        bearerTokenCache = BearerTokenCache(authenticatedClient),
    )

    @Test
    fun clearSession_revokesRefreshTokenOnServer_andClearsLocalSession() = runTest {
        tokenLocalDataSource.saveSession(session(accessToken = "access-a", refreshToken = "refresh-a"))

        repository.clearSession()

        val logoutRequest = recordedRequests.single { it.url.encodedPath.endsWith("auth/logout") }
        assertEquals("POST", logoutRequest.method.value)
        assertEquals("Bearer access-a", logoutRequest.headers[HttpHeaders.Authorization])
        assertTrue(logoutRequest.bodyText().contains("\"refreshToken\":\"refresh-a\""))
        assertNull(repository.getSession())
    }

    @Test
    fun clearSession_whenBackendIsUnreachable_stillClearsLocalSession() = runTest {
        tokenLocalDataSource.saveSession(session(accessToken = "access-a", refreshToken = "refresh-a"))
        logoutHandler = { throw IOException("network down") }

        repository.clearSession()

        assertNull(repository.getSession())
    }

    @Test
    fun clearSession_whenBackendRejectsLogout_stillClearsLocalSession() = runTest {
        tokenLocalDataSource.saveSession(session(accessToken = "access-a", refreshToken = "refresh-a"))
        logoutHandler = { respondJson("""{"statusCode":401,"error":"UNAUTHORIZED","message":"x"}""", HttpStatusCode.Unauthorized) }

        repository.clearSession()

        assertNull(repository.getSession())
    }

    @Test
    fun clearSession_dropsCachedBearerToken_soTheNextUserIsNotSentThePreviousToken() = runTest {
        tokenLocalDataSource.saveSession(session(accessToken = "access-a", refreshToken = "refresh-a"))
        authenticatedClient.get("https://api.test/planner/dashboard")

        repository.clearSession()
        tokenLocalDataSource.saveSession(session(accessToken = "access-b", refreshToken = "refresh-b"))
        authenticatedClient.get("https://api.test/planner/dashboard")

        val dashboardAuthHeaders = recordedRequests
            .filter { it.url.encodedPath.endsWith("planner/dashboard") }
            .map { it.headers[HttpHeaders.Authorization] }
        assertEquals(listOf("Bearer access-a", "Bearer access-b"), dashboardAuthHeaders)
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

    private fun HttpRequestData.bodyText(): String =
        (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString().orEmpty()
}
