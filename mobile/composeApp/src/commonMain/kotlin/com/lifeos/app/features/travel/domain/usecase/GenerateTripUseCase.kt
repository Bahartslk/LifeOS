package com.lifeos.app.features.travel.domain.usecase

import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.TripGenerationRequest
import com.lifeos.app.features.travel.domain.repository.TripGenerationRepository

class GenerateTripUseCase(private val tripGenerationRepository: TripGenerationRepository) {
    suspend operator fun invoke(request: TripGenerationRequest): Result<TripDetail> =
        tripGenerationRepository.generateTrip(request)
}
