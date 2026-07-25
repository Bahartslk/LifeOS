package com.lifeos.app.features.travel.domain.usecase

import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.repository.TravelRepository

class SaveTripUseCase(private val travelRepository: TravelRepository) {
    /** Returns the persisted trip's real id — see [TravelRepository.saveTrip]'s KDoc for why. */
    suspend operator fun invoke(detail: TripDetail): Result<String> = travelRepository.saveTrip(detail)
}
