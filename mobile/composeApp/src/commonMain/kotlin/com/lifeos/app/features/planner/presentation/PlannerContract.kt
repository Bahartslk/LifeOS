package com.lifeos.app.features.planner.presentation

import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.presentation.sections.PlannerQuickAction

/**
 * A load-once, display screen — like [com.lifeos.app.features.home.presentation.HomeUiState],
 * there are no per-field form errors here, just loading/loaded/failed plus
 * [selectedCategory], the "Quick Categories" UI-only filter (applied to
 * [dashboard]'s task lists in `PlannerScreen.kt`, the same "derived state
 * lives in presentation" precedent every other feature already follows).
 */
data class PlannerUiState(
    val isLoading: Boolean = true,
    val dashboard: PlannerDashboard? = null,
    val errorMessage: String? = null,
    val selectedCategory: TaskCategory? = null,
)

sealed interface PlannerEvent {
    data object RetryClicked : PlannerEvent
    /** Dispatched by `PlannerRoute`'s `DisposableEffect` on `ON_RESUME` — see [PlannerViewModel]'s KDoc. */
    data object ScreenResumed : PlannerEvent
    data object SettingsClicked : PlannerEvent
    data class TaskToggled(val taskId: String) : PlannerEvent
    data class TaskClicked(val taskId: String) : PlannerEvent
    data class CategorySelected(val category: TaskCategory?) : PlannerEvent
    data object UpdatePlanClicked : PlannerEvent
    data object PreviousMonthClicked : PlannerEvent
    data object NextMonthClicked : PlannerEvent
    data class QuickActionClicked(val action: PlannerQuickAction) : PlannerEvent
    data object CreateTaskClicked : PlannerEvent
}

sealed interface PlannerAction {
    data object NavigateToProfile : PlannerAction
    data class NavigateToTaskDetail(val taskId: String) : PlannerAction
    data object NavigateToCreateTask : PlannerAction
    data object NavigateToCalendar : PlannerAction
    data class ShowMessage(val message: String) : PlannerAction
}
