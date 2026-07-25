package com.lifeos.app.features.home.di

import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.home.data.datasource.HomeDataSource
import com.lifeos.app.features.home.data.repository.HomeRepositoryImpl
import com.lifeos.app.features.home.domain.repository.HomeRepository
import com.lifeos.app.features.home.domain.usecase.GetHomeDashboardUseCase
import com.lifeos.app.features.home.presentation.HomeViewModel
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import com.lifeos.app.features.travel.domain.usecase.GetTravelListUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Home's Koin module, following the same shape as
 * `features/auth/di/AuthModule.kt`. [HomeRepositoryImpl] (Iteration 4 of
 * the backend integration migration) replaces the former
 * `FakeHomeRepository` — see its KDoc for why Home still has no real
 * network call of its own.
 *
 * [HomeViewModel] also resolves [GetPlannerDashboardUseCase] and
 * [GetTravelListUseCase] — already registered by
 * `features/planner/di/PlannerModule.kt` and `features/travel/di/TravelModule.kt`
 * respectively, which Koin's single shared container makes resolvable here
 * with no extra wiring (no new use case needed for either).
 */
val homeModule: Module = module {
    single { HomeDataSource() }
    single<HomeRepository> { HomeRepositoryImpl(dataSource = get()) }
    factory { GetHomeDashboardUseCase(homeRepository = get()) }
    viewModel {
        HomeViewModel(
            getHomeDashboard = get(),
            getPlannerDashboard = get(),
            getTravelList = get(),
            getSession = get(),
        )
    }
}
