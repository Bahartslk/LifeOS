package com.lifeos.app.core.di

import com.lifeos.app.features.ai.di.aiModule
import com.lifeos.app.features.auth.di.authModule
import com.lifeos.app.features.home.di.homeModule
import com.lifeos.app.features.planner.di.plannerModule
import com.lifeos.app.features.profile.di.profileModule
import com.lifeos.app.features.travel.di.travelModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * Core (feature-agnostic) Koin modules: DI/network/storage infrastructure
 * every feature depends on. See
 * docs/12-project-architecture.md#dependency-injection-boundaries.
 */
fun coreModules(): List<Module> = listOf(
    platformModule(),
    networkModule,
    storageModule,
)

/**
 * Feature Koin modules. Each feature (Authentication, Home, Travel,
 * Planner, AI Assistant, Profile) defines its own module in its own `di/`
 * package and is added here — one line per feature. All six are done.
 */
fun featureModules(): List<Module> = listOf(
    authModule,
    homeModule,
    travelModule,
    plannerModule,
    aiModule,
    profileModule,
)

/**
 * Single Koin entry point shared by both the Android [android.app.Application]
 * and the iOS `MainViewController`. [platformDeclaration] lets each platform
 * attach platform-only Koin configuration (e.g. `androidContext(this)`)
 * before the shared modules are loaded.
 *
 * `allowOverride(true)`: `features/auth/di/AuthModule.kt` intentionally
 * redefines this module's own `AuthTokenProvider` placeholder
 * (`NoOpAuthTokenProvider`, in `networkModule`) with the real
 * implementation once Authentication is wired to a backend — the exact
 * seam `NoOpAuthTokenProvider`'s own KDoc describes. No other binding in
 * the app is expected to collide; if one ever does, Koin still logs which
 * definition won, so an unintended override wouldn't fail silently.
 */
fun initKoin(platformDeclaration: KoinAppDeclaration? = null) {
    startKoin {
        allowOverride(true)
        platformDeclaration?.invoke(this)
        modules(coreModules() + featureModules())
    }
}
