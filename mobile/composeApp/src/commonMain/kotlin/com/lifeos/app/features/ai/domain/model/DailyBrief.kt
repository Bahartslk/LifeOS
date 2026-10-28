package com.lifeos.app.features.ai.domain.model

/**
 * The AI's view of the user's day, built from three independent backend
 * suggestions. Each part is `null` when its own request failed, so one
 * failing AI call never hides the parts that did arrive.
 */
data class DailyBrief(
    val summary: DailySummary?,
    val priorities: List<DailyPriority>?,
    val motivation: String?,
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
