package com.lifeos.app.features.travel.domain.usecase

import com.lifeos.app.features.travel.domain.model.TravelListData
import com.lifeos.app.features.travel.domain.repository.TravelRepository

class GetTravelListUseCase(private val travelRepository: TravelRepository) {
    suspend operator fun invoke(): Result<TravelListData> = travelRepository.getTravelList()
}
