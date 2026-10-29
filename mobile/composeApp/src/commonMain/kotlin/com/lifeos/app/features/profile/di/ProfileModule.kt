package com.lifeos.app.features.profile.di

import com.lifeos.app.features.profile.data.remote.UsersRemoteDataSource
import com.lifeos.app.features.profile.data.repository.ProfileRepositoryImpl
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import com.lifeos.app.features.profile.domain.usecase.DeleteAccountUseCase
import com.lifeos.app.features.profile.domain.usecase.GetUserProfileUseCase
import com.lifeos.app.features.profile.domain.usecase.ObserveThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SetThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SyncDeviceTimezoneUseCase
import com.lifeos.app.features.profile.domain.usecase.UpdateDisplayNameUseCase
import com.lifeos.app.features.profile.presentation.ProfileViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Profile's Koin module, following the same shape as
 * `features/home/di/HomeModule.kt`/`features/ai/di/AiModule.kt`.
 * [UsersRemoteDataSource] uses the shared authenticated [HttpClient].
 * Authentication's repositories and use cases ([com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase],
 * `LogoutUseCase`, `AuthRepository`, `SessionExpiryRepository`) come from
 * `features/auth/di/AuthModule.kt`.
 */
val profileModule: Module = module {
    single { UsersRemoteDataSource(httpClient = get<HttpClient>()) }
    single<ProfileRepository> { ProfileRepositoryImpl(themePreferenceStorage = get(), remoteDataSource = get()) }
    factory { ObserveThemeModeUseCase(profileRepository = get()) }
    factory { SetThemeModeUseCase(profileRepository = get()) }
    factory { GetUserProfileUseCase(profileRepository = get()) }
    factory { SyncDeviceTimezoneUseCase(profileRepository = get()) }
    factory { UpdateDisplayNameUseCase(profileRepository = get(), authRepository = get()) }
    factory {
        DeleteAccountUseCase(profileRepository = get(), authRepository = get(), sessionExpiryRepository = get())
    }
    viewModel {
        ProfileViewModel(
            getSession = get(),
            getUserProfile = get(),
            syncDeviceTimezone = get(),
            observeThemeMode = get(),
            setThemeMode = get(),
            updateDisplayName = get(),
            deleteAccount = get(),
            logout = get(),
        )
    }
}
