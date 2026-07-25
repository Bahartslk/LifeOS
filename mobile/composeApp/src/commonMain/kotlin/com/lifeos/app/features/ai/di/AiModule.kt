package com.lifeos.app.features.ai.di

import com.lifeos.app.features.ai.domain.usecase.BuildAiTaskContextUseCase
import com.lifeos.app.features.ai.presentation.AiAssistantViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * AI Assistant's Koin module, following the same shape as
 * `features/home/di/HomeModule.kt` and `features/travel/di/TravelModule.kt`.
 * No repository or fake data source is registered here — this feature owns
 * no data of its own yet (this integration's scope); [BuildAiTaskContextUseCase]
 * is a dependency-free classification step, and [AiAssistantViewModel]
 * resolves Planner's existing `GetPlannerDashboardUseCase` — already
 * registered by `features/planner/di/PlannerModule.kt`, which Koin's
 * single shared container makes resolvable here with no extra wiring, the
 * same pattern `HomeModule`/`TravelModule` already established.
 */
val aiModule: Module = module {
    factory { BuildAiTaskContextUseCase() }
    viewModel { AiAssistantViewModel(getPlannerDashboard = get(), buildAiTaskContext = get()) }
}
