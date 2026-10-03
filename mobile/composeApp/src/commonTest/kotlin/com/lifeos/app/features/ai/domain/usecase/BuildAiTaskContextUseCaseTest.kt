package com.lifeos.app.features.ai.domain.usecase

import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.PlannerOverview
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildAiTaskContextUseCaseTest {

    private val buildContext = BuildAiTaskContextUseCase()

    @Test
    fun overdueTasks_comeStraightFromTheDashboard() {
        val overdue = listOf(task("late-todo", "2026-09-28"), task("late-active", "2026-09-30", status = TaskStatus.IN_PROGRESS))

        val context = buildContext(dashboard(overdue = overdue))

        assertEquals(overdue, context.overdueTasks)
    }

    @Test
    fun pastDatedTasksInTodayOrUpcoming_areNotReclassifiedAsOverdue() {
        // The backend owns the classification: even a past date in today/upcoming stays where it was put.
        val context = buildContext(
            dashboard(
                today = listOf(task("today-but-past-date", "2020-01-01")),
                upcoming = listOf(task("upcoming-but-past-date", "2020-01-02")),
            ),
        )

        assertEquals(emptyList(), context.overdueTasks)
        assertEquals(listOf("today-but-past-date"), context.todayTasks.map { it.id })
        assertEquals(listOf("upcoming-but-past-date"), context.upcomingTasks.map { it.id })
    }

    @Test
    fun highPriorityTasks_includeOverdueHighTasksExactlyOnce_unfinishedOnly() {
        val context = buildContext(
            dashboard(
                overdue = listOf(task("overdue-high", "2026-09-30", priority = TaskPriority.HIGH), task("overdue-medium", "2026-09-29")),
                today = listOf(
                    task("today-high", "2026-10-02", priority = TaskPriority.HIGH),
                    task("today-high-done", "2026-10-02", priority = TaskPriority.HIGH, status = TaskStatus.DONE),
                ),
                upcoming = listOf(task("upcoming-high", "2026-10-05", priority = TaskPriority.HIGH)),
            ),
        )

        assertEquals(listOf("overdue-high", "today-high", "upcoming-high"), context.highPriorityTasks.map { it.id })
    }

    @Test
    fun travelTasks_stayDrawnFromTodayAndUpcomingOnly() {
        val context = buildContext(
            dashboard(
                overdue = listOf(task("overdue-travel", "2026-09-30", source = TaskSource.TRAVEL)),
                today = listOf(task("today-travel", "2026-10-02", source = TaskSource.TRAVEL), task("today-planner", "2026-10-02")),
                upcoming = listOf(task("upcoming-travel", "2026-10-05", source = TaskSource.TRAVEL, status = TaskStatus.DONE)),
            ),
        )

        assertEquals(listOf("today-travel", "upcoming-travel"), context.travelTasks.map { it.id })
    }

    @Test
    fun progress_comesFromTheDashboardOverview() {
        val progress = buildContext(dashboard()).progress

        assertEquals(3, progress.completedCount)
        assertEquals(5, progress.totalCount)
        assertEquals(60, progress.completionPercent)
    }

    private fun dashboard(
        overdue: List<Task> = emptyList(),
        today: List<Task> = emptyList(),
        upcoming: List<Task> = emptyList(),
    ) = PlannerDashboard(
        date = LocalDate(2026, 10, 2),
        aiInsightMessage = "",
        overview = PlannerOverview(totalTaskCount = 5, upcomingEventCount = 0, completedTaskCount = 3, productivityPercent = 60),
        calendar = PlannerCalendarMonth(monthLabel = "Ekim 2026", weekdayLabels = emptyList(), days = emptyList()),
        overdueTasks = overdue,
        todayTasks = today,
        upcomingTasks = upcoming,
    )

    private fun task(
        id: String,
        dueDate: String,
        priority: TaskPriority = TaskPriority.MEDIUM,
        status: TaskStatus = TaskStatus.TODO,
        source: TaskSource = TaskSource.PLANNER,
    ) = Task(
        id = id,
        title = id,
        description = null,
        dueDate = TaskDueDate(date = LocalDate.parse(dueDate), time = null),
        priority = priority,
        category = TaskCategory.PERSONAL,
        status = status,
        source = source,
        tags = emptyList(),
        hasReminder = false,
        createdAt = LocalDate(2026, 9, 1),
    )
}
