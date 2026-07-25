package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.repository.AuthRepository

class RequestPasswordResetUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> =
        authRepository.requestPasswordReset(email)
}
