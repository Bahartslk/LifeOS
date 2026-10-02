package com.lifeos.app.features.planner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.planner.domain.usecase.GetCalendarMonthUseCase
import com.lifeos.app.features.planner.domain.usecase.GetTasksForDayUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Planner Calendar's own ViewModel, alongside [PlannerViewModel],
 * [TaskDetailViewModel], and [CreateTaskViewModel] in the same feature
 * module — extending `features/planner/` rather than a new module, per
 * this task's scope.
 *
 * Depends only on [GetCalendarMonthUseCase]/[GetTasksForDayUseCase], both
 * of which depend on the [com.lifeos.app.features.planner.domain.repository.PlannerRepository]
 * *interface* — this class never imports anything from `data`, and never
 * mutates a task itself (no toggle/delete here — Calendar only reads,
 * per this task's "Calendar should never own Task state" requirement;
 * completion toggling stays on the Dashboard and Task Detail).
 *
 * [displayedMonth] is a private, plain mutable field — the same "no
 * month leaks into [CalendarUiState]" choice this feature made
 * deliberately: the Screen only ever needs [CalendarUiState.calendar]'s
 * already-formatted `monthLabel`, never a raw month value, so there is
 * nothing for the UI state to carry. It's always the 1st of some month —
 * a plain [LocalDate] rather than a separate year/month pair (this
 * sprint's date refactor) so paging months is real
 * [kotlinx.datetime] arithmetic ([DatePeriod]) instead of hand-rolled
 * month/year rollover math.
 *
 * [CalendarEvent.ScreenResumed] is dispatched by `CalendarRoute` on
 * `ON_RESUME` — the same mechanism `PlannerRoute` uses — so a task created
 * from this screen's FAB (or edited/deleted in Task Detail) shows up as
 * soon as the user navigates back, without re-showing the loading skeleton.
 */
class CalendarViewModel(
    private val getCalendarMonth: GetCalendarMonthUseCase,
    private val getTasksForDay: GetTasksForDayUseCase,
) : ViewModel() {

    private var displayedMonth = LocalDate(AppToday.date.year, AppToday.date.monthNumber, 1)

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _actions = Channel<CalendarAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadMonth()
    }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            CalendarEvent.BackClicked -> sendAction(CalendarAction.NavigateBack)
            CalendarEvent.RetryClicked -> loadMonth()
            CalendarEvent.ScreenResumed -> refreshMonth()
            CalendarEvent.PreviousMonthClicked -> {
                displayedMonth = displayedMonth.plus(DatePeriod(months = -1))
                loadMonth()
            }
            CalendarEvent.NextMonthClicked -> {
                displayedMonth = displayedMonth.plus(DatePeriod(months = 1))
                loadMonth()
            }
            is CalendarEvent.DaySelected -> selectDay(event.date)
            is CalendarEvent.TaskClicked -> sendAction(CalendarAction.NavigateToTaskDetail(event.taskId))
            CalendarEvent.CreateTaskClicked -> sendAction(CalendarAction.NavigateToCreateTask)
        }
    }

    private fun loadMonth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            getCalendarMonth(displayedMonth.year, displayedMonth.monthNumber)
                .onSuccess { calendar ->
                    val defaultDate = calendar.days.firstOrNull { it.isToday && it.isCurrentMonth }?.date ?: displayedMonth
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        calendar = calendar,
                        selectedDate = defaultDate,
                        errorMessage = null,
                    )
                    loadAgenda(defaultDate)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE,
                    )
                }
        }
    }

    /**
     * A silent reload of the displayed month that keeps the user's selected
     * day. Skipped while [loadMonth] is still running or nothing has loaded
     * yet: `ON_RESUME` also fires on first composition, right after `init`'s
     * [loadMonth] started, and a second parallel request would only race it
     * (a failed initial load already offers Retry). If the refresh fails,
     * the calendar on screen stays and only a snackbar is shown.
     */
    private fun refreshMonth() {
        val state = _uiState.value
        if (state.isLoading || state.calendar == null) return
        viewModelScope.launch {
            getCalendarMonth(displayedMonth.year, displayedMonth.monthNumber)
                .onSuccess { calendar ->
                    val selectedDate = _uiState.value.selectedDate
                        ?.takeIf { date -> calendar.days.any { it.date == date && it.isCurrentMonth } }
                        ?: calendar.days.firstOrNull { it.isToday && it.isCurrentMonth }?.date
                        ?: displayedMonth
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        calendar = calendar,
                        selectedDate = selectedDate,
                        errorMessage = null,
                    )
                    loadAgenda(selectedDate, showLoading = false)
                }
                .onFailure {
                    sendAction(CalendarAction.ShowMessage(PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE))
                }
        }
    }

    private fun selectDay(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadAgenda(date)
    }

    private fun loadAgenda(date: LocalDate, showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isAgendaLoading = true)
            }
            getTasksForDay(date)
                .onSuccess { tasks ->
                    _uiState.value = _uiState.value.copy(isAgendaLoading = false, agendaTasks = tasks)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isAgendaLoading = false, agendaTasks = emptyList())
                    sendAction(CalendarAction.ShowMessage(PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE))
                }
        }
    }

    private fun sendAction(action: CalendarAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
