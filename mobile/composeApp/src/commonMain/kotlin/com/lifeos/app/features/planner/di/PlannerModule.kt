package com.lifeos.app.features.planner.di

import com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource
import com.lifeos.app.features.planner.data.remote.PlannerRemoteDataSource
import com.lifeos.app.features.planner.data.repository.PlannerRepositoryImpl
import com.lifeos.app.features.planner.domain.repository.PlannerRepository
import com.lifeos.app.features.planner.domain.usecase.CreateTaskUseCase
import com.lifeos.app.features.planner.domain.usecase.DeleteTaskUseCase
import com.lifeos.app.features.planner.domain.usecase.GetCalendarMonthUseCase
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import com.lifeos.app.features.planner.domain.usecase.GetTaskDetailUseCase
import com.lifeos.app.features.planner.domain.usecase.GetTasksForDayUseCase
import com.lifeos.app.features.planner.domain.usecase.ToggleSubtaskCompletionUseCase
import com.lifeos.app.features.planner.domain.usecase.ToggleTaskCompletionUseCase
import com.lifeos.app.features.planner.presentation.CalendarViewModel
import com.lifeos.app.features.planner.presentation.CreateTaskViewModel
import com.lifeos.app.features.planner.presentation.PlannerViewModel
import com.lifeos.app.features.planner.presentation.TaskDetailViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Planner's Koin module, following the same shape as `features/home/di/HomeModule.kt`
 * and `features/travel/di/TravelModule.kt`. [PlannerRepositoryImpl] replaces
 * the former `FakePlannerRepository` (Iteration 2 of the backend integration
 * migration); [FakePlannerDataSource] is kept as a dependency of
 * [PlannerRepositoryImpl] itself, not removed — Planner Calendar and subtask
 * toggling still have no backend counterpart, see that class's KDoc. Also
 * hosts Task Detail's, Create Task's, and Planner Calendar's dependencies
 * (each extending `features/planner/` rather than a new module).
 */
val plannerModule: Module = module {
    single { FakePlannerDataSource() }
    single { PlannerRemoteDataSource(httpClient = get<HttpClient>()) }
    single<PlannerRepository> {
        PlannerRepositoryImpl(remoteDataSource = get(), fakeDataSource = get())
    }
    factory { GetPlannerDashboardUseCase(plannerRepository = get()) }
    factory { ToggleTaskCompletionUseCase(plannerRepository = get()) }
    factory { GetTaskDetailUseCase(plannerRepository = get()) }
    factory { ToggleSubtaskCompletionUseCase(plannerRepository = get()) }
    factory { DeleteTaskUseCase(plannerRepository = get()) }
    factory { CreateTaskUseCase(plannerRepository = get()) }
    factory { GetCalendarMonthUseCase(plannerRepository = get()) }
    factory { GetTasksForDayUseCase(plannerRepository = get()) }
    viewModel { PlannerViewModel(getPlannerDashboard = get(), toggleTaskCompletion = get()) }
    viewModel { (taskId: String) ->
        TaskDetailViewModel(
            taskId = taskId,
            getTaskDetail = get(),
            toggleTaskCompletion = get(),
            toggleSubtaskCompletion = get(),
            deleteTask = get(),
        )
    }
    viewModel { CreateTaskViewModel(createTask = get()) }
    viewModel { CalendarViewModel(getCalendarMonth = get(), getTasksForDay = get()) }
}
