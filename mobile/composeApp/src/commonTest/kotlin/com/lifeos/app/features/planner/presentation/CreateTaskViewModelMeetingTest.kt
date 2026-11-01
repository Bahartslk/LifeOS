package com.lifeos.app.features.planner.presentation

import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.model.TaskType
import com.lifeos.app.features.planner.domain.repository.PlannerRepository
import com.lifeos.app.features.planner.domain.usecase.CreateTaskUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The Create Task form's type, time, duration, place and reminder rules. */
@OptIn(ExperimentalCoroutinesApi::class)
class CreateTaskViewModelMeetingTest {

    private val repository = RecordingPlannerRepository()
    private val viewModel by lazy { CreateTaskViewModel(CreateTaskUseCase(repository)) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillBasics(dueDate: String = "Yarın") {
        viewModel.onEvent(CreateTaskEvent.TitleChanged("Sprint planlama"))
        viewModel.onEvent(CreateTaskEvent.DueDateChanged(dueDate))
    }

    @Test
    fun choosingMeeting_presetsThe10MinuteReminder_butKeepsAnExplicitChoice() {
        viewModel.onEvent(CreateTaskEvent.TypeSelected(TaskType.MEETING))
        assertEquals(10, viewModel.uiState.value.reminderMinutesBefore)

        viewModel.onEvent(CreateTaskEvent.ReminderSelected(30))
        viewModel.onEvent(CreateTaskEvent.TypeSelected(TaskType.TASK))
        viewModel.onEvent(CreateTaskEvent.TypeSelected(TaskType.MEETING))
        assertEquals(30, viewModel.uiState.value.reminderMinutesBefore)
    }

    @Test
    fun aMeetingWithoutATime_isRejectedWithoutSaving() {
        fillBasics()
        viewModel.onEvent(CreateTaskEvent.TypeSelected(TaskType.MEETING))

        viewModel.onEvent(CreateTaskEvent.SaveClicked)

        assertEquals(PlannerStrings.MEETING_TIME_REQUIRED_ERROR, viewModel.uiState.value.timeError)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun aReminderWithoutATime_isRejectedWithoutSaving() {
        fillBasics()
        viewModel.onEvent(CreateTaskEvent.ReminderSelected(15))

        viewModel.onEvent(CreateTaskEvent.SaveClicked)

        assertEquals(PlannerStrings.REMINDER_TIME_REQUIRED_ERROR, viewModel.uiState.value.timeError)
        assertTrue(repository.requests.isEmpty())
    }

    @Test
    fun aMeetingWithAPickedTime_isSavedWithAllItsFields() = runTest {
        fillBasics()
        viewModel.onEvent(CreateTaskEvent.TypeSelected(TaskType.MEETING))
        viewModel.onEvent(CreateTaskEvent.TimeFieldClicked)
        assertTrue(viewModel.uiState.value.isTimePickerVisible)
        viewModel.onEvent(CreateTaskEvent.TimeSelected(LocalTime(10, 0)))
        viewModel.onEvent(CreateTaskEvent.DurationSelected(60))
        viewModel.onEvent(CreateTaskEvent.LocationChanged("  Ofis  "))

        viewModel.onEvent(CreateTaskEvent.SaveClicked)

        assertEquals(CreateTaskAction.NavigateBack, viewModel.actions.first())
        val request = repository.requests.single()
        assertEquals(TaskType.MEETING, request.type)
        assertEquals(LocalTime(10, 0), request.dueDate.time)
        assertEquals(60, request.durationMinutes)
        assertEquals("Ofis", request.location)
        assertEquals(10, request.reminderMinutesBefore)
        assertEquals(false, viewModel.uiState.value.isTimePickerVisible)
    }

    @Test
    fun aTimeTypedIntoTheDateField_isEnoughForAMeeting_butAPickedTimeWins() = runTest {
        fillBasics(dueDate = "Yarın, 14:00")
        viewModel.onEvent(CreateTaskEvent.TypeSelected(TaskType.MEETING))
        viewModel.onEvent(CreateTaskEvent.SaveClicked)
        assertEquals(LocalTime(14, 0), repository.requests.single().dueDate.time)

        val second = CreateTaskViewModel(CreateTaskUseCase(repository))
        second.onEvent(CreateTaskEvent.TitleChanged("Başka"))
        second.onEvent(CreateTaskEvent.DueDateChanged("Yarın, 14:00"))
        second.onEvent(CreateTaskEvent.TimeSelected(LocalTime(9, 30)))
        second.onEvent(CreateTaskEvent.SaveClicked)
        assertEquals(LocalTime(9, 30), repository.requests.last().dueDate.time)
    }

    @Test
    fun aPlainTask_sendsNoPlace_andNoReminderByDefault() = runTest {
        fillBasics()
        viewModel.onEvent(CreateTaskEvent.LocationChanged("Ofis"))

        viewModel.onEvent(CreateTaskEvent.SaveClicked)

        val request = repository.requests.single()
        assertEquals(TaskType.TASK, request.type)
        assertNull(request.location)
        assertNull(request.reminderMinutesBefore)
    }

    @Test
    fun clearingTheTime_andChangingTheDate_clearTheirStateAndError() {
        fillBasics()
        viewModel.onEvent(CreateTaskEvent.TypeSelected(TaskType.MEETING))
        viewModel.onEvent(CreateTaskEvent.SaveClicked)
        viewModel.onEvent(CreateTaskEvent.TimeSelected(LocalTime(8, 0)))
        assertNull(viewModel.uiState.value.timeError)

        viewModel.onEvent(CreateTaskEvent.TimeCleared)

        assertNull(viewModel.uiState.value.time)
    }
}

/** Records create requests; every other call is unused by the form. */
private class RecordingPlannerRepository : PlannerRepository {
    val requests = mutableListOf<CreateTaskRequest>()

    override suspend fun createTask(request: CreateTaskRequest): Result<Task> {
        requests += request
        return Result.success(
            Task(
                id = "task-${requests.size}",
                title = request.title,
                description = request.description,
                dueDate = request.dueDate,
                priority = request.priority,
                category = request.category,
                status = TaskStatus.TODO,
                source = TaskSource.PLANNER,
                tags = request.tags,
                createdAt = LocalDate(2026, 11, 1),
                type = request.type,
                durationMinutes = request.durationMinutes,
                location = request.location,
                reminderMinutesBefore = request.reminderMinutesBefore,
            ),
        )
    }

    override suspend fun getDashboard(): Result<PlannerDashboard> = error("unused")
    override suspend fun toggleTaskCompletion(taskId: String): Result<Unit> = error("unused")
    override suspend fun getTaskDetail(taskId: String): Result<TaskDetail> = error("unused")
    override suspend fun toggleSubtaskCompletion(taskId: String, subtaskId: String): Result<Unit> = error("unused")
    override suspend fun deleteTask(taskId: String): Result<Unit> = error("unused")
    override suspend fun getCalendarMonth(year: Int, month: Int): Result<PlannerCalendarMonth> = error("unused")
    override suspend fun getTasksForDay(date: LocalDate): Result<List<Task>> = error("unused")
}
