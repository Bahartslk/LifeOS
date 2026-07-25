package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingCompletedUseCase(private val onboardingRepository: OnboardingRepository) {
    operator fun invoke(): Flow<Boolean> = onboardingRepository.isOnboardingCompleted()
}
