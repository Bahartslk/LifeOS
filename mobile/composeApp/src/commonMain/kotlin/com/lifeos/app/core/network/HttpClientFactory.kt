package com.lifeos.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds the single shared Ktor [HttpClient] used across every feature's
 * remote data source, per docs/12-project-architecture.md#mobile-architecture.
 *
 * Every feature's data layer depends on this client rather than constructing
 * its own, so timeout, retry, serialization, and auth-header behavior stay
 * consistent across Travel, Planner, AI Assistant, and Profile.
 */
object HttpClientFactory {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun create(
        engine: HttpClientEngine,
        tokenProvider: AuthTokenProvider,
    ): HttpClient = HttpClient(engine) {
        applyCommonConfig()

        install(Auth) {
            bearer {
                loadTokens {
                    val token = tokenProvider.currentAccessToken() ?: return@loadTokens null
                    BearerTokens(accessToken = token, refreshToken = "")
                }
                refreshTokens {
                    val refreshed = tokenProvider.refreshAccessToken() ?: return@refreshTokens null
                    BearerTokens(accessToken = refreshed, refreshToken = "")
                }
            }
        }
    }

    /**
     * A second client with no [Auth] plugin at all, for the Authentication
     * feature's own remote calls (login/register/refresh) — those must never
     * depend on [AuthTokenProvider] themselves, since [AuthTokenProvider]'s
     * real implementation depends on Authentication's remote data source in
     * turn (refreshing a token is itself an API call). Routing Auth's own
     * calls through [create]'s client would make that a circular dependency:
     * `HttpClient -> AuthTokenProvider -> AuthRemoteDataSource -> HttpClient`.
     * Every other feature's real remote data source uses [create]'s
     * authenticated client instead, per
     * docs/12-project-architecture.md#dependency-injection-boundaries.
     */
    fun createUnauthenticated(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
        applyCommonConfig()
    }

    private fun HttpClientConfig<*>.applyCommonConfig() {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MILLIS
            connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MILLIS
        }

        defaultRequest {
            url(ApiConfig.BASE_URL)
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }
    }
}
