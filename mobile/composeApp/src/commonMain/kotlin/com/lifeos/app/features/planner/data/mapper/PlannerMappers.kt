package com.lifeos.app.features.planner.data.mapper

import com.lifeos.app.core.date.AppDateFormatter
import com.lifeos.app.features.planner.data.dto.CreateTaskRequestDto
import com.lifeos.app.features.planner.data.dto.PlannerDashboardDto
import com.lifeos.app.features.planner.data.dto.TaskDto
import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.PlannerOverview
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.model.TaskType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Data-layer DTO <-> domain-model mapping, per docs/12-project-architecture.md#repository-pattern
 * and the same convention `features/auth/data/mapper/AuthMappers.kt` already
 * established.
 *
 * `category` and the meeting fields round-trip through real backend
 * columns, the same as `priority`/`status`/`source`. The backend's `Task`
 * still has no `tags` column (a mobile-only enrichment — see [Task]'s own
 * KDoc); [toDomain] fills it with a neutral, non-fake default. No AI content or activity history is synthesized anywhere in
 * this file — per this iteration's explicit instruction, an absent backend
 * value stays absent (empty list / blank string), never a made-up
 * placeholder.
 */
fun TaskDto.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    dueDate = TaskDueDate(
        date = LocalDate.parse(dueDate),
        time = dueTime?.let { LocalTime.parse(it) },
    ),
    priority = TaskPriority.valueOf(priority),
    category = TaskCategory.valueOf(category),
    status = TaskStatus.valueOf(status),
    source = TaskSource.valueOf(source),
    tags = emptyList(),
    // createdAt is a full ISO-8601 instant on the wire; Task.createdAt is a
    // bare LocalDate, so only the date component is kept.
    createdAt = LocalDate.parse(createdAt.substring(0, 10)),
    // An unknown future type reads as a plain task rather than failing the whole list.
    type = TaskType.entries.firstOrNull { it.name == type } ?: TaskType.TASK,
    durationMinutes = durationMinutes,
    location = location,
    reminderMinutesBefore = reminderMinutesBefore,
)

/**
 * [request]'s remaining form-only field ([CreateTaskRequest.tags]) has
 * nowhere to persist server-side — spliced back onto the freshly-created
 * [Task] so it looks exactly as entered for the rest of this session. A later reload from the backend (dashboard refresh, app
 * restart) won't have them anymore, since the server never stored them —
 * approved graceful-degradation behavior, not a bug.
 */
fun TaskDto.toDomain(request: CreateTaskRequest): Task = toDomain().copy(tags = request.tags)

/** [CreateTaskRequest.notes] is dropped here for the same reason `TaskDetail.notes` is always blank when read back — the backend has nowhere to store it. */
fun CreateTaskRequest.toDto(): CreateTaskRequestDto = CreateTaskRequestDto(
    title = title,
    description = description,
    dueDate = dueDate.date.toString(),
    dueTime = dueDate.time?.let { AppDateFormatter.toTimeLabel(it) },
    priority = priority.name,
    category = category.name,
    taskListId = null,
    type = type.name,
    durationMinutes = durationMinutes,
    location = location?.trim()?.ifBlank { null },
    reminderMinutesBefore = reminderMinutesBefore,
)

/**
 * [date]/[calendar] are supplied by the caller rather than derived here:
 * the backend's dashboard response has no `date`/calendar-grid concept of
 * its own (the caller sends its local date as `?date=` and passes the
 * same value here) —
 * [com.lifeos.app.features.planner.data.repository.PlannerRepositoryImpl]
 * computes the real "today" and the calendar grid (from the user's real
 * tasks for the current month) itself and passes both in.
 */
fun PlannerDashboardDto.toDomain(date: LocalDate, calendar: PlannerCalendarMonth): PlannerDashboard = PlannerDashboard(
    date = date,
    // No backend-authored AI insight exists for this dashboard — left blank
    // rather than synthesizing placeholder copy.
    aiInsightMessage = "",
    overview = PlannerOverview(
        totalTaskCount = completedCount + pendingCount,
        upcomingEventCount = upcomingTasks.size,
        completedTaskCount = completedCount,
        productivityPercent = progressPercentage,
    ),
    calendar = calendar,
    overdueTasks = overdueTasks.map { it.toDomain() },
    todayTasks = todayTasks.map { it.toDomain() },
    upcomingTasks = upcomingTasks.map { it.toDomain() },
)
