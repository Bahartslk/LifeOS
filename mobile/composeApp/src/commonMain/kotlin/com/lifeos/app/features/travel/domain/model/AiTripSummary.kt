package com.lifeos.app.features.travel.domain.model

/**
 * "AI Forecast" card content (travel-details.png): a generated highlight
 * plus the two supporting chips (weather, wind). [highlightMessage] is
 * plain data here — any bold/emphasis styling (e.g. "tomorrow at 5:30 AM")
 * is a presentation concern, not modeled as rich text in the domain layer.
 *
 * [travelTips] defaults to an empty list — Travel Detail's existing AI
 * Forecast card has no "tips" concept and every current call site
 * constructs this with named arguments, so the default keeps them all
 * source-compatible. Only "Create Travel (AI)"'s Generated Preview
 * (this task's "Travel tips" requirement) populates it.
 */
data class AiTripSummary(
    val highlightMessage: String,
    val weatherTemperatureCelsius: Int,
    val windSpeedKmh: Int,
    val travelTips: List<String> = emptyList(),
)
