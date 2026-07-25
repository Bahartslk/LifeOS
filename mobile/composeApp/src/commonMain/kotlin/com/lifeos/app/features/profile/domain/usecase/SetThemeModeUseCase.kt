package com.lifeos.app.features.profile.domain.usecase

import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.profile.domain.repository.ProfileRepository

class SetThemeModeUseCase(private val profileRepository: ProfileRepository) {
    suspend operator fun invoke(mode: ThemeMode) = profileRepository.setThemeMode(mode)
}
