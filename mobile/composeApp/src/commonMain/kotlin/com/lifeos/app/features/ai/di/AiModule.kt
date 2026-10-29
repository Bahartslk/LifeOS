package com.lifeos.app.features.ai.di

import com.lifeos.app.features.ai.data.remote.ChatRemoteDataSource
import com.lifeos.app.features.ai.data.remote.DailyBriefRemoteDataSource
import com.lifeos.app.features.ai.data.repository.ChatRepositoryImpl
import com.lifeos.app.features.ai.domain.repository.ChatRepository
import com.lifeos.app.features.ai.domain.usecase.SendChatMessageUseCase
import com.lifeos.app.features.ai.presentation.chat.AiChatViewModel
import com.lifeos.app.features.ai.data.repository.DailyBriefRepositoryImpl
import com.lifeos.app.features.ai.domain.repository.DailyBriefRepository
import com.lifeos.app.features.ai.domain.usecase.AddReminderToAgendaUseCase
import com.lifeos.app.features.ai.domain.usecase.BuildAiTaskContextUseCase
import com.lifeos.app.features.ai.domain.usecase.GetDailyBriefUseCase
import com.lifeos.app.features.ai.presentation.AiAssistantViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * AI Assistant's Koin module, following the same shape as
 * `features/home/di/HomeModule.kt` and `features/travel/di/TravelModule.kt`.
 *
 * [AiAssistantViewModel] reads Planner's existing `GetPlannerDashboardUseCase`
 * (registered by `features/planner/di/PlannerModule.kt`) for the task
 * context, and this feature's own [GetDailyBriefUseCase] for the
 * backend-generated Daily Brief. [DailyBriefRepository] is a `single` so its
 * same-day cache survives the ViewModel being recreated.
 */
val aiModule: Module = module {
    single { DailyBriefRemoteDataSource(httpClient = get<HttpClient>()) }
    single<DailyBriefRepository> { DailyBriefRepositoryImpl(remoteDataSource = get()) }
    factory { GetDailyBriefUseCase(dailyBriefRepository = get()) }
    factory { BuildAiTaskContextUseCase() }
    factory { AddReminderToAgendaUseCase(createTask = get()) }

    // Chat: stateless on the server, so nothing here needs to outlive the screen.
    single { ChatRemoteDataSource(httpClient = get<HttpClient>()) }
    single<ChatRepository> { ChatRepositoryImpl(remoteDataSource = get()) }
    factory { SendChatMessageUseCase(chatRepository = get()) }
    viewModel { AiChatViewModel(sendChatMessage = get()) }
    viewModel {
        AiAssistantViewModel(
            getPlannerDashboard = get(),
            buildAiTaskContext = get(),
            getDailyBrief = get(),
            addReminderToAgenda = get(),
        )
    }
}
