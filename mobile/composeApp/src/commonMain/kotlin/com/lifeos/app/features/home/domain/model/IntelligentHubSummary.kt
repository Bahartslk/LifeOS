package com.lifeos.app.features.home.domain.model

/**
 * The "Intelligent Hub" hero card's content (home.png), per
 * docs/07-functional-requirements.md#home-dashboard (FR-HOME-03, an
 * AI-curated highlight) and docs/09-ai-features.md#feature-overview (the
 * daily highlight surfaced on Home).
 *
 * [highlights] is an ordered list, not fixed named fields — a real backend
 * composing this from Travel/Planner/weather data would naturally return a
 * variable-length list (a user with no trip gets fewer rows), so the model
 * is shaped that way from the start rather than needing a breaking change
 * later.
 */
data class IntelligentHubSummary(
    val highlights: List<HubHighlight>,
)

data class HubHighlight(
    val type: HubHighlightType,
    val message: String,
)

enum class HubHighlightType {
    TRIP,
    PACKING,
    TASK,
    WEATHER,
}
