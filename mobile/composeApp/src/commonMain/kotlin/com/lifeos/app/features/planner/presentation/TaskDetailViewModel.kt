package com.lifeos.app.features.planner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.planner.domain.model.TaskNotFoundException
import com.lifeos.app.features.planner.domain.usecase.DeleteTaskUseCase
import com.lifeos.app.features.planner.domain.usecase.GetTaskDetailUseCase
import com.lifeos.app.features.planner.domain.usecase.ToggleSubtaskCompletionUseCase
import com.lifeos.app.features.planner.domain.usecase.ToggleTaskCompletionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Task Detail's own ViewModel, alongside [PlannerViewModel] in the same
 * feature module — extending `features/planner/` rather than a new module,
 * per this task's scope. [taskId] is supplied at creation time (via Koin's
 * parameter injection in `di/PlannerModule.kt`), the same way
 * [com.lifeos.app.features.travel.presentation.TravelDetailViewModel]
 * receives its `tripId`.
 *
 * [toggleTaskCompletion] is the exact same use case
 * [PlannerViewModel] already uses for the Dashboard's task-card checkbox —
 * reused here for the "Mark Complete" button rather than a duplicate.
 */
class TaskDetailViewModel(
    private val taskId: String,
    private val getTaskDetail: GetTaskDetailUseCase,
    private val toggleTaskCompletion: ToggleTaskCompletionUseCase,
    private val toggleSubtaskCompletion: ToggleSubtaskCompletionUseCase,
    private val deleteTask: DeleteTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _actions = Channel<TaskDetailAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadDetail()
    }

    fun onEvent(event: TaskDetailEvent) {
        when (event) {
            TaskDetailEvent.BackClicked -> sendAction(TaskDetailAction.NavigateBack)
            TaskDetailEvent.RetryClicked -> loadDetail()
            TaskDetailEvent.CompleteToggleClicked -> toggleCompletion()
            is TaskDetailEvent.SubtaskToggled -> toggleSubtask(event.subtaskId)
            is TaskDetailEvent.NotesChanged -> {
                _uiState.value = _uiState.value.copy(notesText = event.text)
            }
            TaskDetailEvent.AddAttachmentClicked -> showMessage(PlannerStrings.ATTACHMENT_COMING_SOON)
            TaskDetailEvent.ViewRelatedTripClicked -> showMessage(PlannerStrings.RELATED_TRIP_COMING_SOON)
            TaskDetailEvent.ViewAiSuggestionsClicked -> showMessage(PlannerStrings.AI_SUGGESTIONS_COMING_SOON)
            TaskDetailEvent.DeleteClicked -> {
                _uiState.value = _uiState.value.copy(isDeleteConfirmationVisible = true)
            }
            TaskDetailEvent.DeleteDismissed -> {
                _uiState.value = _uiState.value.copy(isDeleteConfirmationVisible = false)
            }
            TaskDetailEvent.DeleteConfirmed -> confirmDelete()
        }
    }

    private fun toggleCompletion() {
        viewModelScope.launch {
            toggleTaskCompletion(taskId)
                .onSuccess { loadDetail(showLoading = false) }
                .onFailure { showMessage(PlannerStrings.DETAIL_LOAD_ERROR_MESSAGE) }
        }
    }

    private fun toggleSubtask(subtaskId: String) {
        viewModelScope.launch {
            toggleSubtaskCompletion(taskId, subtaskId)
                .onSuccess { loadDetail(showLoading = false) }
                .onFailure { showMessage(PlannerStrings.DETAIL_LOAD_ERROR_MESSAGE) }
        }
    }

    /**
     * On success, only [TaskDetailAction.NavigateBack] is sent — not also a
     * "task deleted" [TaskDetailAction.ShowMessage] — since
     * `CollectActions`'s `collectLatest` would cancel the still-suspended
     * snackbar the instant the navigation action arrives right behind it.
     * The task's disappearance from the Planner Dashboard list is
     * confirmation enough.
     */
    private fun confirmDelete() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleteConfirmationVisible = false)
            deleteTask(taskId)
                .onSuccess { sendAction(TaskDetailAction.NavigateBack) }
                .onFailure { showMessage(PlannerStrings.DETAIL_LOAD_ERROR_MESSAGE) }
        }
    }

    private fun loadDetail(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, isNotFound = false)
            }
            getTaskDetail(taskId)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        detail = detail,
                        // Only re-seed notesText on the *initial* load — a background
                        // refresh triggered by a completion/subtask toggle must never
                        // clobber notes the user is still mid-edit on.
                        notesText = if (showLoading) detail.notes else _uiState.value.notesText,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = if (throwable is TaskNotFoundException) {
                        _uiState.value.copy(isLoading = false, isNotFound = true)
                    } else {
                        _uiState.value.copy(isLoading = false, errorMessage = PlannerStrings.DETAIL_LOAD_ERROR_MESSAGE)
                    }
                }
        }
    }

    private fun showMessage(message: String) {
        sendAction(TaskDetailAction.ShowMessage(message))
    }

    private fun sendAction(action: TaskDetailAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
