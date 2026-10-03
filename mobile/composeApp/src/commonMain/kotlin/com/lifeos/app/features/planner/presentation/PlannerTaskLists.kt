package com.lifeos.app.features.planner.presentation

import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory

/**
 * The three task lists [PlannerScreen] renders, after the "Quick
 * Categories" filter — presentation-layer derived state that never
 * mutates the dashboard's own lists. Pulled out of the Composable so the
 * filtering and the empty-screen decision are plain, testable functions.
 */
internal data class PlannerTaskLists(
    val overdue: List<Task>,
    val today: List<Task>,
    val upcoming: List<Task>,
)

internal fun PlannerDashboard.taskListsFor(category: TaskCategory?): PlannerTaskLists = PlannerTaskLists(
    overdue = overdueTasks.filterByCategory(category),
    today = todayTasks.filterByCategory(category),
    upcoming = upcomingTasks.filterByCategory(category),
)

/**
 * Whether the dashboard has any task to show at all — the screen-wide
 * "no tasks yet" empty state is only for a user with nothing overdue,
 * nothing today and nothing upcoming.
 */
internal fun plannerHasAnyTasks(dashboard: PlannerDashboard): Boolean =
    dashboard.overdueTasks.isNotEmpty() || dashboard.todayTasks.isNotEmpty() || dashboard.upcomingTasks.isNotEmpty()

private fun List<Task>.filterByCategory(category: TaskCategory?): List<Task> =
    if (category == null) this else filter { it.category == category }
