package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.repository.AuthRepository

/**
 * A single unit of business logic, per docs/12-project-architecture.md#mvvm-responsibilities
 * — the same thin-wrapper shape every other Auth use case already follows.
 * Calls [AuthRepository.clearSession], which revokes the refresh token
 * server-side (`POST /api/v1/auth/logout`) before clearing the local
 * session. Called from Profile's logout confirmation — the app's only
 * logout affordance.
 */
class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.clearSession()
}
