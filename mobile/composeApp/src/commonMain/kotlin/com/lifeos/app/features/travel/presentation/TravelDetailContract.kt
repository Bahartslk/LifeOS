package com.lifeos.app.features.travel.presentation

import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.travel.domain.model.DocumentType
import com.lifeos.app.features.travel.domain.model.TripDetail

/**
 * [notesText] is tracked separately from [detail]'s own `notes` field
 * (initialized from it on load) so typing is a pure UI-state update, not a
 * mutation of the loaded domain snapshot — the same separation
 * [expandedDayNumbers] gives the Timeline's expand/collapse state, which
 * has no domain representation at all.
 *
 * [relatedTasks] is Planner's own [Task] list, filtered to this trip's
 * `TaskSource.TRAVEL`-tagged tasks — Travel's Planner integration. It is a
 * sibling field, not nested inside [detail], because it comes from a
 * different upstream source (Planner's `GetPlannerDashboardUseCase`) than
 * [detail] (Travel's own `GetTripDetailUseCase`); [TravelDetailViewModel]
 * fetches both and merges them here, the same pattern the Home integration
 * already established. Degrades to an empty list (never fails the whole
 * screen) if Planner's fetch fails — trip preparation tasks are a
 * supplementary section, not core Travel content.
 */
data class TravelDetailUiState(
    val isLoading: Boolean = true,
    val detail: TripDetail? = null,
    val errorMessage: String? = null,
    val isNotFound: Boolean = false,
    val expandedDayNumbers: Set<Int> = emptySet(),
    val notesText: String = "",
    val relatedTasks: List<Task> = emptyList(),
)

sealed interface TravelDetailEvent {
    data object BackClicked : TravelDetailEvent
    data object RetryClicked : TravelDetailEvent
    data object ShareClicked : TravelDetailEvent
    data class DayExpandToggled(val dayNumber: Int) : TravelDetailEvent
    data object ManageBookingClicked : TravelDetailEvent
    data class PackingItemToggled(val itemId: String) : TravelDetailEvent
    data object ViewFullPackingListClicked : TravelDetailEvent
    data class DocumentClicked(val type: DocumentType) : TravelDetailEvent
    data class NotesChanged(val text: String) : TravelDetailEvent
    data class TaskClicked(val taskId: String) : TravelDetailEvent
    data object AddTaskClicked : TravelDetailEvent
}

sealed interface TravelDetailAction {
    data object NavigateBack : TravelDetailAction
    data class NavigateToTaskDetail(val taskId: String) : TravelDetailAction
    data object NavigateToCreateTask : TravelDetailAction
    data class ShowMessage(val message: String) : TravelDetailAction
}
