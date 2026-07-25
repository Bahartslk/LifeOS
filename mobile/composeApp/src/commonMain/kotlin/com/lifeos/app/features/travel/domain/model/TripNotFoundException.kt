package com.lifeos.app.features.travel.domain.model

/**
 * Thrown by [com.lifeos.app.features.travel.domain.repository.TravelRepository.getTripDetail]
 * when no trip matches the given id. A domain-level type (not a data-layer
 * exception reused across layers) so [com.lifeos.app.features.travel.presentation.TravelDetailViewModel]
 * can distinguish "trip not found" (an Empty State) from any other failure
 * (an Error State) without depending on anything in `data`.
 */
class TripNotFoundException(tripId: String) : Exception("Trip not found: $tripId")
