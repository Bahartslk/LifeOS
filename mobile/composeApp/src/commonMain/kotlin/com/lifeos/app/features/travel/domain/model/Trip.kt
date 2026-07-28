package com.lifeos.app.features.travel.domain.model

/**
 * A single trip, shown either in Upcoming Journeys or Past Journeys
 * depending on [status]. Deliberately one entity rather than separate
 * "UpcomingTrip"/"PastTrip" types — it mirrors the single `trips` table
 * defined in docs/14-database-design.md#entities, using the exact same
 * status vocabulary (`planned`, `ongoing`, `completed`, `cancelled`), so
 * this model needs no reshaping once a real Travel backend exists.
 *
 * Fields only meaningful for one status are nullable rather than split
 * into two types: [daysUntilStart] and [weatherTemperatureCelsius] apply to
 * [TripStatus.PLANNED] trips (travel-list.png's countdown/weather chips);
 * [photoCount] applies to [TripStatus.COMPLETED] trips (the "428 Photos"
 * indicator on Memories cards).
 *
 * [travelStyle] is the "Seyahat Tarzı" chosen in Create Travel (AI) —
 * persisted on the backend `trips.category` column so it survives a
 * save/reload instead of only ever existing during draft generation.
 * Defaulted to [TravelStyle.RELAX] so existing call sites that predate this
 * field don't need updating.
 */
data class Trip(
    val id: String,
    val destinationCity: String,
    val destinationCountry: String,
    val dateRangeLabel: String,
    val status: TripStatus,
    val coverImageUrl: String?,
    val isAiOptimized: Boolean,
    val daysUntilStart: Int?,
    val weatherTemperatureCelsius: Int?,
    val photoCount: Int?,
    val travelStyle: TravelStyle = TravelStyle.RELAX,
)

enum class TripStatus {
    PLANNED,
    ONGOING,
    COMPLETED,
    CANCELLED,
}
