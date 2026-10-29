package com.lifeos.app.features.profile.domain.usecase

import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.profile.domain.model.UserProfile
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException

/**
 * Renames the user on the backend, then updates the stored session's name
 * too, since Home's greeting reads the name from the session. The caller
 * validates the name first (Register's NameValidator rules).
 */
class UpdateDisplayNameUseCase(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(displayName: String): Result<UserProfile> = try {
        val profile = profileRepository.updateDisplayName(displayName.trim())
        authRepository.updateSessionDisplayName(profile.displayName)
        Result.success(profile)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
