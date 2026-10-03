package com.lifeos.app.features.planner.presentation

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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlannerTaskListsTest {

    @Test
    fun plannerHasAnyTasks_isFalseOnlyWhenAllThreeListsAreEmpty() {
        assertFalse(plannerHasAnyTasks(dashboard()))
    }

    @Test
    fun plannerHasAnyTasks_isTrueWithOnlyOverdueTasks() {
        // Regression: a user with nothing today/upcoming but an overdue task must not see "no tasks yet".
        assertTrue(plannerHasAnyTasks(dashboard(overdue = listOf(task("late")))))
    }

    @Test
    fun plannerHasAnyTasks_isTrueWithOnlyTodayOrUpcomingTasks() {
        assertTrue(plannerHasAnyTasks(dashboard(today = listOf(task("now")))))
        assertTrue(plannerHasAnyTasks(dashboard(upcoming = listOf(task("later")))))
    }

    @Test
    fun taskListsFor_withoutCategory_keepsEveryListAndItsOrder() {
        val dashboard = dashboard(
            overdue = listOf(task("late-1"), task("late-2")),
            today = listOf(task("now")),
            upcoming = listOf(task("later")),
        )

        val lists = dashboard.taskListsFor(category = null)

        assertEquals(listOf("late-1", "late-2"), lists.overdue.map { it.id })
        assertEquals(listOf("now"), lists.today.map { it.id })
        assertEquals(listOf("later"), lists.upcoming.map { it.id })
    }

    @Test
    fun taskListsFor_appliesTheCategoryFilterToOverdueTasksToo() {
        val dashboard = dashboard(
            overdue = listOf(task("late-work", TaskCategory.WORK), task("late-finance", TaskCategory.FINANCE)),
            today = listOf(task("now-work", TaskCategory.WORK), task("now-health", TaskCategory.HEALTH)),
            upcoming = listOf(task("later-finance", TaskCategory.FINANCE)),
        )

        val lists = dashboard.taskListsFor(TaskCategory.WORK)

        assertEquals(listOf("late-work"), lists.overdue.map { it.id })
        assertEquals(listOf("now-work"), lists.today.map { it.id })
        assertEquals(emptyList(), lists.upcoming)
    }

    @Test
    fun taskListsFor_canFilterOverdueDownToEmpty_soTheSectionIsNotRendered() {
        val lists = dashboard(overdue = listOf(task("late-finance", TaskCategory.FINANCE))).taskListsFor(TaskCategory.HEALTH)

        assertEquals(emptyList(), lists.overdue)
    }

    private fun dashboard(
        overdue: List<Task> = emptyList(),
        today: List<Task> = emptyList(),
        upcoming: List<Task> = emptyList(),
    ) = PlannerDashboard(
        date = LocalDate(2026, 10, 2),
        aiInsightMessage = "",
        overview = PlannerOverview(totalTaskCount = 0, upcomingEventCount = 0, completedTaskCount = 0, productivityPercent = 0),
        calendar = PlannerCalendarMonth(monthLabel = "Ekim 2026", weekdayLabels = emptyList(), days = emptyList()),
        overdueTasks = overdue,
        todayTasks = today,
        upcomingTasks = upcoming,
    )

    private fun task(id: String, category: TaskCategory = TaskCategory.PERSONAL) = Task(
        id = id,
        title = id,
        description = null,
        dueDate = TaskDueDate(date = LocalDate(2026, 10, 2), time = null),
        priority = TaskPriority.MEDIUM,
        category = category,
        status = TaskStatus.TODO,
        source = TaskSource.PLANNER,
        tags = emptyList(),
        hasReminder = false,
        createdAt = LocalDate(2026, 9, 1),
    )
}
