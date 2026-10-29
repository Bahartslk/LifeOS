package com.lifeos.app.features.ai.presentation

import com.lifeos.app.features.ai.domain.model.AiQuotaExceededException
import com.lifeos.app.features.ai.domain.model.DailyBrief
import com.lifeos.app.features.ai.domain.model.DailyReminder
import com.lifeos.app.features.ai.domain.model.ReminderRelatedTo
import com.lifeos.app.features.ai.domain.repository.DailyBriefRepository
import com.lifeos.app.features.ai.domain.usecase.AddReminderToAgendaUseCase
import com.lifeos.app.features.ai.domain.usecase.BuildAiTaskContextUseCase
import com.lifeos.app.features.ai.domain.usecase.GetDailyBriefUseCase
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.PlannerOverview
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.repository.PlannerRepository
import com.lifeos.app.features.planner.domain.usecase.CreateTaskUseCase
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AiAssistantViewModelTest {

    private val plannerRepository = StubPlannerRepository()
    private val briefRepository = StubDailyBriefRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsContextAndDailyBrief() = runTest {
        briefRepository.results += Result.success(BRIEF)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNotNull(state.context)
        assertFalse(state.dailyBrief.isLoading)
        assertSame(BRIEF, state.dailyBrief.brief)
        assertEquals(listOf(false), briefRepository.forceRefreshCalls)
    }

    @Test
    fun briefFailure_showsBriefErrorButKeepsTaskContext() = runTest {
        briefRepository.results += Result.failure(RuntimeException("503"))

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNotNull(state.context)
        assertNull(state.errorMessage)
        assertEquals(AiAssistantStrings.BRIEF_ERROR_MESSAGE, state.dailyBrief.errorMessage)
        assertNull(state.dailyBrief.brief)
    }

    @Test
    fun briefQuotaExceeded_showsTheQuotaMessage() = runTest {
        briefRepository.results += Result.failure(AiQuotaExceededException())

        val viewModel = createViewModel()

        assertEquals(AiAssistantStrings.AI_QUOTA_EXCEEDED_MESSAGE, viewModel.uiState.value.dailyBrief.errorMessage)
    }

    @Test
    fun screenResumed_refreshesTasksButNotTheBrief() = runTest {
        briefRepository.results += Result.success(BRIEF)
        val viewModel = createViewModel()

        viewModel.onEvent(AiAssistantEvent.ScreenResumed)

        assertEquals(2, plannerRepository.dashboardCalls)
        assertEquals(1, briefRepository.forceRefreshCalls.size)
        assertSame(BRIEF, viewModel.uiState.value.dailyBrief.brief)
    }

    @Test
    fun refreshClicked_forcesNewBrief_andKeepsOldOneIfItFails() = runTest {
        briefRepository.results += Result.success(BRIEF)
        val viewModel = createViewModel()
        briefRepository.results += Result.failure(RuntimeException("timeout"))

        viewModel.onEvent(AiAssistantEvent.DailyBriefRefreshClicked)

        assertEquals(listOf(false, true), briefRepository.forceRefreshCalls)
        val briefState = viewModel.uiState.value.dailyBrief
        assertSame(BRIEF, briefState.brief)
        assertEquals(AiAssistantStrings.BRIEF_ERROR_MESSAGE, briefState.errorMessage)
    }

    @Test
    fun refreshClicked_whileLoading_isIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        briefRepository.gate = gate
        briefRepository.results += Result.success(BRIEF)
        val viewModel = createViewModel()

        viewModel.onEvent(AiAssistantEvent.DailyBriefRefreshClicked)
        gate.complete(Unit)

        assertEquals(1, briefRepository.forceRefreshCalls.size)
        assertSame(BRIEF, viewModel.uiState.value.dailyBrief.brief)
    }

    @Test
    fun addReminder_success_createsTheTaskAndMarksItAdded() = runTest {
        briefRepository.results += Result.success(BRIEF_WITH_REMINDER)
        val viewModel = createViewModel()

        viewModel.onEvent(AiAssistantEvent.AddReminderToAgendaClicked(REMINDER))

        val request = plannerRepository.createdRequests.single()
        assertEquals(REMINDER.title, request.title)
        assertEquals(REMINDER.recommendedDate, request.dueDate.date)
        assertTrue(request.hasReminder)
        val briefState = viewModel.uiState.value.dailyBrief
        assertEquals(setOf(REMINDER), briefState.addedReminders)
        assertTrue(briefState.addingReminders.isEmpty())
        // The task context reloads so the new task shows up below the card.
        assertEquals(2, plannerRepository.dashboardCalls)
    }

    @Test
    fun addReminder_failure_showsSnackbarAndLetsTheUserTryAgain() = runTest {
        briefRepository.results += Result.success(BRIEF_WITH_REMINDER)
        val viewModel = createViewModel()
        val actions = mutableListOf<AiAssistantAction>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.actions.toList(actions) }
        plannerRepository.createResults += Result.failure(RuntimeException("500"))

        viewModel.onEvent(AiAssistantEvent.AddReminderToAgendaClicked(REMINDER))

        assertEquals(listOf<AiAssistantAction>(AiAssistantAction.ShowMessage(AiAssistantStrings.REMINDER_ADD_ERROR_MESSAGE)), actions)
        val briefState = viewModel.uiState.value.dailyBrief
        assertTrue(briefState.addedReminders.isEmpty())
        assertTrue(briefState.addingReminders.isEmpty())

        viewModel.onEvent(AiAssistantEvent.AddReminderToAgendaClicked(REMINDER))
        assertEquals(2, plannerRepository.createdRequests.size)
        assertEquals(setOf(REMINDER), viewModel.uiState.value.dailyBrief.addedReminders)
    }

    @Test
    fun addReminder_sameSuggestionTwice_createsOnlyOneTask() = runTest {
        briefRepository.results += Result.success(BRIEF_WITH_REMINDER)
        val viewModel = createViewModel()
        val gate = CompletableDeferred<Unit>()
        plannerRepository.createGate = gate

        viewModel.onEvent(AiAssistantEvent.AddReminderToAgendaClicked(REMINDER))
        assertEquals(setOf(REMINDER), viewModel.uiState.value.dailyBrief.addingReminders)
        viewModel.onEvent(AiAssistantEvent.AddReminderToAgendaClicked(REMINDER)) // while saving
        gate.complete(Unit)
        viewModel.onEvent(AiAssistantEvent.AddReminderToAgendaClicked(REMINDER)) // after it was added

        assertEquals(1, plannerRepository.createdRequests.size)
    }

    @Test
    fun addedReminder_staysAddedAfterTheBriefIsRefreshed() = runTest {
        briefRepository.results += Result.success(BRIEF_WITH_REMINDER)
        val viewModel = createViewModel()
        viewModel.onEvent(AiAssistantEvent.AddReminderToAgendaClicked(REMINDER))
        briefRepository.results += Result.success(BRIEF_WITH_REMINDER)

        viewModel.onEvent(AiAssistantEvent.DailyBriefRefreshClicked)
        viewModel.onEvent(AiAssistantEvent.AddReminderToAgendaClicked(REMINDER))

        assertEquals(setOf(REMINDER), viewModel.uiState.value.dailyBrief.addedReminders)
        assertEquals(1, plannerRepository.createdRequests.size)
    }

    private fun createViewModel() = AiAssistantViewModel(
        getPlannerDashboard = GetPlannerDashboardUseCase(plannerRepository),
        buildAiTaskContext = BuildAiTaskContextUseCase(),
        getDailyBrief = GetDailyBriefUseCase(briefRepository),
        addReminderToAgenda = AddReminderToAgendaUseCase(
            createTask = CreateTaskUseCase(plannerRepository),
            today = { TODAY },
        ),
    )

    private class StubDailyBriefRepository : DailyBriefRepository {
        val results = ArrayDeque<Result<DailyBrief>>()
        val forceRefreshCalls = mutableListOf<Boolean>()
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun getDailyBrief(forceRefresh: Boolean): Result<DailyBrief> {
            forceRefreshCalls += forceRefresh
            gate?.await()
            return results.removeFirst()
        }
    }

    private class StubPlannerRepository : PlannerRepository {
        var dashboardCalls = 0

        override suspend fun getDashboard(): Result<PlannerDashboard> {
            dashboardCalls++
            return Result.success(DASHBOARD)
        }

        override suspend fun toggleTaskCompletion(taskId: String): Result<Unit> = error("unused")
        override suspend fun getTaskDetail(taskId: String): Result<TaskDetail> = error("unused")
        override suspend fun toggleSubtaskCompletion(taskId: String, subtaskId: String): Result<Unit> = error("unused")
        override suspend fun deleteTask(taskId: String): Result<Unit> = error("unused")
        val createdRequests = mutableListOf<CreateTaskRequest>()
        val createResults = ArrayDeque<Result<Task>>()
        var createGate: CompletableDeferred<Unit>? = null

        override suspend fun createTask(request: CreateTaskRequest): Result<Task> {
            createdRequests += request
            createGate?.await()
            return createResults.removeFirstOrNull() ?: Result.success(request.toTask())
        }

        private fun CreateTaskRequest.toTask() = Task(
            id = "task-${createdRequests.size}",
            title = title,
            description = description,
            dueDate = dueDate,
            priority = priority,
            category = category,
            status = TaskStatus.TODO,
            source = TaskSource.AI_ASSISTANT,
            tags = tags,
            hasReminder = hasReminder,
            createdAt = TODAY,
        )

        override suspend fun getCalendarMonth(year: Int, month: Int): Result<PlannerCalendarMonth> = error("unused")
        override suspend fun getTasksForDay(date: LocalDate): Result<List<Task>> = error("unused")
    }

    private companion object {
        val TODAY = LocalDate(2026, 10, 28)
        val BRIEF = DailyBrief(summary = null, priorities = emptyList(), motivation = "Başarabilirsin!")
        val REMINDER = DailyReminder(
            title = "Otel rezervasyonunu kontrol et",
            reason = "Seyahatten bir gün önce.",
            relatedTo = ReminderRelatedTo.TRAVEL,
            recommendedDate = LocalDate(2026, 10, 30),
        )
        val BRIEF_WITH_REMINDER = BRIEF.copy(reminders = listOf(REMINDER))

        val DASHBOARD = PlannerDashboard(
            date = LocalDate(2026, 10, 28),
            aiInsightMessage = "",
            overview = PlannerOverview(totalTaskCount = 0, upcomingEventCount = 0, completedTaskCount = 0, productivityPercent = 0),
            calendar = PlannerCalendarMonth(monthLabel = "Ekim 2026", weekdayLabels = emptyList(), days = emptyList()),
            overdueTasks = emptyList(),
            todayTasks = emptyList(),
            upcomingTasks = emptyList(),
        )
    }
}
