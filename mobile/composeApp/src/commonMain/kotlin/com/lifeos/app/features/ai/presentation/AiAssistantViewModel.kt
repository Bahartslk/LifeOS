package com.lifeos.app.features.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.ai.domain.model.AiQuotaExceededException
import com.lifeos.app.features.ai.domain.model.DailyReminder
import com.lifeos.app.features.ai.domain.usecase.AddReminderToAgendaUseCase
import com.lifeos.app.features.ai.domain.usecase.BuildAiTaskContextUseCase
import com.lifeos.app.features.ai.domain.usecase.GetDailyBriefUseCase
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
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
 * The AI-generated Daily Brief ([GetDailyBriefUseCase]) loads once when the
 * screen opens and again only when the user taps refresh — never on
 * [AiAssistantEvent.ScreenResumed], since every load costs real AI calls.
 */
class AiAssistantViewModel(
    private val getPlannerDashboard: GetPlannerDashboardUseCase,
    private val buildAiTaskContext: BuildAiTaskContextUseCase,
    private val getDailyBrief: GetDailyBriefUseCase,
    private val addReminderToAgenda: AddReminderToAgendaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    private val _actions = Channel<AiAssistantAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    private var dailyBriefJob: Job? = null

    init {
        loadContext()
        loadDailyBrief(forceRefresh = false)
    }

    fun onEvent(event: AiAssistantEvent) {
        when (event) {
            AiAssistantEvent.RetryClicked -> loadContext()
            // A silent background refresh, not a full reload — see this class's KDoc.
            AiAssistantEvent.ScreenResumed -> loadContext(showLoading = false)
            is AiAssistantEvent.TaskClicked -> sendAction(AiAssistantAction.NavigateToTaskDetail(event.taskId))
            AiAssistantEvent.AddTaskClicked -> sendAction(AiAssistantAction.NavigateToCreateTask)
            AiAssistantEvent.DailyBriefRefreshClicked -> loadDailyBrief(forceRefresh = true)
            is AiAssistantEvent.AddReminderToAgendaClicked -> addReminder(event.reminder)
        }
    }

    /**
     * Saves one AI reminder suggestion as a task. A suggestion that is being
     * saved or was already saved is ignored, so it can never be added twice.
     * On success the task context reloads quietly, so the new task shows up.
     */
    private fun addReminder(reminder: DailyReminder) {
        val brief = _uiState.value.dailyBrief
        if (reminder in brief.addingReminders || reminder in brief.addedReminders) return
        updateDailyBrief { it.copy(addingReminders = it.addingReminders + reminder) }
        viewModelScope.launch {
            addReminderToAgenda(reminder)
                .onSuccess {
                    updateDailyBrief {
                        it.copy(addingReminders = it.addingReminders - reminder, addedReminders = it.addedReminders + reminder)
                    }
                    loadContext(showLoading = false)
                }
                .onFailure {
                    updateDailyBrief { it.copy(addingReminders = it.addingReminders - reminder) }
                    sendAction(AiAssistantAction.ShowMessage(AiAssistantStrings.REMINDER_ADD_ERROR_MESSAGE))
                }
        }
    }

    private fun updateDailyBrief(transform: (DailyBriefUiState) -> DailyBriefUiState) {
        _uiState.update { it.copy(dailyBrief = transform(it.dailyBrief)) }
    }

    private fun loadContext(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            getPlannerDashboard()
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(isLoading = false, context = buildAiTaskContext(dashboard), errorMessage = null)
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = AiAssistantStrings.LOAD_ERROR_MESSAGE)
                    }
                }
        }
    }

    /** Ignores taps while a load is already running, so one tap never sends a second round of AI calls. */
    private fun loadDailyBrief(forceRefresh: Boolean) {
        if (dailyBriefJob?.isActive == true) return
        _uiState.update { it.copy(dailyBrief = it.dailyBrief.copy(isLoading = true, errorMessage = null)) }
        dailyBriefJob = viewModelScope.launch {
            val result = getDailyBrief(forceRefresh)
            _uiState.update { state ->
                val dailyBrief = result.fold(
                    // Keeps which reminders were already added, so a refresh can't re-enable them.
                    onSuccess = { brief -> state.dailyBrief.copy(isLoading = false, brief = brief, errorMessage = null) },
                    // Keep the last brief on screen if a refresh fails.
                    onFailure = { error ->
                        val message = if (error is AiQuotaExceededException) {
                            AiAssistantStrings.AI_QUOTA_EXCEEDED_MESSAGE
                        } else {
                            AiAssistantStrings.BRIEF_ERROR_MESSAGE
                        }
                        state.dailyBrief.copy(isLoading = false, errorMessage = message)
                    },
                )
                state.copy(dailyBrief = dailyBrief)
            }
        }
    }

    private fun sendAction(action: AiAssistantAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
