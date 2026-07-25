package com.lifeos.app.core.date

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

/**
 * Parses the same free-text Turkish due-date vocabulary Create Task's
 * `DueDateField` already accepted before this refactor — a bare time
 * ("14:00"), "Bugün"/"Yarın" with an optional time, a bare weekday name
 * ("Pazartesi"), or a day+Turkish-month with an optional year and time
 * ("12 Kasım", "12 Kasım 2025", "12 Kasım, 14:00"). Returns `null` for
 * anything else, so the caller can surface a field-level validation error
 * instead of ever constructing a [com.lifeos.app.features.planner.domain.model.Task]
 * from text nobody can be sure means what it looks like it means.
 *
 * Returns a plain `Pair<LocalDate, LocalTime?>` rather than
 * [com.lifeos.app.features.planner.domain.model.TaskDueDate] — this parser
 * has no [com.lifeos.app.features.planner.domain.model.Task] knowledge, so
 * Home or Travel can reuse it unchanged for their own due-date-shaped free
 * text; Planner wraps the result into its own domain type at the call site.
 */
object AppDateParser {

    fun parse(input: String, today: LocalDate = AppToday.date): Pair<LocalDate, LocalTime?>? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val commaIndex = trimmed.indexOf(',')
        val prefix = if (commaIndex >= 0) trimmed.substring(0, commaIndex).trim() else trimmed
        val timeText = if (commaIndex >= 0) trimmed.substring(commaIndex + 1).trim() else null

        // A comma was present but what follows it isn't a valid time — the whole input is malformed.
        val time = timeText?.let { parseTime(it) ?: return null }

        if (commaIndex < 0) {
            parseTime(prefix)?.let { bareTime -> return today to bareTime }
        }

        when (prefix.lowercase()) {
            TODAY_KEYWORD -> return today to time
            TOMORROW_KEYWORD -> return today.plus(1, DateTimeUnit.DAY) to time
        }

        AppDateFormatter.weekdayFromTurkishName(prefix)?.let { weekday ->
            return nextOccurrenceOf(weekday, after = today) to time
        }

        parseDayMonth(prefix, today)?.let { date -> return date to time }

        return null
    }

    private fun parseTime(text: String): LocalTime? {
        val match = TIME_REGEX.matchEntire(text.trim()) ?: return null
        val hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].toInt()
        return try {
            LocalTime(hour, minute)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun parseDayMonth(text: String, today: LocalDate): LocalDate? {
        val parts = text.split(" ").filter { it.isNotBlank() }
        if (parts.size !in 2..3) return null

        val day = parts[0].toIntOrNull() ?: return null
        val month = AppDateFormatter.monthNumberFromTurkishName(parts[1]) ?: return null
        val explicitYear = if (parts.size == 3) parts[2].toIntOrNull() ?: return null else null

        return try {
            if (explicitYear != null) {
                LocalDate(explicitYear, month, day)
            } else {
                val candidate = LocalDate(today.year, month, day)
                if (candidate < today) LocalDate(today.year + 1, month, day) else candidate
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun nextOccurrenceOf(weekday: DayOfWeek, after: LocalDate): LocalDate {
        var candidate = after.plus(1, DateTimeUnit.DAY)
        while (candidate.dayOfWeek != weekday) {
            candidate = candidate.plus(1, DateTimeUnit.DAY)
        }
        return candidate
    }

    private val TIME_REGEX = Regex("""(\d{1,2}):(\d{2})""")
    private const val TODAY_KEYWORD = "bugün"
    private const val TOMORROW_KEYWORD = "yarın"
}
