package com.lifeos.app.features.home.domain.model

/**
 * The "Your Next Adventure" trip card (home.png: Cappadocia, flight, hotel,
 * countdown), per FR-HOME-02 ("displays the user's upcoming trip(s)").
 * A minimal, Home-scoped read model built from Travel's real
 * [com.lifeos.app.features.travel.domain.model.Trip] (Iteration 4's backend
 * integration) — not Travel's full `Trip`/`TripDetail` entity itself, which
 * owns itinerary items, budget, packing lists, etc. Home only ever needs a
 * summary, and fetching a trip's full detail (an extra itinerary request)
 * just to populate this summary card would be an unnecessary network call.
 *
 * [weatherTemperatureCelsius]/[flightCode]/[flightGate]/[flightDepartureLabel]/
 * [hotelName]/[hotelRoomType] are all nullable — Travel's backend has no
 * weather data at all, no reliable flight model (a single itinerary
 * `location` string can't be split into departure/arrival airports,
 * terminal, and gate), and hotel info only when a real `ACCOMMODATION`
 * itinerary item happens to exist (which Home doesn't fetch, per above).
 * [com.lifeos.app.features.home.presentation.sections.UpcomingJourneySection]
 * omits each chip/row when its backing value is `null`, the same pattern
 * [com.lifeos.app.features.travel.domain.model.Trip.daysUntilStart] already
 * uses successfully.
 */
data class UpcomingJourney(
    val destinationName: String,
    val region: String,
    val daysRemaining: Int,
    val weatherTemperatureCelsius: Int?,
    val flightCode: String?,
    val flightGate: String?,
    val flightDepartureLabel: String?,
    val hotelName: String?,
    val hotelRoomType: String?,
    val coverImageUrl: String?,
)
