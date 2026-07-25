package com.lifeos.app.features.profile.domain.usecase

import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeModeUseCase(private val profileRepository: ProfileRepository) {
    operator fun invoke(): Flow<ThemeMode> = profileRepository.observeThemeMode()
}
