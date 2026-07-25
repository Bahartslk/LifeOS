package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.repository.AuthRepository

/**
 * A single unit of business logic, per docs/12-project-architecture.md#mvvm-responsibilities
 * — the same thin-wrapper shape every other Auth use case already follows.
 * Calls [AuthRepository.clearSession], which revokes the refresh token
 * server-side (`POST /api/v1/auth/logout`) before clearing the local
 * session. Not yet called from any ViewModel — Profile owns the only
 * logout affordance in the app today, and its "Do NOT implement real
 * logout" scope is unchanged in this iteration; this use case exists so
 * the repository-level integration is complete and ready for Profile to
 * wire up later.
 */
class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.clearSession()
}
