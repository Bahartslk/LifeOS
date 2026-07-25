package com.lifeos.app.features.auth.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Whether the user has completed the onboarding carousel. Unlike
 * [AuthRepository], this has a real (non-fake) implementation from day one
 * — "has this device seen onboarding" is a purely local fact, it never
 * needs a backend.
 */
interface OnboardingRepository {
    fun isOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted()
}
