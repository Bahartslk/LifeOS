package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.repository.AuthRepository

/** Used by Splash to decide whether a stored session is still available. */
class GetSessionUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): AuthSession? = authRepository.getSession()
}
