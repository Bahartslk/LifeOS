package com.lifeos.app.features.planner.domain.repository

import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskDetail
import kotlinx.datetime.LocalDate

/**
 * Abstracts fetching and mutating Planner data, per
 * docs/12-project-architecture.md#repository-pattern. ViewModels depend
 * only on this interface; [com.lifeos.app.features.planner.data.repository.FakePlannerRepository]
 * is bound in `di/PlannerModule.kt` today and is the only thing that
 * changes when a real backend exists — see its KDoc for the replacement
 * plan.
 *
 * [toggleTaskCompletion], [toggleSubtaskCompletion], [deleteTask], and
 * [createTask] all round-trip through the repository (rather than a purely
 * local UI-state flip) because Planner is this app's source of truth for
 * tasks: once Home reads its "Today's Priorities" from here too, a change
 * made from any screen must be reflected everywhere a task is displayed.
 * Extended here for Task Detail and Create Task rather than a new
 * repository, per each task's "reuse PlannerRepository" scope.
 *
 * [getCalendarMonth]/[getTasksForDay] are Planner Calendar's own additions —
 * still no new repository, and [getTasksForDay] returns the exact same
 * [Task] type every other screen already uses, never a `CalendarTask` or
 * `CalendarEvent`: the calendar is simply another view over the same task
 * list [getDashboard] and [getTaskDetail] already expose. [getTasksForDay]
 * takes a single real [LocalDate] (this sprint's refactor simplified it
 * from three separate `year`/`month`/`day` ints) since [Task.dueDate] is
 * now a real date too — matching is a plain equality check, not string or
 * manual (year, month, day) matching.
 */
interface PlannerRepository {
    suspend fun getDashboard(): Result<PlannerDashboard>

    suspend fun toggleTaskCompletion(taskId: String): Result<Unit>

    suspend fun getTaskDetail(taskId: String): Result<TaskDetail>

    suspend fun toggleSubtaskCompletion(taskId: String, subtaskId: String): Result<Unit>

    suspend fun deleteTask(taskId: String): Result<Unit>

    suspend fun createTask(request: CreateTaskRequest): Result<Task>

    suspend fun getCalendarMonth(year: Int, month: Int): Result<PlannerCalendarMonth>

    suspend fun getTasksForDay(date: LocalDate): Result<List<Task>>
}
