package com.lifeos.app.features.home.domain.model

import com.lifeos.app.features.planner.domain.model.TaskPriority

/**
 * One row in "Today's Priority" (home.png), per FR-HOME-01 ("displays the
 * user's tasks due today"). Read-only from Home's perspective: marking a
 * task complete is Planner's responsibility (FR-PLANNER-03), not Home's —
 * tapping a row here navigates to Planner's Task Detail rather than
 * mutating state.
 *
 * A presentation-shaped projection of Planner's [com.lifeos.app.features.planner.domain.model.Task]
 * (this sprint's Planner integration), built fresh by
 * [com.lifeos.app.features.home.presentation.HomeViewModel] on every load —
 * never Home's own stored/duplicated task state. [category]/[dueLabel] are
 * already-formatted Turkish display text (via
 * [com.lifeos.app.core.date.RelativeDateFormatter] for [dueLabel]) rather
 * than [com.lifeos.app.features.planner.domain.model.TaskCategory]/a real
 * date, since [TodaysPrioritiesSection][com.lifeos.app.features.home.presentation.sections.TodaysPrioritiesSection]
 * only ever needs to display them, never branch on them.
 *
 * [priority] reuses Planner's own [TaskPriority] directly rather than a
 * second, identically-shaped Home enum — Planner owns task vocabulary, not
 * just task instances.
 */
data class PriorityTask(
    val id: String,
    val title: String,
    val category: String,
    val dueLabel: String,
    val isCompleted: Boolean,
    val priority: TaskPriority,
)
