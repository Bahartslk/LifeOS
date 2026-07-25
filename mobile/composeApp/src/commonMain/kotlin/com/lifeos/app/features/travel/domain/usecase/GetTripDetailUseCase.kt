package com.lifeos.app.features.travel.domain.usecase

import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.repository.TravelRepository

class GetTripDetailUseCase(private val travelRepository: TravelRepository) {
    suspend operator fun invoke(tripId: String): Result<TripDetail> =
        travelRepository.getTripDetail(tripId)
}
