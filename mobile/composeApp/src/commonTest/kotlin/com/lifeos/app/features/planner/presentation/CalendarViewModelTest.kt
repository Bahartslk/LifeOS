package com.lifeos.app.features.planner.presentation

import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.repository.PlannerRepository
import com.lifeos.app.features.planner.domain.usecase.GetCalendarMonthUseCase
import com.lifeos.app.features.planner.domain.usecase.GetTasksForDayUseCase
import com.lifeos.app.features.planner.domain.util.CalendarMonthBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val repository = StubPlannerRepository()
    private val firstOfCurrentMonth = LocalDate(AppToday.date.year, AppToday.date.monthNumber, 1)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsCurrentMonth() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(listOf(firstOfCurrentMonth.year to firstOfCurrentMonth.monthNumber), repository.monthRequests)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNotNull(state.calendar)
        assertEquals(firstOfCurrentMonth, state.calendar!!.days.first { it.isCurrentMonth }.date)
    }

    @Test
    fun init_selectsToday_andLoadsItsAgenda() = runTest {
        repository.agendaResult = { date -> Result.success(listOf(task("today-task", date))) }

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(AppToday.date, state.selectedDate)
        assertEquals(listOf(AppToday.date), repository.agendaRequests)
        assertEquals(listOf("today-task"), state.agendaTasks.map { it.id })
        assertFalse(state.isAgendaLoading)
    }

    @Test
    fun daySelected_loadsAgendaForThatDay() = runTest {
        val viewModel = createViewModel()
        val day = firstOfCurrentMonth.plus(DatePeriod(days = 9))
        repository.agendaResult = { date -> Result.success(listOf(task("selected", date))) }

        viewModel.onEvent(CalendarEvent.DaySelected(day))

        assertEquals(day, viewModel.uiState.value.selectedDate)
        assertEquals(day, repository.agendaRequests.last())
        assertEquals(listOf("selected"), viewModel.uiState.value.agendaTasks.map { it.id })
    }

    @Test
    fun previousMonthClicked_loadsPreviousMonth_andSelectsItsFirstDay() = runTest {
        val viewModel = createViewModel()
        val previous = firstOfCurrentMonth.plus(DatePeriod(months = -1))

        viewModel.onEvent(CalendarEvent.PreviousMonthClicked)

        assertEquals(previous.year to previous.monthNumber, repository.monthRequests.last())
        assertEquals(previous, viewModel.uiState.value.selectedDate)
        assertEquals(previous, repository.agendaRequests.last())
    }

    @Test
    fun nextMonthClicked_loadsNextMonth_andSelectsItsFirstDay() = runTest {
        val viewModel = createViewModel()
        val next = firstOfCurrentMonth.plus(DatePeriod(months = 1))

        viewModel.onEvent(CalendarEvent.NextMonthClicked)

        assertEquals(next.year to next.monthNumber, repository.monthRequests.last())
        assertEquals(next, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun monthLoadFailure_showsErrorState_andRetryRecovers() = runTest {
        repository.monthFails = true
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE, viewModel.uiState.value.errorMessage)
        assertTrue(repository.agendaRequests.isEmpty())

        repository.monthFails = false
        viewModel.onEvent(CalendarEvent.RetryClicked)

        assertNull(viewModel.uiState.value.errorMessage)
        assertNotNull(viewModel.uiState.value.calendar)
    }

    @Test
    fun agendaLoadFailure_clearsAgenda_andSendsMessage() = runTest {
        repository.agendaResult = { Result.failure(RuntimeException("network")) }

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertNotNull(state.calendar)
        assertTrue(state.agendaTasks.isEmpty())
        assertFalse(state.isAgendaLoading)
        assertEquals(CalendarAction.ShowMessage(PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE), viewModel.actions.first())
    }

    @Test
    fun screenResumed_refreshesSilently_keepingSelectedDay() = runTest {
        val viewModel = createViewModel()
        val day = firstOfCurrentMonth.plus(DatePeriod(days = 4))
        viewModel.onEvent(CalendarEvent.DaySelected(day))
        val gate = CompletableDeferred<Unit>()
        repository.monthGate = gate
        repository.agendaResult = { date -> Result.success(listOf(task("new-task", date))) }

        viewModel.onEvent(CalendarEvent.ScreenResumed)
        // No loading skeleton over a calendar the user is already looking at.
        assertFalse(viewModel.uiState.value.isLoading)
        gate.complete(Unit)

        assertEquals(2, repository.monthRequests.size)
        assertEquals(day, viewModel.uiState.value.selectedDate)
        assertEquals(day, repository.agendaRequests.last())
        assertEquals(listOf("new-task"), viewModel.uiState.value.agendaTasks.map { it.id })
    }

    @Test
    fun screenResumed_failure_keepsCalendar_andSendsMessage() = runTest {
        val viewModel = createViewModel()
        val calendar = viewModel.uiState.value.calendar
        repository.monthFails = true

        viewModel.onEvent(CalendarEvent.ScreenResumed)

        assertEquals(calendar, viewModel.uiState.value.calendar)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(CalendarAction.ShowMessage(PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE), viewModel.actions.first())
    }

    /**
     * M-1 regression: `ON_RESUME` fires on first composition while `init`'s
     * load is still in flight. Had it started a second request that failed
     * (queued below) while the first succeeded, the valid calendar used to
     * end up behind a persistent error state.
     */
    @Test
    fun screenResumed_duringInitialLoad_startsNoSecondRequest_andLoadSucceedsCleanly() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.monthGate = gate
        repository.monthFailureQueue += listOf(false, true)
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.onEvent(CalendarEvent.ScreenResumed)

        assertEquals(1, repository.monthRequests.size)
        assertTrue(viewModel.uiState.value.isLoading)
        gate.complete(Unit)

        val state = viewModel.uiState.value
        assertEquals(1, repository.monthRequests.size)
        assertEquals(1, repository.agendaRequests.size)
        assertFalse(state.isLoading)
        assertNotNull(state.calendar)
        assertNull(state.errorMessage)
    }

    @Test
    fun screenResumed_afterFailedInitialLoad_keepsErrorState_withoutRequest() = runTest {
        repository.monthFails = true
        val viewModel = createViewModel()

        viewModel.onEvent(CalendarEvent.ScreenResumed)

        assertEquals(1, repository.monthRequests.size)
        assertNull(viewModel.uiState.value.calendar)
        assertEquals(PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun screenResumed_afterLoadCompleted_refreshesAgain_onEveryReturn() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(CalendarEvent.ScreenResumed)
        viewModel.onEvent(CalendarEvent.ScreenResumed)

        assertEquals(3, repository.monthRequests.size)
        assertEquals(3, repository.agendaRequests.size)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun screenResumed_refreshFailure_neverReplacesCalendarWithErrorState() = runTest {
        val viewModel = createViewModel()
        val calendar = viewModel.uiState.value.calendar
        val selectedDate = viewModel.uiState.value.selectedDate
        repository.monthFailureQueue += true

        viewModel.onEvent(CalendarEvent.ScreenResumed)

        assertEquals(CalendarAction.ShowMessage(PlannerStrings.CALENDAR_LOAD_ERROR_MESSAGE), viewModel.actions.first())
        assertEquals(calendar, viewModel.uiState.value.calendar)
        assertEquals(selectedDate, viewModel.uiState.value.selectedDate)
        assertNull(viewModel.uiState.value.errorMessage)

        // The next successful return still refreshes normally.
        viewModel.onEvent(CalendarEvent.ScreenResumed)

        assertEquals(3, repository.monthRequests.size)
        assertNotNull(viewModel.uiState.value.calendar)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    private fun createViewModel() = CalendarViewModel(
        getCalendarMonth = GetCalendarMonthUseCase(repository),
        getTasksForDay = GetTasksForDayUseCase(repository),
    )

    private fun task(id: String, date: LocalDate) = Task(
        id = id,
        title = "Görev",
        description = null,
        dueDate = TaskDueDate(date = date, time = null),
        priority = TaskPriority.MEDIUM,
        category = TaskCategory.PERSONAL,
        status = TaskStatus.TODO,
        source = TaskSource.PLANNER,
        tags = emptyList(),
        hasReminder = false,
        createdAt = date,
    )

    private class StubPlannerRepository : PlannerRepository {
        val monthRequests = mutableListOf<Pair<Int, Int>>()
        val agendaRequests = mutableListOf<LocalDate>()
        var monthFails = false

        /** Per-call outcomes (`true` = fail), taken in request order before [monthFails] applies — lets one test make two overlapping requests end differently. */
        val monthFailureQueue = ArrayDeque<Boolean>()
        var monthGate: CompletableDeferred<Unit>? = null
        var agendaResult: (LocalDate) -> Result<List<Task>> = { Result.success(emptyList()) }

        override suspend fun getCalendarMonth(year: Int, month: Int): Result<PlannerCalendarMonth> {
            monthRequests += year to month
            val fails = monthFailureQueue.removeFirstOrNull() ?: monthFails
            monthGate?.await()
            monthGate = null
            if (fails) return Result.failure(RuntimeException("network"))
            return Result.success(CalendarMonthBuilder.build(year = year, month = month, tasks = emptyList()))
        }

        override suspend fun getTasksForDay(date: LocalDate): Result<List<Task>> {
            agendaRequests += date
            return agendaResult(date)
        }

        override suspend fun getDashboard(): Result<PlannerDashboard> = error("unused")
        override suspend fun toggleTaskCompletion(taskId: String): Result<Unit> = error("unused")
        override suspend fun getTaskDetail(taskId: String): Result<TaskDetail> = error("unused")
        override suspend fun toggleSubtaskCompletion(taskId: String, subtaskId: String): Result<Unit> = error("unused")
        override suspend fun deleteTask(taskId: String): Result<Unit> = error("unused")
        override suspend fun createTask(request: CreateTaskRequest): Result<Task> = error("unused")
    }
}
