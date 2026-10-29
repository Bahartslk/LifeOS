package com.lifeos.app.features.ai.domain.model

import kotlinx.datetime.LocalDate

/**
 * The AI's view of the user's day, from the backend's Daily Brief overview.
 * Each part is `null` when the AI's answer for it was missing or invalid, so
 * one bad part never hides the parts that did arrive.
 *
 * [reminders] is optional: [isComplete] ignores it, so a brief without
 * reminder suggestions is still cached for the day instead of spending
 * another AI request on every load.
 */
data class DailyBrief(
    val summary: DailySummary?,
    val priorities: List<DailyPriority>?,
    val motivation: String?,
    val reminders: List<DailyReminder>? = null,
) {
    val isComplete: Boolean get() = summary != null && priorities != null && motivation != null
}

data class DailySummary(
    val suggestedFocus: String,
    val keyEvents: List<String>,
)

/** An AI-recommended order for today's tasks. [rank] 1 means "do this first"; it never changes the task's stored priority. */
data class DailyPriority(
    val rank: Int,
    val taskTitle: String,
    val reason: String,
)

/**
 * A reminder the AI suggests adding to the agenda. Only a suggestion: nothing
 * is created until the user taps "Ajandaya ekle". [recommendedDate] is `null`
 * when no specific date applies.
 */
data class DailyReminder(
    val title: String,
    val reason: String,
    val relatedTo: ReminderRelatedTo,
    val recommendedDate: LocalDate?,
)

enum class ReminderRelatedTo { PLANNER, TRAVEL, BOTH }
