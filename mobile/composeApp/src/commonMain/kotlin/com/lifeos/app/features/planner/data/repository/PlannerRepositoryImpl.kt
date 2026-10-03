package com.lifeos.app.features.planner.data.repository

import com.lifeos.app.core.date.AppToday
import com.lifeos.app.core.network.ApiException
import com.lifeos.app.features.planner.data.dto.TaskDto
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
import com.lifeos.app.features.planner.domain.util.CalendarMonthBuilder
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Real implementation of [PlannerRepository], backed entirely by the real
 * `/api/v1/planner` endpoints via [PlannerRemoteDataSource]. [PlannerRepository]'s
 * signature, and every use case/ViewModel that depends on it, are exactly
 * as they were.
 *
 * Planner Calendar ([getCalendarMonth]/[getTasksForDay], and the dashboard's
 * own month strip) has no dedicated backend endpoint: it reads the user's
 * real tasks through `GET /planner/tasks?dueAfter=&dueBefore=` (paged, see
 * [fetchTasksInRange]) and lays them out with [CalendarMonthBuilder].
 *
 * [toggleSubtaskCompletion] has no backend counterpart (no subtask model
 * exists server-side) — it fails explicitly rather than faking success.
 */
class PlannerRepositoryImpl(
    private val remoteDataSource: PlannerRemoteDataSource,
    /** The device's local date; a parameter only so tests can pin it. */
    private val today: () -> LocalDate = { AppToday.date },
) : PlannerRepository {

    /**
     * The dashboard and the current month's task range are fetched in
     * parallel. Only the dashboard request is required: if the range request
     * fails, the dashboard is still returned with every calendar day's
     * `taskCount` left at 0, rather than failing the whole screen over its
     * smallest widget.
     *
     * "Today" is read once and reused for the dashboard's `?date=`, the
     * calendar month and [PlannerDashboard.date], so all three always agree
     * — even if the call straddles midnight.
     */
    override suspend fun getDashboard(): Result<PlannerDashboard> = try {
        val currentDate = today()
        coroutineScope {
            val monthTasks = async { monthTasksOrNull(currentDate.year, currentDate.monthNumber) }
            val dto = remoteDataSource.getDashboard(currentDate)
            val calendar = CalendarMonthBuilder.build(
                year = currentDate.year,
                month = currentDate.monthNumber,
                tasks = monthTasks.await().orEmpty(),
                today = currentDate,
            )
            Result.success(dto.toDomain(date = currentDate, calendar = calendar))
        }
    } catch (e: CancellationException) {
        throw e
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

    /**
     * Never reachable from real data today — [getTaskDetail] always returns
     * an empty subtask list — but fails explicitly, with no network call,
     * instead of reporting a success that changed nothing.
     */
    override suspend fun toggleSubtaskCompletion(taskId: String, subtaskId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Subtasks are not supported by the backend yet."))

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

    override suspend fun getCalendarMonth(year: Int, month: Int): Result<PlannerCalendarMonth> = try {
        val tasks = fetchMonthTasks(year, month)
        Result.success(CalendarMonthBuilder.build(year = year, month = month, tasks = tasks))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** Ordered by due time, untimed tasks last — the backend orders same-day tasks only by id. */
    override suspend fun getTasksForDay(date: LocalDate): Result<List<Task>> = try {
        val tasks = fetchTasksInRange(dueAfter = date, dueBefore = date).map { it.toDomain() }
        Result.success(tasks.sortedWith(compareBy(nullsLast()) { it.dueDate.time }))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun fetchMonthTasks(year: Int, month: Int): List<Task> {
        val firstOfMonth = LocalDate(year, month, 1)
        val lastOfMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        return fetchTasksInRange(dueAfter = firstOfMonth, dueBefore = lastOfMonth).map { it.toDomain() }
    }

    /** [getDashboard]'s best-effort month fetch: any failure (HTTP, network, mapping) means "no counts", never a failed dashboard. */
    private suspend fun monthTasksOrNull(year: Int, month: Int): List<Task>? = try {
        fetchMonthTasks(year, month)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    /**
     * Every task due within [dueAfter]..[dueBefore] (both inclusive), following
     * `meta.nextCursor` until `hasMore` is false. Stops early — keeping what
     * it already has — if the backend repeats a cursor, reports `hasMore`
     * without a cursor, or [MAX_PAGES] is reached, so a misbehaving response
     * can never loop forever. Throws on the first failed page.
     */
    private suspend fun fetchTasksInRange(dueAfter: LocalDate, dueBefore: LocalDate): List<TaskDto> {
        val tasks = mutableListOf<TaskDto>()
        val seenCursors = mutableSetOf<String>()
        var cursor: String? = null
        for (pageIndex in 0 until MAX_PAGES) {
            val page = remoteDataSource.getTasks(
                dueAfter = dueAfter,
                dueBefore = dueBefore,
                cursor = cursor,
                limit = PAGE_SIZE,
            ).getOrThrow()
            tasks += page.data
            val nextCursor = page.meta.nextCursor
            if (!page.meta.hasMore || nextCursor == null || !seenCursors.add(nextCursor)) break
            cursor = nextCursor
        }
        return tasks
    }

    /** A 404 from any per-task endpoint means "not this caller's task" — surfaced as [TaskNotFoundException], same as [com.lifeos.app.features.travel.domain.model.TripNotFoundException]'s precedent. */
    private fun ApiException.toDomainOrSelf(taskId: String): Exception =
        if (statusCode == HttpStatusCode.NotFound.value) TaskNotFoundException(taskId) else this

    private companion object {
        /** The backend's maximum `limit` for `GET /planner/tasks`. */
        const val PAGE_SIZE = 100

        /** Safety cap: 10 pages of [PAGE_SIZE] is far beyond any real month. */
        const val MAX_PAGES = 10
    }
}
