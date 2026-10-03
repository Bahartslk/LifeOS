package com.lifeos.app.features.planner.presentation

import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.PlannerOverview
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.repository.PlannerRepository
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import com.lifeos.app.features.planner.domain.usecase.ToggleTaskCompletionUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Pins the current [PlannerViewModel] behavior; nothing here changes production code. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelTest {

    private val repository = StubPlannerRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsDashboard() = runTest {
        repository.dashboardResults += Result.success(DASHBOARD)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertSame(DASHBOARD, state.dashboard)
        assertNull(state.errorMessage)
        assertEquals(1, repository.dashboardCalls)
    }

    @Test
    fun init_apiFailure_showsErrorState() = runTest {
        repository.dashboardResults += Result.failure(RuntimeException("network"))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.dashboard)
        assertEquals(PlannerStrings.LOAD_ERROR_MESSAGE, state.errorMessage)
    }

    @Test
    fun retryClicked_showsLoading_thenLoadsDashboard() = runTest {
        repository.dashboardResults += Result.failure(RuntimeException("network"))
        val viewModel = createViewModel()
        val gate = CompletableDeferred<Unit>()
        repository.dashboardGate = gate
        repository.dashboardResults += Result.success(DASHBOARD)

        viewModel.onEvent(PlannerEvent.RetryClicked)
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        gate.complete(Unit)

        assertFalse(viewModel.uiState.value.isLoading)
        assertSame(DASHBOARD, viewModel.uiState.value.dashboard)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(2, repository.dashboardCalls)
    }

    @Test
    fun screenResumed_refreshesSilently() = runTest {
        repository.dashboardResults += Result.success(DASHBOARD)
        val viewModel = createViewModel()
        val gate = CompletableDeferred<Unit>()
        repository.dashboardGate = gate
        repository.dashboardResults += Result.success(UPDATED_DASHBOARD)

        viewModel.onEvent(PlannerEvent.ScreenResumed)
        // No loading skeleton over data the user has already seen.
        assertFalse(viewModel.uiState.value.isLoading)
        assertSame(DASHBOARD, viewModel.uiState.value.dashboard)
        gate.complete(Unit)

        assertSame(UPDATED_DASHBOARD, viewModel.uiState.value.dashboard)
        assertEquals(2, repository.dashboardCalls)
    }

    /**
     * Documents current behavior: a failed silent refresh sets [PlannerUiState.errorMessage]
     * while keeping the previously loaded dashboard in state.
     */
    @Test
    fun screenResumed_failure_keepsDashboard_andSetsError() = runTest {
        repository.dashboardResults += Result.success(DASHBOARD)
        val viewModel = createViewModel()
        repository.dashboardResults += Result.failure(RuntimeException("network"))

        viewModel.onEvent(PlannerEvent.ScreenResumed)

        assertSame(DASHBOARD, viewModel.uiState.value.dashboard)
        assertEquals(PlannerStrings.LOAD_ERROR_MESSAGE, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun taskToggled_success_togglesAndRefreshesDashboard() = runTest {
        repository.dashboardResults += Result.success(DASHBOARD)
        val viewModel = createViewModel()
        repository.dashboardResults += Result.success(UPDATED_DASHBOARD)

        viewModel.onEvent(PlannerEvent.TaskToggled(TASK_ID))

        assertEquals(listOf(TASK_ID), repository.toggledTaskIds)
        assertEquals(2, repository.dashboardCalls)
        assertSame(UPDATED_DASHBOARD, viewModel.uiState.value.dashboard)
    }

    @Test
    fun taskToggled_failure_showsMessage_andDoesNotRefresh() = runTest {
        repository.dashboardResults += Result.success(DASHBOARD)
        val viewModel = createViewModel()
        repository.toggleResult = Result.failure(RuntimeException("network"))

        viewModel.onEvent(PlannerEvent.TaskToggled(TASK_ID))

        assertEquals(PlannerAction.ShowMessage(PlannerStrings.LOAD_ERROR_MESSAGE), viewModel.actions.first())
        assertEquals(1, repository.dashboardCalls)
        assertSame(DASHBOARD, viewModel.uiState.value.dashboard)
    }

    private fun createViewModel() = PlannerViewModel(
        getPlannerDashboard = GetPlannerDashboardUseCase(repository),
        toggleTaskCompletion = ToggleTaskCompletionUseCase(repository),
    )

    private class StubPlannerRepository : PlannerRepository {
        val dashboardResults = ArrayDeque<Result<PlannerDashboard>>()
        var dashboardGate: CompletableDeferred<Unit>? = null
        var dashboardCalls = 0
        var toggleResult: Result<Unit> = Result.success(Unit)
        val toggledTaskIds = mutableListOf<String>()

        override suspend fun getDashboard(): Result<PlannerDashboard> {
            dashboardCalls++
            dashboardGate?.await()
            dashboardGate = null
            return dashboardResults.removeFirst()
        }

        override suspend fun toggleTaskCompletion(taskId: String): Result<Unit> {
            toggledTaskIds += taskId
            return toggleResult
        }

        override suspend fun getTaskDetail(taskId: String): Result<TaskDetail> = error("unused")
        override suspend fun toggleSubtaskCompletion(taskId: String, subtaskId: String): Result<Unit> = error("unused")
        override suspend fun deleteTask(taskId: String): Result<Unit> = error("unused")
        override suspend fun createTask(request: CreateTaskRequest): Result<Task> = error("unused")
        override suspend fun getCalendarMonth(year: Int, month: Int): Result<PlannerCalendarMonth> = error("unused")
        override suspend fun getTasksForDay(date: LocalDate): Result<List<Task>> = error("unused")
    }

    private companion object {
        const val TASK_ID = "task-1"

        private val EMPTY_CALENDAR = PlannerCalendarMonth(monthLabel = "Ekim 2026", weekdayLabels = emptyList(), days = emptyList())

        val DASHBOARD = PlannerDashboard(
            date = LocalDate(2026, 10, 2),
            aiInsightMessage = "",
            overview = PlannerOverview(totalTaskCount = 1, upcomingEventCount = 0, completedTaskCount = 0, productivityPercent = 0),
            calendar = EMPTY_CALENDAR,
            overdueTasks = emptyList(),
            todayTasks = emptyList(),
            upcomingTasks = emptyList(),
        )

        val UPDATED_DASHBOARD = DASHBOARD.copy(
            overview = DASHBOARD.overview.copy(completedTaskCount = 1, productivityPercent = 100),
        )
    }
}
