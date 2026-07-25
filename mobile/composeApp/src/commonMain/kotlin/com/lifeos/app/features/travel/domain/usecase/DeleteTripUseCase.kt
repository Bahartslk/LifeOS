package com.lifeos.app.features.travel.domain.usecase

import com.lifeos.app.features.travel.domain.repository.TravelRepository

class DeleteTripUseCase(private val travelRepository: TravelRepository) {
    suspend operator fun invoke(tripId: String): Result<Unit> = travelRepository.deleteTrip(tripId)
}
