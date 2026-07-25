package com.lifeos.app.features.auth.data

import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource

/**
 * The real [AuthTokenProvider] `core/network/NetworkModule.kt`'s
 * `NoOpAuthTokenProvider` placeholder was always meant to be overridden by,
 * per that class's own KDoc. Bound in `AuthModule.kt` with Koin's
 * `override = true` for exactly that reason.
 *
 * [refreshAccessToken] is the [io.ktor.client.plugins.auth.Auth] plugin's
 * `refreshTokens` callback (wired in `HttpClientFactory.create`) — invoked
 * automatically on a 401 from any *authenticated* endpoint. It rotates the
 * refresh token here (the backend revokes the presented one and issues a
 * new one on every use, per docs/12-project-architecture.md#jwt-strategy),
 * so both tokens are persisted, not just the access token — reusing the
 * old refresh token on the next attempt would already be revoked and fail.
 * If the refresh call itself fails (expired/revoked/reused refresh token),
 * the stale local session is cleared so [com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase]
 * correctly reports "signed out" on the next check instead of retrying a
 * token that can never succeed again.
 */
class AuthTokenProviderImpl(
    private val tokenLocalDataSource: AuthTokenLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthTokenProvider {

    override suspend fun currentAccessToken(): String? =
        tokenLocalDataSource.getSession()?.accessToken

    override suspend fun refreshAccessToken(): String? {
        val session = tokenLocalDataSource.getSession() ?: return null
        return try {
            val tokens = authRemoteDataSource.refresh(session.refreshToken)
            tokenLocalDataSource.saveSession(
                session.copy(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken),
            )
            tokens.accessToken
        } catch (e: Exception) {
            tokenLocalDataSource.clearSession()
            null
        }
    }
}
