package com.lifeos.app.features.auth.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.BearerTokenCache
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.AuthUser
import com.lifeos.app.features.auth.domain.model.EmailAlreadyRegisteredException
import com.lifeos.app.features.auth.domain.model.InvalidCredentialsException
import com.lifeos.app.features.auth.domain.model.InvalidResetCodeException
import com.lifeos.app.features.auth.domain.model.PasswordResetRateLimitedException
import com.lifeos.app.features.auth.domain.model.SessionTokens
import com.lifeos.app.features.auth.domain.repository.AuthRepository
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

/**
 * Real implementation of [AuthRepository], backed by
 * `POST /api/v1/auth/{register,login,refresh,logout}` via
 * [AuthRemoteDataSource] — replaces the former `FakeAuthRepository` per
 * that class's own "Replacing this with the real backend" plan. Nothing
 * outside this file changed to make that swap: [AuthRepository]'s
 * signature, [AuthSession]/[AuthUser], and every use case/ViewModel that
 * depends on them are exactly as they were.
 *
 * Backend responses are mapped to the two exception types
 * [LoginUseCase][com.lifeos.app.features.auth.domain.usecase.LoginUseCase]/
 * [RegisterUseCase][com.lifeos.app.features.auth.domain.usecase.RegisterUseCase]'s
 * callers already expect (same types the fake repository used) — a real
 * backend surfaces more failure modes than the fake one did (network
 * failure, timeout, 5xx), which fall through to [Result.failure] with the
 * raised exception as-is; `LoginViewModel`/`RegisterViewModel` already
 * treat every failure generically today, per their own "a real backend
 * would surface distinct failure types" comments — refining that is a
 * follow-up, not part of this integration.
 */
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val tokenLocalDataSource: AuthTokenLocalDataSource,
    private val bearerTokenCache: BearerTokenCache,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        return try {
            val response = remoteDataSource.login(email, password)
            val session = AuthSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                user = AuthUser(email = response.user.email, fullName = response.user.displayName),
            )
            startSession(session)
            Result.success(session)
        } catch (e: ApiException) {
            if (e.statusCode == HttpStatusCode.Unauthorized.value) {
                Result.failure(InvalidCredentialsException())
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(fullName: String, email: String, password: String): Result<AuthSession> {
        return try {
            val tokens = remoteDataSource.register(displayName = fullName, email = email, password = password)
            // The register response is a bare token pair (backend's
            // AuthTokensDto never echoes the user back) — the session's user
            // is built from what was just submitted, not from the response.
            val session = AuthSession(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                user = AuthUser(email = email, fullName = fullName),
            )
            startSession(session)
            Result.success(session)
        } catch (e: ApiException) {
            if (e.statusCode == HttpStatusCode.Conflict.value) {
                Result.failure(EmailAlreadyRegisteredException())
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> = passwordResetCall {
        remoteDataSource.forgotPassword(email)
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> =
        passwordResetCall { remoteDataSource.resetPassword(email = email, code = code, newPassword = newPassword) }

    /** Maps the reset routes' two specific failures; anything else (network, 5xx) passes through as is. */
    private suspend fun passwordResetCall(call: suspend () -> Unit): Result<Unit> = try {
        call()
        Result.success(Unit)
    } catch (e: ApiException) {
        Result.failure(
            when {
                e.statusCode == HttpStatusCode.TooManyRequests.value -> PasswordResetRateLimitedException()
                e.code == INVALID_RESET_CODE -> InvalidResetCodeException()
                else -> e
            },
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getSession(): AuthSession? = tokenLocalDataSource.getSession()

    override suspend fun clearSession() {
        val session = tokenLocalDataSource.getSession()
        if (session != null) {
            // Best-effort: the local session is cleared regardless of whether
            // the network call succeeds, so a logout never leaves the user
            // stuck signed-in-but-broken because the backend was unreachable.
            runCatching { remoteDataSource.logout(session.accessToken, session.refreshToken) }
        }
        clearLocalSession()
    }

    override suspend fun clearLocalSession() {
        tokenLocalDataSource.clearSession()
        bearerTokenCache.clear()
    }

    override suspend fun updateSessionDisplayName(fullName: String) {
        tokenLocalDataSource.updateFullName(fullName)
    }

    /** Like [startSession]: the shared client must drop its cached token so the next request uses the new one. */
    override suspend fun replaceSessionTokens(tokens: SessionTokens) {
        tokenLocalDataSource.saveTokens(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
        bearerTokenCache.clear()
    }

    /**
     * Saves the new session and drops whatever the shared client cached
     * (e.g. "no token" after an expired session), so the very next request
     * already carries the new access token instead of first getting a 401.
     */
    private suspend fun startSession(session: AuthSession) {
        tokenLocalDataSource.saveSession(session)
        bearerTokenCache.clear()
    }

    private companion object {
        /** The backend's `code` for a wrong, expired or used-up reset code. */
        const val INVALID_RESET_CODE = "INVALID_RESET_CODE"
    }
}
