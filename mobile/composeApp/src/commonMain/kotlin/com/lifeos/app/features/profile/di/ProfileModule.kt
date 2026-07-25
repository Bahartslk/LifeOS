package com.lifeos.app.features.profile.di

import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.profile.data.repository.ProfileRepositoryImpl
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import com.lifeos.app.features.profile.domain.usecase.ObserveThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SetThemeModeUseCase
import com.lifeos.app.features.profile.presentation.ProfileViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Profile's Koin module, following the same shape as
 * `features/home/di/HomeModule.kt`/`features/ai/di/AiModule.kt`.
 * [ProfileRepositoryImpl] depends on `core/storage`'s [com.lifeos.app.core.storage.ThemePreferenceStorage] —
 * already registered by `core/di/StorageModule.kt`, resolvable here with
 * no extra wiring. [ProfileViewModel] also resolves Authentication's
 * existing [GetSessionUseCase] — already registered by
 * `features/auth/di/AuthModule.kt` — the same cross-feature-use-case-reuse
 * pattern `HomeModule`/`TravelModule`/`AiModule` already established for
 * Planner; no new Auth use case needed.
 */
val profileModule: Module = module {
    single<ProfileRepository> { ProfileRepositoryImpl(themePreferenceStorage = get()) }
    factory { ObserveThemeModeUseCase(profileRepository = get()) }
    factory { SetThemeModeUseCase(profileRepository = get()) }
    viewModel {
        ProfileViewModel(
            getSession = get(),
            observeThemeMode = get(),
            setThemeMode = get(),
        )
    }
}
