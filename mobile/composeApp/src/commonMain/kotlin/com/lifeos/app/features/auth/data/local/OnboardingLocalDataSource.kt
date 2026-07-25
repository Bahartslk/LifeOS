package com.lifeos.app.features.auth.data.local

import com.lifeos.app.core.storage.PreferencesStorage
import kotlinx.coroutines.flow.Flow

class OnboardingLocalDataSource(private val preferencesStorage: PreferencesStorage) {

    fun isCompleted(): Flow<Boolean> =
        preferencesStorage.observeBoolean(KEY_ONBOARDING_COMPLETED, defaultValue = false)

    suspend fun setCompleted() {
        preferencesStorage.putBoolean(KEY_ONBOARDING_COMPLETED, true)
    }

    private companion object {
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
