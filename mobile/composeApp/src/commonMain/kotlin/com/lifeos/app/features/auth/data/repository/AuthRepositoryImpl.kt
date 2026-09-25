package com.lifeos.app.features.auth.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.AuthUser
import com.lifeos.app.features.auth.domain.model.EmailAlreadyRegisteredException
import com.lifeos.app.features.auth.domain.model.InvalidCredentialsException
import com.lifeos.app.features.auth.domain.repository.AuthRepository
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay

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
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        return try {
            val response = remoteDataSource.login(email, password)
            val session = AuthSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                user = AuthUser(email = response.user.email, fullName = response.user.displayName),
            )
            tokenLocalDataSource.saveSession(session)
            Result.success(session)
        } catch (e: ApiException) {
            logFailure("login", e)
            if (e.statusCode == HttpStatusCode.Unauthorized.value) {
                Result.failure(InvalidCredentialsException())
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            logFailure("login", e)
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
            tokenLocalDataSource.saveSession(session)
            Result.success(session)
        } catch (e: ApiException) {
            logFailure("register", e)
            if (e.statusCode == HttpStatusCode.Conflict.value) {
                Result.failure(EmailAlreadyRegisteredException())
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            logFailure("register", e)
            Result.failure(e)
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        // FR-AUTH-04's backend endpoints (POST /auth/forgot-password,
        // /auth/reset-password) don't exist yet — docs/15-api-design.md
        // documents both as undelivered. Kept as a graceful, always-succeeds
        // stub identical to the former fake behavior (Forgot Password's
        // "Success state" doesn't distinguish a known vs. unknown email
        // either way, per that screen's own anti-enumeration reasoning) so
        // ForgotPasswordViewModel needs no changes; real integration is a
        // follow-up once that backend work ships.
        delay(FAKE_REQUEST_DELAY_MS)
        return Result.success(Unit)
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
        tokenLocalDataSource.clearSession()
    }

    /**
     * The two catch blocks above previously discarded the caught exception
     * entirely once it didn't match the status code they special-case —
     * `Result.failure(e)` still carries it to the ViewModel, but every
     * ViewModel today collapses any non-domain exception to the same generic
     * "check your connection" message (per their own comments), so the real
     * cause — a 400 from a body the backend rejected, a DNS failure, a
     * timeout, a TLS error, anything — was never visible anywhere. Printing
     * it here means it shows up in `adb logcat` even though the UI still only
     * shows the generic message.
     */
    private fun logFailure(operation: String, e: Throwable) {
        println("[AuthRepositoryImpl] $operation failed: ${e::class.simpleName}: ${e.message}\n${e.stackTraceToString()}")
    }

    private companion object {
        const val FAKE_REQUEST_DELAY_MS = 500L
    }
}
