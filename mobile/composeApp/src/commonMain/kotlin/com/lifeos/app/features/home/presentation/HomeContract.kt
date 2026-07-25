package com.lifeos.app.features.home.presentation

import com.lifeos.app.features.home.domain.model.HomeDashboard
import com.lifeos.app.features.home.domain.model.OverviewStats
import com.lifeos.app.features.home.domain.model.PriorityTask

/**
 * A load-once, display screen — unlike Authentication's form screens, there
 * are no per-field errors here, just the three states any resource-loading
 * screen needs: loading, loaded, or failed. [dashboard]/[overview] and
 * [errorMessage] are never both non-null.
 *
 * [priorities]/[overview] are separate, top-level fields rather than nested
 * inside [dashboard] (this sprint's Planner integration) — they come from a
 * different upstream source ([com.lifeos.app.features.planner.domain.repository.PlannerRepository],
 * via [com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase])
 * than [dashboard]'s own chrome ([com.lifeos.app.features.home.domain.repository.HomeRepository]),
 * so [HomeViewModel] fetches both and combines them here rather than one
 * feature's repository depending on another's.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val greetingMessage: String? = null,
    val dashboard: HomeDashboard? = null,
    val priorities: List<PriorityTask> = emptyList(),
    val overview: OverviewStats? = null,
    val errorMessage: String? = null,
)

sealed interface HomeEvent {
    data object RetryClicked : HomeEvent
    data object ScreenResumed : HomeEvent
    data object SettingsClicked : HomeEvent
    data object CompleteChecklistClicked : HomeEvent
    data object ViewTripClicked : HomeEvent
    data object ManageAllPrioritiesClicked : HomeEvent
    data class QuickActionClicked(val action: HomeQuickAction) : HomeEvent
    data class PriorityTaskClicked(val taskId: String) : HomeEvent
}

sealed interface HomeAction {
    data object NavigateToTravel : HomeAction
    data object NavigateToPlanner : HomeAction
    data object NavigateToAiChat : HomeAction
    data object NavigateToProfile : HomeAction
    data object NavigateToCreateTask : HomeAction
    data class NavigateToTaskDetail(val taskId: String) : HomeAction
    data class ShowMessage(val message: String) : HomeAction
}

/** The four shortcuts in Quick Actions (home.png) — a fixed set, not user data. */
enum class HomeQuickAction {
    NEW_TASK,
    CREATE_TRIP,
    ASK_AI,
    CREATE_NOTE,
}
