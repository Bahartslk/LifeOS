package com.lifeos.app.features.auth.data.repository

import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.BearerTokenCache
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.core.storage.PreferencesStorage
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource
import com.lifeos.app.testing.InMemoryPreferencesDataStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRepositoryImplLoginTest {

    private val tokenLocalDataSource = AuthTokenLocalDataSource(PreferencesStorage(InMemoryPreferencesDataStore()))
    private val recordedRequests = mutableListOf<HttpRequestData>()

    private val engine = MockEngine { request ->
        recordedRequests += request
        val body = when {
            request.url.encodedPath.endsWith("auth/login") ->
                """{"data":{"accessToken":"access-new","refreshToken":"refresh-new",
                "user":{"id":"u1","email":"user@example.com","displayName":"Test User","createdAt":"2026-10-29T00:00:00Z"}}}"""
            request.url.encodedPath.endsWith("auth/register") ->
                """{"data":{"accessToken":"access-new","refreshToken":"refresh-new"}}"""
            else -> """{"data":{}}"""
        }
        respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
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
    fun login_afterAnExpiredSession_theNextRequestCarriesTheNewToken() = runTest {
        // Signed out: the shared client has cached "no token".
        authenticatedClient.get("https://api.test/planner/dashboard")

        repository.login("user@example.com", "Password1!").getOrThrow()
        authenticatedClient.get("https://api.test/planner/dashboard")

        assertEquals(listOf(null, "Bearer access-new"), dashboardAuthHeaders())
    }

    @Test
    fun register_theNextRequestCarriesTheNewToken() = runTest {
        authenticatedClient.get("https://api.test/planner/dashboard")

        repository.register("Test User", "user@example.com", "Password1!").getOrThrow()
        authenticatedClient.get("https://api.test/planner/dashboard")

        assertEquals(listOf(null, "Bearer access-new"), dashboardAuthHeaders())
    }

    private fun dashboardAuthHeaders() = recordedRequests
        .filter { it.url.encodedPath.endsWith("planner/dashboard") }
        .map { it.headers[HttpHeaders.Authorization] }
}
