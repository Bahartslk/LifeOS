package com.lifeos.app.features.auth.data.repository

import com.lifeos.app.features.auth.data.local.OnboardingLocalDataSource
import com.lifeos.app.features.auth.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

/**
 * The real (non-fake) implementation — onboarding completion is a purely
 * local fact, so unlike [com.lifeos.app.features.auth.data.repository.AuthRepositoryImpl] this never needs to change
 * when the backend arrives.
 */
class OnboardingRepositoryImpl(
    private val localDataSource: OnboardingLocalDataSource,
) : OnboardingRepository {

    override fun isOnboardingCompleted(): Flow<Boolean> = localDataSource.isCompleted()

    override suspend fun setOnboardingCompleted() = localDataSource.setCompleted()
}
