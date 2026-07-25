package com.lifeos.app.features.auth.domain.repository

import com.lifeos.app.features.auth.domain.model.AuthSession

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

    suspend fun clearSession()
}
