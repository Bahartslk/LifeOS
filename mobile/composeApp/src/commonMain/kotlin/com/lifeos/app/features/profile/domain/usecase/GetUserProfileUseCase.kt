package com.lifeos.app.features.profile.domain.usecase

import com.lifeos.app.features.profile.domain.model.UserProfile
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException

class GetUserProfileUseCase(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(): Result<UserProfile> = try {
        Result.success(profileRepository.getProfile())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
