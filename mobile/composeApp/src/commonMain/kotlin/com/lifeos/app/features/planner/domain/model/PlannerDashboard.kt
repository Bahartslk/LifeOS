package com.lifeos.app.features.planner.domain.model

import kotlinx.datetime.LocalDate

/**
 * The aggregate root for the Planner Dashboard (planner.png), per this
 * task's scope — mirrors a future `GET /api/v1/planner/dashboard`-style BFF
 * response, the same one-call-one-payload shape
 * [com.lifeos.app.features.home.domain.model.HomeDashboard] already
 * established for Home.
 *
 * [todayTasks] and [upcomingTasks] are separate, explicit lists rather than
 * one flat list the UI buckets by date at render time — the same choice
 * [com.lifeos.app.features.travel.domain.model.TravelListData] made for
 * `upcomingTrips`/`pastTrips`. Once Home wants to group tasks by date
 * itself, it can now do so directly off [Task.dueDate] (a real
 * [kotlinx.datetime.LocalDate]) instead of needing this same
 * already-bucketed shape repeated.
 */
data class PlannerDashboard(
    val date: LocalDate,
    val aiInsightMessage: String,
    val overview: PlannerOverview,
    val calendar: PlannerCalendarMonth,
    val todayTasks: List<Task>,
    val upcomingTasks: List<Task>,
)

/**
 * "Today's Overview" / "Today's Productivity" stats (planner.png: "Total
 * tasks 12", "Upcoming event 3", "Completed 8", "Productivity 92%").
 * [productivityPercent] is a distinct, independently-sourced metric from
 * [completedTaskCount]/[totalTaskCount] — not a derived ratio of them — the
 * same "AI score is its own number" precedent
 * [com.lifeos.app.features.home.domain.model.OverviewStats.productivityPercent]
 * already established (8/12 completed is 67%, yet Stitch shows 92%
 * productivity: two different metrics, not one computed from the other).
 */
data class PlannerOverview(
    val totalTaskCount: Int,
    val upcomingEventCount: Int,
    val completedTaskCount: Int,
    val productivityPercent: Int,
)

/**
 * A full, calendrically-correct month grid (planner.png: "Ekim 2024"). See
 * [com.lifeos.app.features.planner.presentation.sections.PlannerCalendarSection]'s
 * KDoc for why this is a complete month rather than the two partial weeks
 * the mockup screenshot appears to show. [monthLabel]/[weekdayLabels] stay
 * pre-formatted Turkish text (not [PlannerCalendarDay.date] derivatives at
 * every call site) since every consumer of this grid needs them verbatim
 * and identically — one formatting call when the grid is built, not one
 * per composable that renders it.
 */
data class PlannerCalendarMonth(
    val monthLabel: String,
    val weekdayLabels: List<String>,
    val days: List<PlannerCalendarDay>,
)

/**
 * One grid cell. [date] is the real date this cell represents (this
 * sprint's refactor — previously just a bare [Int] day-of-month, which
 * could not disambiguate a leading/trailing cell borrowed from an adjacent
 * month from a same-numbered day in the displayed month). [isCurrentMonth]
 * is `false` for those borrowed leading/trailing days (rendered dimmed,
 * per every standard calendar widget convention); the day number itself is
 * still shown via `date.dayOfMonth` wherever this cell is rendered.
 *
 * [taskCount] is the Planner Calendar screen's requirement (task-count
 * indicators per day) — always `0` for leading/trailing days, since those
 * cells are decorative only (not selectable, per
 * [com.lifeos.app.features.planner.presentation.sections.CalendarDay]'s
 * KDoc) and were never given task-related meaning even before this field
 * existed. Defaults to `0` so the Dashboard's own read-only calendar widget
 * ([com.lifeos.app.features.planner.presentation.sections.PlannerCalendarSection],
 * which has no use for indicators) needs no changes.
 */
data class PlannerCalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val taskCount: Int = 0,
)
