package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        password: String,
    ): Result<AuthSession> = authRepository.register(fullName, email, password)
}
