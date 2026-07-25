package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.repository.AuthRepository

/** A single unit of business logic, per docs/12-project-architecture.md#mvvm-responsibilities. */
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> =
        authRepository.login(email, password)
}
