package com.lifeos.app.features.ai.domain.usecase

import com.lifeos.app.features.ai.domain.model.DailyReminder
import com.lifeos.app.features.ai.domain.model.ReminderRelatedTo
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.repository.PlannerRepository
import com.lifeos.app.features.planner.domain.usecase.CreateTaskUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddReminderToAgendaUseCaseTest {

    private val repository = RecordingPlannerRepository()
    private val useCase = AddReminderToAgendaUseCase(createTask = CreateTaskUseCase(repository), today = { TODAY })

    @Test
    fun futureDate_isKept_andReasonBecomesDescription() = runTest {
        useCase(reminder(date = LocalDate(2026, 10, 30), relatedTo = ReminderRelatedTo.TRAVEL))

        val request = repository.request
        assertEquals("Otel rezervasyonunu kontrol et", request.title)
        assertEquals("Seyahatten bir gün önce.", request.description)
        assertEquals(LocalDate(2026, 10, 30), request.dueDate.date)
        assertNull(request.dueDate.time)
        assertEquals(TaskPriority.MEDIUM, request.priority)
        assertEquals(TaskCategory.TRAVEL, request.category)
        assertTrue(request.hasReminder)
    }

    @Test
    fun missingOrPastDate_becomesToday() = runTest {
        useCase(reminder(date = null))
        assertEquals(TODAY, repository.request.dueDate.date)

        useCase(reminder(date = LocalDate(2026, 10, 1)))
        assertEquals(TODAY, repository.request.dueDate.date)
    }

    @Test
    fun category_followsWhatTheReminderIsAbout() = runTest {
        useCase(reminder(relatedTo = ReminderRelatedTo.PLANNER))
        assertEquals(TaskCategory.PERSONAL, repository.request.category)

        useCase(reminder(relatedTo = ReminderRelatedTo.BOTH))
        assertEquals(TaskCategory.TRAVEL, repository.request.category)
    }

    @Test
    fun blankReason_leavesNoDescription() = runTest {
        useCase(reminder().copy(reason = ""))

        assertNull(repository.request.description)
    }

    private fun reminder(
        date: LocalDate? = LocalDate(2026, 10, 30),
        relatedTo: ReminderRelatedTo = ReminderRelatedTo.TRAVEL,
    ) = DailyReminder(
        title = "Otel rezervasyonunu kontrol et",
        reason = "Seyahatten bir gün önce.",
        relatedTo = relatedTo,
        recommendedDate = date,
    )

    private class RecordingPlannerRepository : PlannerRepository {
        lateinit var request: CreateTaskRequest

        override suspend fun createTask(request: CreateTaskRequest): Result<Task> {
            this.request = request
            return Result.failure(UnsupportedOperationException("not needed"))
        }

        override suspend fun getDashboard(): Result<PlannerDashboard> = error("unused")
        override suspend fun toggleTaskCompletion(taskId: String): Result<Unit> = error("unused")
        override suspend fun getTaskDetail(taskId: String): Result<TaskDetail> = error("unused")
        override suspend fun toggleSubtaskCompletion(taskId: String, subtaskId: String): Result<Unit> = error("unused")
        override suspend fun deleteTask(taskId: String): Result<Unit> = error("unused")
        override suspend fun getCalendarMonth(year: Int, month: Int): Result<PlannerCalendarMonth> = error("unused")
        override suspend fun getTasksForDay(date: LocalDate): Result<List<Task>> = error("unused")
    }

    private companion object {
        val TODAY = LocalDate(2026, 10, 28)
    }
}
