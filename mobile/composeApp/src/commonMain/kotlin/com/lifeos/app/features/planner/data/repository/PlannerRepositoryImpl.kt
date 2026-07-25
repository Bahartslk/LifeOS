package com.lifeos.app.features.planner.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource
import com.lifeos.app.features.planner.data.mapper.toDomain
import com.lifeos.app.features.planner.data.mapper.toDto
import com.lifeos.app.features.planner.data.remote.PlannerRemoteDataSource
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.model.TaskNotFoundException
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.repository.PlannerRepository
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Real implementation of [PlannerRepository], backed by the real
 * `/api/v1/planner` endpoints via [PlannerRemoteDataSource] —
 * replaces the former `FakePlannerRepository` per that class's own
 * "Replacing this with the real backend" plan. [PlannerRepository]'s
 * signature, and every use case/ViewModel that depends on it, are exactly
 * as they were.
 *
 * [getCalendarMonth]/[getTasksForDay] (Planner Calendar) and
 * [toggleSubtaskCompletion] stay backed by [fakeDataSource], unchanged: the
 * backend has no calendar-month endpoint and no subtask model at all, and
 * neither is part of this iteration's required functionality — see the
 * approved mismatch report for the full reasoning.
 */
class PlannerRepositoryImpl(
    private val remoteDataSource: PlannerRemoteDataSource,
    private val fakeDataSource: FakePlannerDataSource,
) : PlannerRepository {

    override suspend fun getDashboard(): Result<PlannerDashboard> = try {
        val dto = remoteDataSource.getDashboard()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        // Reuses the fake data source's pure calendar-grid arithmetic (no
        // backend endpoint exists for it) — its task-count-per-day still
        // reflects the fake data source's own fixed task set, not the real
        // backend tasks above; a known, approved limitation.
        val calendar = fakeDataSource.calendarMonth(today.year, today.monthNumber)
        Result.success(dto.toDomain(date = today, calendar = calendar))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun toggleTaskCompletion(taskId: String): Result<Unit> = try {
        // The interface takes no explicit target state, so the current
        // status must be read first to know whether to call .../complete or
        // .../incomplete.
        val current = remoteDataSource.getTask(taskId)
        if (current.status == TaskStatus.DONE.name) {
            remoteDataSource.markIncomplete(taskId)
        } else {
            remoteDataSource.markComplete(taskId)
        }
        Result.success(Unit)
    } catch (e: ApiException) {
        Result.failure(e.toDomainOrSelf(taskId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTaskDetail(taskId: String): Result<TaskDetail> = try {
        val task = remoteDataSource.getTask(taskId).toDomain()
        Result.success(
            TaskDetail(
                task = task,
                // None of these have a backend counterpart yet (see the
                // approved mismatch report) — left genuinely empty, not
                // synthesized.
                notes = "",
                subtasks = emptyList(),
                attachments = emptyList(),
                activity = emptyList(),
            ),
        )
    } catch (e: ApiException) {
        Result.failure(e.toDomainOrSelf(taskId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun toggleSubtaskCompletion(taskId: String, subtaskId: String): Result<Unit> {
        fakeDataSource.toggleSubtaskCompletion(taskId, subtaskId)
        return Result.success(Unit)
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> = try {
        remoteDataSource.deleteTask(taskId)
        Result.success(Unit)
    } catch (e: ApiException) {
        Result.failure(e.toDomainOrSelf(taskId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createTask(request: CreateTaskRequest): Result<Task> = try {
        val dto = remoteDataSource.createTask(request.toDto())
        Result.success(dto.toDomain(request))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCalendarMonth(year: Int, month: Int): Result<PlannerCalendarMonth> =
        Result.success(fakeDataSource.calendarMonth(year, month))

    override suspend fun getTasksForDay(date: LocalDate): Result<List<Task>> =
        Result.success(fakeDataSource.tasksForDay(date))

    /** A 404 from any per-task endpoint means "not this caller's task" — surfaced as [TaskNotFoundException], same as [com.lifeos.app.features.travel.domain.model.TripNotFoundException]'s precedent. */
    private fun ApiException.toDomainOrSelf(taskId: String): Exception =
        if (statusCode == HttpStatusCode.NotFound.value) TaskNotFoundException(taskId) else this
}
