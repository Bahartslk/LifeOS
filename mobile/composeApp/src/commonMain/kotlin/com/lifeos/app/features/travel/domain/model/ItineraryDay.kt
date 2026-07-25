package com.lifeos.app.features.travel.domain.model

/**
 * One "Daily Journey" timeline entry (travel-details.png: "01 Arrival &
 * Cave Hotel Check-in"). [activities] breaks the day into Morning/Afternoon/
 * Evening per this task's explicit Timeline Section requirement — the
 * Stitch mockup itself shows a single description per day, so
 * [TimelineDayCard] shows [activities] only once a day card is expanded
 * (see this feature's "Deviations from Stitch" note).
 */
data class ItineraryDay(
    val dayNumber: Int,
    val title: String,
    val activities: List<ItineraryActivity>,
    val photoUrls: List<String>,
    val bookingActionLabel: String?,
)

data class ItineraryActivity(
    val period: DayPeriod,
    val description: String,
)

enum class DayPeriod {
    MORNING,
    AFTERNOON,
    EVENING,
}
