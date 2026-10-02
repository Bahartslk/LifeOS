package com.lifeos.app.features.planner.domain.util

import com.lifeos.app.core.date.AppDateFormatter
import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.planner.domain.model.PlannerCalendarDay
import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.Task
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Builds a complete, calendrically-correct, Monday-first month grid for any
 * (year, month) — shared by the real repository (counts from backend tasks)
 * and `FakePlannerDataSource` (preview data), so the grid arithmetic lives
 * in exactly one place.
 *
 * Leading/trailing days borrowed from adjacent months complete the first
 * and last weeks; they stay decorative-only (`taskCount = 0`) even if
 * [tasks] happens to contain tasks due on them. Built entirely from
 * `kotlinx-datetime` arithmetic — no hand-rolled leap-year/weekday math.
 */
object CalendarMonthBuilder {

    private const val DAYS_PER_WEEK = 7

    /** [today] defaults to the device's local date ([AppToday]); a parameter only so tests can pin it. */
    fun build(
        year: Int,
        month: Int,
        tasks: List<Task>,
        today: LocalDate = AppToday.date,
    ): PlannerCalendarMonth {
        val firstOfMonth = LocalDate(year, month, 1)
        val firstOfNextMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH)
        val totalDays = firstOfNextMonth.minus(1, DateTimeUnit.DAY).dayOfMonth
        // DayOfWeek is ISO-ordered (Monday first), so .ordinal is already a Monday-indexed 0..6 count.
        val leadingCount = firstOfMonth.dayOfWeek.ordinal
        val taskCountByDate = tasks.groupingBy { it.dueDate.date }.eachCount()

        fun day(date: LocalDate, isCurrentMonth: Boolean) = PlannerCalendarDay(
            date = date,
            isCurrentMonth = isCurrentMonth,
            isToday = date == today,
            taskCount = if (isCurrentMonth) taskCountByDate[date] ?: 0 else 0,
        )

        val days = buildList {
            for (offset in leadingCount downTo 1) {
                add(day(firstOfMonth.minus(offset, DateTimeUnit.DAY), isCurrentMonth = false))
            }
            for (dayOfMonth in 1..totalDays) {
                add(day(LocalDate(year, month, dayOfMonth), isCurrentMonth = true))
            }
            val trailingCount = (DAYS_PER_WEEK - (size % DAYS_PER_WEEK)) % DAYS_PER_WEEK
            for (offset in 0 until trailingCount) {
                add(day(firstOfNextMonth.plus(offset, DateTimeUnit.DAY), isCurrentMonth = false))
            }
        }

        return PlannerCalendarMonth(
            monthLabel = AppDateFormatter.toMonthYearLabel(year, month),
            weekdayLabels = AppDateFormatter.weekdayShortLabels,
            days = days,
        )
    }
}
