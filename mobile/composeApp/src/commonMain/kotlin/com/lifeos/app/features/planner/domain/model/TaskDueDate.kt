package com.lifeos.app.features.planner.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * A [Task]'s due date, now a real domain value instead of a pre-formatted
 * Turkish display string (this sprint's refactor — see [Task]'s KDoc for
 * why). [time] is nullable rather than this being two separate required
 * fields: some tasks are genuinely all-day ("12 Kasım" — Kapadokya
 * Seyahati has no meaningful time-of-day), others are timed ("15:30" —
 * Proje Çalışması). [com.lifeos.app.core.date.RelativeDateFormatter]
 * turns this back into the Turkish text every screen shows; nothing in
 * `domain` ever formats it.
 */
data class TaskDueDate(
    val date: LocalDate,
    val time: LocalTime? = null,
)
