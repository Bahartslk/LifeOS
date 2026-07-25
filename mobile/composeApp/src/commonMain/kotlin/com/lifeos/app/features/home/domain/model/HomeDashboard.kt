package com.lifeos.app.features.home.domain.model

/**
 * The aggregate root for Home's own chrome — everything Home displays that
 * Planner does not own. Mirrors a future `GET /api/v1/dashboard`-style BFF
 * response for this reduced scope — see
 * [com.lifeos.app.features.home.data.repository.FakeHomeRepository] for the
 * plan to replace this with a real network call.
 *
 * [overview]/[priorities] deliberately do NOT live here anymore (this
 * sprint's Planner integration) — Planner is the single source of truth for
 * every task, so [com.lifeos.app.features.home.presentation.HomeViewModel]
 * builds Home's "Today's Priorities" and task-count stats directly from
 * [com.lifeos.app.features.planner.domain.model.PlannerDashboard] instead of
 * this class carrying its own (necessarily fake, necessarily duplicated)
 * copy. [taskTrend] is the one piece of "Overview" data that genuinely stays
 * Home's own — see [TaskTrend]'s KDoc.
 *
 * [upcomingJourney] is nullable: a user with no planned trips is a real,
 * expected state (docs/08-non-functional-requirements.md#usability's
 * "informative, not generic" empty states), not an error.
 */
data class HomeDashboard(
    val greeting: GreetingInfo,
    val intelligentHub: IntelligentHubSummary,
    val taskTrend: TaskTrend,
    val upcomingJourney: UpcomingJourney?,
)

/** Who to greet and what photo to show, per home.png's "Good morning, Bahar 👋". */
data class GreetingInfo(
    val userFirstName: String,
    val avatarUrl: String?,
)

/**
 * The "Overview" section's historical trend data (home.png: "12% higher
 * than last week", the weekly completion chart) — the one part of Home's
 * old `OverviewStats` that has no Planner equivalent, since no feature in
 * this app tracks completion history over time yet (Iteration 4's backend
 * integration confirmed neither Planner's nor Travel's backend has a task-
 * history endpoint). [productivityDeltaPercent] is always `null` and
 * [weeklyCompletionRatios] always empty until a real history capability
 * exists — see [OverviewStats]'s KDoc for why those are honest empty
 * states, not fabricated numbers.
 * [com.lifeos.app.features.home.presentation.HomeViewModel] merges this
 * with Planner's real, current completed/total/productivity numbers to
 * build the [com.lifeos.app.features.home.domain.model.OverviewStats] the
 * UI actually renders.
 */
data class TaskTrend(
    val productivityDeltaPercent: Int?,
    val weeklyCompletionRatios: List<Float>,
)
