package com.lifeos.app.features.profile.domain.usecase

import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException

/**
 * Changes the password, then swaps this device to the new token pair the
 * backend issued, so it stays signed in while every other session is
 * signed out. If the backend call fails, nothing changed.
 */
class ChangePasswordUseCase(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> = try {
        val tokens = profileRepository.changePassword(currentPassword, newPassword)
        authRepository.replaceSessionTokens(tokens)
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
