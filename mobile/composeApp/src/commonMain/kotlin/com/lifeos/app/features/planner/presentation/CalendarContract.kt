package com.lifeos.app.features.planner.presentation

import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.Task
import kotlinx.datetime.LocalDate

/**
 * [selectedDate] is the real date (within whichever month [calendar]
 * currently displays) the "Daily Agenda" shows — presentation-only derived
 * state, the same "day selection lives in UI state, not the domain model"
 * precedent [com.lifeos.app.features.travel.presentation.TravelDetailUiState.expandedDayNumbers]
 * already established. A real [LocalDate] (this sprint's date refactor,
 * previously a bare day-of-month [Int]) removes the ambiguity a plain day
 * number had between the displayed month's own days and same-numbered
 * leading/trailing days borrowed from adjacent months. [isAgendaLoading]
 * is tracked separately from [isLoading] so paging months and re-selecting
 * a day never force the whole screen back into its full-screen loading
 * state — only the agenda section shows its own loading placeholder.
 */
data class CalendarUiState(
    val isLoading: Boolean = true,
    val calendar: PlannerCalendarMonth? = null,
    val selectedDate: LocalDate? = null,
    val agendaTasks: List<Task> = emptyList(),
    val isAgendaLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface CalendarEvent {
    data object BackClicked : CalendarEvent
    data object RetryClicked : CalendarEvent
    data object PreviousMonthClicked : CalendarEvent
    data object NextMonthClicked : CalendarEvent
    data class DaySelected(val date: LocalDate) : CalendarEvent
    data class TaskClicked(val taskId: String) : CalendarEvent
    data object CreateTaskClicked : CalendarEvent
}

sealed interface CalendarAction {
    data object NavigateBack : CalendarAction
    data class NavigateToTaskDetail(val taskId: String) : CalendarAction
    data object NavigateToCreateTask : CalendarAction
    data class ShowMessage(val message: String) : CalendarAction
}
