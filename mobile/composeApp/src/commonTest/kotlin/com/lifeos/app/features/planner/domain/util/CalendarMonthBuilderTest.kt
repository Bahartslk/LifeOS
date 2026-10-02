package com.lifeos.app.features.planner.domain.util

import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskDueDate
import com.lifeos.app.features.planner.domain.model.TaskPriority
import com.lifeos.app.features.planner.domain.model.TaskSource
import com.lifeos.app.features.planner.domain.model.TaskStatus
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CalendarMonthBuilderTest {

    @Test
    fun twentyEightDayMonth_startingOnMonday_hasNoAdjacentDays() {
        // 1 Feb 2021 is a Monday and February 2021 has exactly four weeks.
        val calendar = build(2021, 2)

        assertEquals(28, calendar.days.size)
        assertTrue(calendar.days.all { it.isCurrentMonth })
        assertEquals(LocalDate(2021, 2, 1), calendar.days.first().date)
        assertEquals(LocalDate(2021, 2, 28), calendar.days.last().date)
    }

    @Test
    fun twentyEightDayMonth_withAdjacentDays() {
        // 1 Feb 2026 is a Sunday: six leading January days, one trailing March day.
        val calendar = build(2026, 2)

        assertEquals(28, calendar.days.count { it.isCurrentMonth })
        assertEquals(LocalDate(2026, 1, 26), calendar.days.first().date)
        assertEquals(LocalDate(2026, 3, 1), calendar.days.last().date)
        assertEquals(35, calendar.days.size)
    }

    @Test
    fun twentyNineDayLeapFebruary() {
        // 1 Feb 2028 is a Tuesday.
        val calendar = build(2028, 2)

        assertEquals(29, calendar.days.count { it.isCurrentMonth })
        assertEquals(LocalDate(2028, 2, 29), calendar.days.last { it.isCurrentMonth }.date)
        assertEquals(LocalDate(2028, 1, 31), calendar.days.first().date)
        assertEquals(LocalDate(2028, 3, 5), calendar.days.last().date)
    }

    @Test
    fun thirtyDayMonth() {
        // 1 Sep 2026 is a Tuesday.
        val calendar = build(2026, 9)

        assertEquals(30, calendar.days.count { it.isCurrentMonth })
        assertEquals(LocalDate(2026, 8, 31), calendar.days.first().date)
        assertEquals(LocalDate(2026, 10, 4), calendar.days.last().date)
    }

    @Test
    fun thirtyOneDayMonth() {
        // 1 Oct 2026 is a Thursday.
        val calendar = build(2026, 10)

        assertEquals(31, calendar.days.count { it.isCurrentMonth })
        assertEquals(LocalDate(2026, 9, 28), calendar.days.first().date)
        assertEquals(LocalDate(2026, 11, 1), calendar.days.last().date)
    }

    @Test
    fun everyGrid_isMondayFirst_inWholeContiguousWeeks() {
        for (month in 1..12) {
            val days = build(2026, month).days
            assertEquals(0, days.size % 7, "month=$month")
            assertEquals(DayOfWeek.MONDAY, days.first().date.dayOfWeek, "month=$month")
            assertEquals(DayOfWeek.SUNDAY, days.last().date.dayOfWeek, "month=$month")
            days.zipWithNext().forEach { (a, b) -> assertEquals(a.date.toEpochDays() + 1, b.date.toEpochDays()) }
        }
    }

    @Test
    fun previousAndNextMonthDays_areMarkedNotCurrentMonth() {
        val days = build(2026, 10).days

        assertEquals(
            listOf(LocalDate(2026, 9, 28), LocalDate(2026, 9, 29), LocalDate(2026, 9, 30)),
            days.takeWhile { !it.isCurrentMonth }.map { it.date },
        )
        assertEquals(listOf(LocalDate(2026, 11, 1)), days.takeLastWhile { !it.isCurrentMonth }.map { it.date })
    }

    @Test
    fun isToday_marksOnlyTheGivenDate() {
        val today = LocalDate(2026, 10, 2)

        val days = build(2026, 10, today = today).days

        assertEquals(listOf(today), days.filter { it.isToday }.map { it.date })
    }

    @Test
    fun isToday_isFalseEverywhere_whenTodayIsOutsideTheGrid() {
        assertFalse(build(2026, 12, today = LocalDate(2026, 10, 2)).days.any { it.isToday })
    }

    @Test
    fun taskCounts_comeFromTasksDueOnEachCurrentMonthDay() {
        val tasks = listOf(task("2026-10-02"), task("2026-10-02"), task("2026-10-31"), task("2025-10-02"))

        val days = build(2026, 10, tasks = tasks).days

        assertEquals(2, days.single { it.date == LocalDate(2026, 10, 2) }.taskCount)
        assertEquals(1, days.single { it.date == LocalDate(2026, 10, 31) }.taskCount)
        assertEquals(3, days.sumOf { it.taskCount })
    }

    @Test
    fun adjacentMonthDays_alwaysHaveZeroTaskCount() {
        val tasks = listOf(task("2026-09-30"), task("2026-11-01"))

        val days = build(2026, 10, tasks = tasks).days

        assertEquals(0, days.single { it.date == LocalDate(2026, 9, 30) }.taskCount)
        assertEquals(0, days.single { it.date == LocalDate(2026, 11, 1) }.taskCount)
    }

    @Test
    fun labels_comeFromAppDateFormatter() {
        val calendar = build(2026, 10)

        assertEquals("Ekim 2026", calendar.monthLabel)
        assertEquals(7, calendar.weekdayLabels.size)
    }

    private fun build(
        year: Int,
        month: Int,
        tasks: List<Task> = emptyList(),
        today: LocalDate = LocalDate(2000, 1, 1),
    ): PlannerCalendarMonth = CalendarMonthBuilder.build(year = year, month = month, tasks = tasks, today = today)

    private var nextId = 0

    private fun task(dueDate: String) = Task(
        id = "task-${nextId++}",
        title = "Görev",
        description = null,
        dueDate = TaskDueDate(date = LocalDate.parse(dueDate), time = null),
        priority = TaskPriority.MEDIUM,
        category = TaskCategory.PERSONAL,
        status = TaskStatus.TODO,
        source = TaskSource.PLANNER,
        tags = emptyList(),
        hasReminder = false,
        createdAt = LocalDate(2026, 10, 1),
    )
}
