package com.lifeos.app.core.date

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * The app's current "today" for every relative-date computation across
 * this codebase — "today" highlighting on the Planner Calendar, relative
 * due-date formatting ("Yarın"/"Bugün"), and free-text date parsing all
 * measure against this single value rather than each picking their own.
 *
 * The real device date (Iteration 2.5's fix), read via `kotlinx-datetime`'s
 * [Clock.System] rather than a platform expect/actual — no Kotlin/Native or
 * Android-specific code needed, since `kotlinx-datetime` already provides
 * this as common code. A computed property, not a `val` cached at object
 * creation, so a long-lived app session still sees the correct date after
 * a real midnight rollover. Previously a fixed 24 October 2024 (matching
 * every feature's fake seed data) — now that Planner is backed by a real
 * backend with real, non-fixed dates, that fixed value would make "today"
 * highlighting and every relative-date label wrong instead of right.
 */
object AppToday {
    val date get() = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
