package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.auth.domain.repository.SessionExpiryRepository

/**
 * Sets a new password with the emailed code. On success the backend has
 * signed out every session; the user signs in again, and the sign-in screen
 * says the password was reset.
 */
class ResetPasswordUseCase(
    private val authRepository: AuthRepository,
    private val sessionExpiryRepository: SessionExpiryRepository,
) {
    suspend operator fun invoke(email: String, code: String, newPassword: String): Result<Unit> =
        authRepository.resetPassword(email, code, newPassword)
            .onSuccess { sessionExpiryRepository.markPasswordReset() }
}
