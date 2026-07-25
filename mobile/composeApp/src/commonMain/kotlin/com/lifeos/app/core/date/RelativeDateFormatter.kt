package com.lifeos.app.core.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.daysUntil

/**
 * The "smart" due-date label every task list/card in this app shows —
 * today's tasks read as a bare time ("08:30"), tomorrow's as "Yarın,
 * 14:00", this week's as a weekday name ("Pazartesi"), and anything
 * further as day+month ("12 Kasım"). This is exactly the set of display
 * shapes [com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource]'s
 * fake due-date strings already used before this refactor replaced those
 * hand-written strings with real [LocalDate]/[LocalTime] values — this
 * object is what now derives the same Turkish text from real dates instead
 * of a human having typed it once and frozen it in a string literal.
 *
 * Deliberately free of any [com.lifeos.app.features.planner.domain.model.Task]
 * knowledge (plain [LocalDate]/[LocalTime] in, [String] out) so Home and
 * Travel can reuse it unchanged for their own due-date-shaped fields.
 */
object RelativeDateFormatter {

    fun format(date: LocalDate, time: LocalTime? = null, today: LocalDate = AppToday.date): String {
        val daysFromToday = today.daysUntil(date)
        val base = when {
            daysFromToday == 0 -> time?.let { AppDateFormatter.toTimeLabel(it) } ?: TODAY_LABEL
            daysFromToday == 1 -> TOMORROW_LABEL
            daysFromToday in 2..DAYS_TREATED_AS_THIS_WEEK -> AppDateFormatter.toWeekdayLabel(date)
            date.year == today.year -> AppDateFormatter.toShortLabel(date)
            else -> AppDateFormatter.toFullLabel(date)
        }
        val needsTimeSuffix = daysFromToday != 0 && time != null
        return if (needsTimeSuffix) "$base, ${AppDateFormatter.toTimeLabel(requireNotNull(time))}" else base
    }

    private const val TODAY_LABEL = "Bugün"
    private const val TOMORROW_LABEL = "Yarın"
    private const val DAYS_TREATED_AS_THIS_WEEK = 6
}
