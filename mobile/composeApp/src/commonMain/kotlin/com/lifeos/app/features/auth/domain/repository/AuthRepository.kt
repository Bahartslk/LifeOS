package com.lifeos.app.features.auth.domain.repository

import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.SessionTokens

/**
 * Abstracts session-issuing operations (login, register, password reset)
 * behind an interface, per docs/12-project-architecture.md#repository-pattern.
 * ViewModels and use cases depend only on this interface; the concrete
 * implementation ([com.lifeos.app.features.auth.data.repository.AuthRepositoryImpl])
 * is supplied via Koin, per docs/12-project-architecture.md#dependency-injection-boundaries.
 *
 * Backed by the real `/api/v1/auth` endpoints (Iteration 1 of the backend
 * integration migration) — see `data/repository/AuthRepositoryImpl.kt`.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>

    suspend fun register(fullName: String, email: String, password: String): Result<AuthSession>

    suspend fun requestPasswordReset(email: String): Result<Unit>

    /** The persisted session from a previous app run, if any — used by Splash. */
    suspend fun getSession(): AuthSession?

    /** Signs out: revokes the session on the backend (best effort), then clears it on the device. */
    suspend fun clearSession()

    /** Clears the session on the device only, for when the backend has already ended it (e.g. the account was deleted). */
    suspend fun clearLocalSession()

    /** Keeps the stored session's name in step after the user renames themselves. */
    suspend fun updateSessionDisplayName(fullName: String)

    /** Swaps the stored session to a new token pair, keeping the signed-in user. */
    suspend fun replaceSessionTokens(tokens: SessionTokens)
}
