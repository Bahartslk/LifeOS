package com.lifeos.app.features.auth.data

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource
import kotlinx.coroutines.CancellationException

/**
 * The real [AuthTokenProvider] `core/network/NetworkModule.kt`'s
 * `NoOpAuthTokenProvider` placeholder was always meant to be overridden by,
 * per that class's own KDoc. Bound in `AuthModule.kt` with Koin's
 * `override = true` for exactly that reason.
 *
 * [refreshAccessToken] is the [io.ktor.client.plugins.auth.Auth] plugin's
 * `refreshTokens` callback (wired in `HttpClientFactory.create`), invoked on
 * a 401 from any authenticated endpoint (Ktor sends one refresh for any
 * number of concurrent 401s). It rotates the refresh token: the backend
 * revokes the presented one and issues a new one on every use, per
 * docs/12-project-architecture.md#jwt-strategy, so both tokens are saved.
 *
 * When the refresh fails:
 * - **Rejected by the backend** (400/401/403: unknown, expired, revoked or
 *   reused refresh token): the session can never be renewed, so it is
 *   cleared and [SessionExpiryRepositoryImpl] tells the app to go back to
 *   sign-in.
 * - **Anything else** (no network, timeout, 5xx, rate limit): the session is
 *   kept. This request fails, and the next one tries to refresh again.
 */
class AuthTokenProviderImpl(
    private val tokenLocalDataSource: AuthTokenLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val sessionExpiry: SessionExpiryRepositoryImpl,
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            if (e.statusCode in SESSION_REJECTED_STATUS_CODES) {
                tokenLocalDataSource.clearSession()
                sessionExpiry.markSessionExpired()
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        /** Bad request, unauthorized, forbidden: the backend will never accept this refresh token. */
        val SESSION_REJECTED_STATUS_CODES = setOf(400, 401, 403)
    }
}
