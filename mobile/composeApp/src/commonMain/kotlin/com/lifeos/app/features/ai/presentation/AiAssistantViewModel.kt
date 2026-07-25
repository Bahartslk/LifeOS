package com.lifeos.app.features.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.ai.domain.usecase.BuildAiTaskContextUseCase
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * The AI Assistant's own ViewModel — a pure consumer of Planner, owning no
 * task data of its own (this integration's requirement). Depends on
 * Planner's existing [GetPlannerDashboardUseCase] (reused directly, no new
 * Planner use case) and this feature's own [BuildAiTaskContextUseCase] (a
 * dependency-free classification step, not a repository) — the exact same
 * `ViewModel -> feature UseCases + Planner UseCases -> merge -> UI`
 * architecture the Home and Travel integrations already established.
 * [com.lifeos.app.features.planner.domain.repository.PlannerRepository]
 * remains the single source of truth; nothing here composes repositories.
 *
 * The [AiAssistantEvent.ScreenResumed]-triggered silent refresh mirrors
 * [com.lifeos.app.features.home.presentation.HomeViewModel]'s exact
 * pattern — this screen persists across tab switches (`navigateToMainTab`
 * saves/restores state rather than recreating the ViewModel), so without
 * it, the assistant would keep reasoning over stale Planner data after the
 * user creates, completes, or deletes a task elsewhere and switches back.
 *
 * Not connected to any AI provider — [BuildAiTaskContextUseCase] only
 * prepares the structured context a future Gemini/OpenAI-backed use case
 * would consume; no network call, no prompt, no response happens here yet.
 */
class AiAssistantViewModel(
    private val getPlannerDashboard: GetPlannerDashboardUseCase,
    private val buildAiTaskContext: BuildAiTaskContextUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    private val _actions = Channel<AiAssistantAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadContext()
    }

    fun onEvent(event: AiAssistantEvent) {
        when (event) {
            AiAssistantEvent.RetryClicked -> loadContext()
            // A silent background refresh, not a full reload — see this class's KDoc.
            AiAssistantEvent.ScreenResumed -> loadContext(showLoading = false)
            is AiAssistantEvent.TaskClicked -> sendAction(AiAssistantAction.NavigateToTaskDetail(event.taskId))
            AiAssistantEvent.AddTaskClicked -> sendAction(AiAssistantAction.NavigateToCreateTask)
        }
    }

    private fun loadContext(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            getPlannerDashboard()
                .onSuccess { dashboard ->
                    _uiState.value = AiAssistantUiState(
                        isLoading = false,
                        context = buildAiTaskContext(dashboard),
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = AiAssistantStrings.LOAD_ERROR_MESSAGE,
                    )
                }
        }
    }

    private fun sendAction(action: AiAssistantAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
