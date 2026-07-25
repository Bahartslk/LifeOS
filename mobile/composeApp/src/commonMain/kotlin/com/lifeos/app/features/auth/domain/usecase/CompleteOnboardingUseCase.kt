package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.repository.OnboardingRepository

class CompleteOnboardingUseCase(private val onboardingRepository: OnboardingRepository) {
    suspend operator fun invoke() = onboardingRepository.setOnboardingCompleted()
}
