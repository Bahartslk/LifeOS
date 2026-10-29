package com.lifeos.app.features.profile.domain.usecase

import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.auth.domain.repository.SessionExpiryRepository
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException

/**
 * Deletes the account on the backend, then ends the session on the device
 * and leaves a notice for the sign-in screen. If the backend call fails,
 * nothing was deleted and the session is kept.
 *
 * The session is cleared locally only: the backend already removed every
 * refresh token with the account, so a logout call would have nothing to revoke.
 */
class DeleteAccountUseCase(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val sessionExpiryRepository: SessionExpiryRepository,
) {
    suspend operator fun invoke(password: String): Result<Unit> {
        try {
            profileRepository.deleteAccount(password)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(e)
        }
        authRepository.clearLocalSession()
        sessionExpiryRepository.markAccountDeleted()
        return Result.success(Unit)
    }
}
