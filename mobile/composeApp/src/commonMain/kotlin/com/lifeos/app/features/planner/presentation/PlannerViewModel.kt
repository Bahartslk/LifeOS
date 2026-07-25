package com.lifeos.app.features.planner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import com.lifeos.app.features.planner.domain.usecase.ToggleTaskCompletionUseCase
import com.lifeos.app.features.planner.presentation.sections.PlannerQuickAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Planner's own ViewModel, following the same shape every other feature's
 * dashboard ViewModel already does
 * ([com.lifeos.app.features.home.presentation.HomeViewModel],
 * [com.lifeos.app.features.travel.presentation.TravelViewModel]). Every
 * event whose real destination is out of this task's scope (AI Assistant)
 * resolves to a "coming soon" message — the same graceful-degradation
 * pattern used throughout this codebase for features not yet built.
 * [PlannerEvent.TaskClicked], [PlannerEvent.CreateTaskClicked], and the
 * Dashboard calendar widget's chevrons/[PlannerQuickAction.VIEW_CALENDAR]
 * are the exceptions: Task Detail, Create Task, and Planner Calendar are
 * now real, so all of them navigate instead. Tapping the Dashboard's own
 * inline calendar chevrons opens the full Calendar screen to actually page
 * months, rather than paging the small preview widget in place — see
 * [com.lifeos.app.features.planner.presentation.sections.PlannerCalendarSection]'s
 * KDoc.
 *
 * [PlannerEvent.ScreenResumed] (Iteration 2.5's "Dashboard refresh after
 * navigation" fix) is dispatched by `PlannerRoute`'s `DisposableEffect` on
 * `ON_RESUME` — the exact same mechanism [com.lifeos.app.features.home.presentation.HomeViewModel]
 * already uses for the same reason: this ViewModel persists across tab
 * switches and across Create Task/Task Detail navigation, so without it,
 * returning here after creating, completing, or deleting a task would keep
 * showing the dashboard snapshot loaded before that change.
 */
class PlannerViewModel(
    private val getPlannerDashboard: GetPlannerDashboardUseCase,
    private val toggleTaskCompletion: ToggleTaskCompletionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    private val _actions = Channel<PlannerAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadDashboard()
    }

    fun onEvent(event: PlannerEvent) {
        when (event) {
            PlannerEvent.RetryClicked -> loadDashboard()
            // A silent background refresh, not a full reload — see HomeViewModel's
            // identical ScreenResumed handling for why (never re-show the loading
            // skeleton over data the user has already seen).
            PlannerEvent.ScreenResumed -> loadDashboard(showLoading = false)
            PlannerEvent.SettingsClicked -> sendAction(PlannerAction.NavigateToProfile)
            is PlannerEvent.TaskToggled -> toggleTask(event.taskId)
            is PlannerEvent.TaskClicked -> sendAction(PlannerAction.NavigateToTaskDetail(event.taskId))
            is PlannerEvent.CategorySelected -> {
                _uiState.value = _uiState.value.copy(selectedCategory = event.category)
            }
            PlannerEvent.UpdatePlanClicked -> showMessage(PlannerStrings.UPDATE_PLAN_COMING_SOON)
            PlannerEvent.PreviousMonthClicked -> sendAction(PlannerAction.NavigateToCalendar)
            PlannerEvent.NextMonthClicked -> sendAction(PlannerAction.NavigateToCalendar)
            is PlannerEvent.QuickActionClicked -> onQuickAction(event.action)
            PlannerEvent.CreateTaskClicked -> sendAction(PlannerAction.NavigateToCreateTask)
        }
    }

    private fun onQuickAction(action: PlannerQuickAction) {
        when (action) {
            PlannerQuickAction.NEW_TASK -> sendAction(PlannerAction.NavigateToCreateTask)
            PlannerQuickAction.VIEW_CALENDAR -> sendAction(PlannerAction.NavigateToCalendar)
            PlannerQuickAction.REMINDERS -> showMessage(PlannerStrings.REMINDERS_COMING_SOON)
            PlannerQuickAction.VIEW_REPORT -> showMessage(PlannerStrings.REPORT_COMING_SOON)
        }
    }

    private fun toggleTask(taskId: String) {
        viewModelScope.launch {
            toggleTaskCompletion(taskId)
                .onSuccess { loadDashboard(showLoading = false) }
                .onFailure { showMessage(PlannerStrings.LOAD_ERROR_MESSAGE) }
        }
    }

    private fun loadDashboard(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            getPlannerDashboard()
                .onSuccess { dashboard ->
                    _uiState.value = _uiState.value.copy(isLoading = false, dashboard = dashboard)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = PlannerStrings.LOAD_ERROR_MESSAGE,
                    )
                }
        }
    }

    private fun showMessage(message: String) {
        sendAction(PlannerAction.ShowMessage(message))
    }

    private fun sendAction(action: PlannerAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
