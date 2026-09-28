package com.lifeos.app.features.auth.di

import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.features.auth.data.AuthTokenProviderImpl
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.local.OnboardingLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource
import com.lifeos.app.features.auth.data.repository.AuthRepositoryImpl
import com.lifeos.app.features.auth.data.repository.OnboardingRepositoryImpl
import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.auth.domain.repository.OnboardingRepository
import com.lifeos.app.features.auth.domain.usecase.CompleteOnboardingUseCase
import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.auth.domain.usecase.LoginUseCase
import com.lifeos.app.features.auth.domain.usecase.LogoutUseCase
import com.lifeos.app.features.auth.domain.usecase.ObserveOnboardingCompletedUseCase
import com.lifeos.app.features.auth.domain.usecase.RegisterUseCase
import com.lifeos.app.features.auth.domain.usecase.RequestPasswordResetUseCase
import com.lifeos.app.features.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.lifeos.app.features.auth.presentation.login.LoginViewModel
import com.lifeos.app.features.auth.presentation.onboarding.OnboardingViewModel
import com.lifeos.app.features.auth.presentation.register.RegisterViewModel
import com.lifeos.app.features.auth.presentation.splash.SplashViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Authentication's Koin module — the only file outside `core/di` that
 * needed to change to wire the real backend in (Iteration 1 of the backend
 * integration migration), per docs/12-project-architecture.md#dependency-injection-boundaries.
 * This module's [AuthTokenProvider] binding is meant to replace
 * `core/di/NetworkModule.kt`'s `NoOpAuthTokenProvider` placeholder, per that
 * class's own KDoc — `core/di/AppModule.kt`'s `startKoin { allowOverride(true) }`
 * is what permits that redefinition (Koin 4's module-level `override` flag
 * from older versions no longer exists; this is the current mechanism).
 */
val authModule: Module = module {
    // Data sources
    single { AuthTokenLocalDataSource(preferencesStorage = get()) }
    single { OnboardingLocalDataSource(preferencesStorage = get()) }

    // A second, unauthenticated HttpClient for this feature's own remote
    // calls only — see HttpClientFactory.createUnauthenticated's KDoc for
    // why Auth can't use the shared authenticated client every other
    // feature will use once they migrate (it would be a circular
    // dependency: refreshing a token is itself one of these calls).
    single<HttpClient>(named("unauthenticated")) {
        HttpClientFactory.createUnauthenticated(engine = get<HttpClientEngine>())
    }
    single { AuthRemoteDataSource(httpClient = get(named("unauthenticated"))) }

    // Repositories
    single<AuthRepository> {
        AuthRepositoryImpl(remoteDataSource = get(), tokenLocalDataSource = get(), bearerTokenCache = get())
    }
    single<OnboardingRepository> { OnboardingRepositoryImpl(localDataSource = get()) }

    // The real AuthTokenProvider — overrides NetworkModule's NoOpAuthTokenProvider.
    single<AuthTokenProvider> {
        AuthTokenProviderImpl(tokenLocalDataSource = get(), authRemoteDataSource = get())
    }

    // Use cases
    factory { LoginUseCase(authRepository = get()) }
    factory { RegisterUseCase(authRepository = get()) }
    factory { RequestPasswordResetUseCase(authRepository = get()) }
    factory { GetSessionUseCase(authRepository = get()) }
    factory { LogoutUseCase(authRepository = get()) }
    factory { ObserveOnboardingCompletedUseCase(onboardingRepository = get()) }
    factory { CompleteOnboardingUseCase(onboardingRepository = get()) }

    // ViewModels
    viewModel { SplashViewModel(getSession = get(), observeOnboardingCompleted = get()) }
    viewModel { OnboardingViewModel(completeOnboarding = get()) }
    viewModel { LoginViewModel(login = get()) }
    viewModel { RegisterViewModel(register = get()) }
    viewModel { ForgotPasswordViewModel(requestPasswordReset = get()) }
}
