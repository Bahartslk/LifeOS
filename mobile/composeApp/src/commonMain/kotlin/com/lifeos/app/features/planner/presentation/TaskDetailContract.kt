package com.lifeos.app.features.planner.presentation

import com.lifeos.app.features.planner.domain.model.TaskDetail

/**
 * [notesText] is tracked separately from [detail]'s own `notes` field
 * (initialized from it on load) so typing is a pure UI-state update, not a
 * mutation of the loaded domain snapshot — the same separation
 * [com.lifeos.app.features.travel.presentation.TravelDetailUiState.notesText]
 * already established for Travel Detail. [isDeleteConfirmationVisible]
 * gates the [com.lifeos.app.core.designsystem.components.ConfirmationDialog]
 * shown by `TaskDetailScreen.kt`.
 */
data class TaskDetailUiState(
    val isLoading: Boolean = true,
    val detail: TaskDetail? = null,
    val errorMessage: String? = null,
    val isNotFound: Boolean = false,
    val notesText: String = "",
    val isDeleteConfirmationVisible: Boolean = false,
)

sealed interface TaskDetailEvent {
    data object BackClicked : TaskDetailEvent
    data object RetryClicked : TaskDetailEvent
    data object CompleteToggleClicked : TaskDetailEvent
    data class SubtaskToggled(val subtaskId: String) : TaskDetailEvent
    data class NotesChanged(val text: String) : TaskDetailEvent
    data object AddAttachmentClicked : TaskDetailEvent
    data object ViewRelatedTripClicked : TaskDetailEvent
    data object ViewAiSuggestionsClicked : TaskDetailEvent
    data object DeleteClicked : TaskDetailEvent
    data object DeleteConfirmed : TaskDetailEvent
    data object DeleteDismissed : TaskDetailEvent
}

sealed interface TaskDetailAction {
    data object NavigateBack : TaskDetailAction
    data class ShowMessage(val message: String) : TaskDetailAction
}
