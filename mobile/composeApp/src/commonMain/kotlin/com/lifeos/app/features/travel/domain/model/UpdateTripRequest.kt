package com.lifeos.app.features.travel.domain.model

import kotlinx.datetime.LocalDate

/**
 * Partial-update input for [com.lifeos.app.features.travel.domain.repository.TravelRepository.updateTrip]
 * (Iteration 3's addition — no UI currently triggers this; added purely to
 * satisfy the "Update trip" backend capability, the same
 * capability-without-a-UI-hook precedent [com.lifeos.app.features.auth.domain.usecase.LogoutUseCase]
 * already established). Every field is `null` = "leave unchanged", matching
 * the backend's own PATCH semantics.
 *
 * Deliberately its own type rather than reusing [Trip]: [Trip] has no
 * `title`/`description`/real dates at all (it only ever carries a
 * pre-formatted [Trip.dateRangeLabel]) — a real update request needs real,
 * unambiguous values to send over the wire, not a display string to
 * reverse-parse.
 */
data class UpdateTripRequest(
    val title: String? = null,
    val description: String? = null,
    val destination: String? = null,
    val country: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val coverImageUrl: String? = null,
    val status: TripStatus? = null,
)
