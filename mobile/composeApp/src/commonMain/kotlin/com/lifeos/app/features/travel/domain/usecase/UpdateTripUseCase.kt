package com.lifeos.app.features.travel.domain.usecase

import com.lifeos.app.features.travel.domain.model.UpdateTripRequest
import com.lifeos.app.features.travel.domain.repository.TravelRepository

class UpdateTripUseCase(private val travelRepository: TravelRepository) {
    suspend operator fun invoke(tripId: String, request: UpdateTripRequest): Result<Unit> =
        travelRepository.updateTrip(tripId, request)
}
