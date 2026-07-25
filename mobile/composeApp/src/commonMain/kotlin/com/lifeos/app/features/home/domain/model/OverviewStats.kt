package com.lifeos.app.features.home.domain.model

/**
 * The "Overview" section's stat tiles and weekly chart (home.png:
 * "TASKS COMPLETED 18/24", "PRODUCTIVITY SCORE 92%", "12% higher than last
 * week").
 *
 * [productivityDeltaPercent] is `null` and [weeklyCompletionRatios] is
 * empty whenever no real week-over-week history exists (Iteration 4's
 * backend integration) — neither Planner nor Travel's backend tracks
 * completion history over time yet, so there is nothing real to compute
 * either from. `null`/empty are real, honest states here, not "loading" —
 * see [com.lifeos.app.features.home.presentation.sections.OverviewSection]'s
 * `ProductivityTile` for how the comparison sentence is skipped when
 * [productivityDeltaPercent] is `null`, and
 * [com.lifeos.app.features.home.presentation.sections.WeeklyProgressChart]
 * for how an empty [weeklyCompletionRatios] renders as a blank chart rather
 * than a fabricated one. When non-empty, [weeklyCompletionRatios] is 7
 * values (Monday..Sunday), each in `0f..1f`.
 */
data class OverviewStats(
    val completedTaskCount: Int,
    val totalTaskCount: Int,
    val productivityPercent: Int,
    val productivityDeltaPercent: Int?,
    val weeklyCompletionRatios: List<Float>,
)
