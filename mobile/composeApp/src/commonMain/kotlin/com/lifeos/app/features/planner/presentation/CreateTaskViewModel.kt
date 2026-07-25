package com.lifeos.app.features.planner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.core.date.AppDateParser
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.usecase.CreateTaskUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * "Create Task"'s own ViewModel, alongside [PlannerViewModel] and
 * [TaskDetailViewModel] in the same feature module — extending
 * `features/planner/` rather than a new module, per this task's scope.
 *
 * Depends only on [CreateTaskUseCase], which depends on the
 * [com.lifeos.app.features.planner.domain.repository.PlannerRepository]
 * *interface* — this class never imports anything from `data`. [RepeatOption]
 * never leaves this class: it is read from [CreateTaskUiState] and written
 * back to it, but never appears in the [CreateTaskRequest] built for saving,
 * per this task's "Repeat Option is UI only" requirement.
 *
 * [save] is also where [CreateTaskUiState.dueDate]'s free-typed text stops
 * being a string — [AppDateParser] turns it into a real
 * [com.lifeos.app.features.planner.domain.model.TaskDueDate] before
 * [CreateTaskRequest] is ever built, so nothing downstream of this
 * ViewModel (the domain, `data`, or a future backend) ever sees a
 * formatted date string again. An unparseable value surfaces as the same
 * field-level `dueDateError` a blank value already did.
 */
class CreateTaskViewModel(
    private val createTask: CreateTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    private val _actions = Channel<CreateTaskAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: CreateTaskEvent) {
        when (event) {
            CreateTaskEvent.BackClicked -> sendAction(CreateTaskAction.NavigateBack)
            is CreateTaskEvent.TitleChanged -> update { it.copy(title = event.value, titleError = null) }
            is CreateTaskEvent.DescriptionChanged -> update { it.copy(description = event.value) }
            is CreateTaskEvent.DueDateChanged -> update { it.copy(dueDate = event.value, dueDateError = null) }
            is CreateTaskEvent.ReminderToggled -> update { it.copy(hasReminder = event.enabled) }
            is CreateTaskEvent.PrioritySelected -> update { it.copy(priority = event.priority) }
            is CreateTaskEvent.CategorySelected -> update { it.copy(category = event.category) }
            is CreateTaskEvent.TagInputChanged -> update { it.copy(tagInput = event.value) }
            CreateTaskEvent.TagAdded -> addTag()
            is CreateTaskEvent.TagRemoved -> update { it.copy(tags = it.tags - event.tag) }
            is CreateTaskEvent.EstimatedDurationChanged -> update { it.copy(estimatedDuration = event.value) }
            is CreateTaskEvent.RepeatOptionSelected -> update { it.copy(repeatOption = event.option) }
            is CreateTaskEvent.NotesChanged -> update { it.copy(notes = event.value) }
            CreateTaskEvent.SaveClicked -> save()
        }
    }

    private fun addTag() {
        val tag = _uiState.value.tagInput.trim()
        if (tag.isEmpty() || tag in _uiState.value.tags) {
            update { it.copy(tagInput = "") }
            return
        }
        update { it.copy(tags = it.tags + tag, tagInput = "") }
    }

    private fun save() {
        val state = _uiState.value
        val titleError = if (state.title.isBlank()) PlannerStrings.TITLE_REQUIRED_ERROR else null
        val parsedDueDate = if (state.dueDate.isBlank()) null else AppDateParser.parse(state.dueDate)
        val dueDateError = when {
            state.dueDate.isBlank() -> PlannerStrings.DUE_DATE_REQUIRED_ERROR
            parsedDueDate == null -> PlannerStrings.DUE_DATE_INVALID_FORMAT_ERROR
            else -> null
        }
        if (titleError != null || parsedDueDate == null) {
            update { it.copy(titleError = titleError, dueDateError = dueDateError) }
            return
        }

        viewModelScope.launch {
            update { it.copy(isSaving = true) }
            val dueDate = TaskDueDate(date = parsedDueDate.first, time = parsedDueDate.second)
            createTask(state.toRequest(dueDate))
                .onSuccess {
                    update { it.copy(isSaving = false) }
                    sendAction(CreateTaskAction.NavigateBack)
                }
                .onFailure {
                    update { it.copy(isSaving = false) }
                    sendAction(CreateTaskAction.ShowMessage(PlannerStrings.CREATE_TASK_SAVE_ERROR_MESSAGE))
                }
        }
    }

    private fun update(transform: (CreateTaskUiState) -> CreateTaskUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private fun sendAction(action: CreateTaskAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}

private fun CreateTaskUiState.toRequest(dueDate: TaskDueDate): CreateTaskRequest = CreateTaskRequest(
    title = title.trim(),
    description = description.trim().ifBlank { null },
    dueDate = dueDate,
    priority = priority,
    category = category,
    tags = tags,
    hasReminder = hasReminder,
    estimatedDurationLabel = estimatedDuration.trim().ifBlank { null },
    notes = notes.trim().ifBlank { null },
)
