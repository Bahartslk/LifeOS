package com.lifeos.app.features.home.domain.usecase

import com.lifeos.app.features.home.domain.model.HomeDashboard
import com.lifeos.app.features.home.domain.repository.HomeRepository

class GetHomeDashboardUseCase(private val homeRepository: HomeRepository) {
    suspend operator fun invoke(): Result<HomeDashboard> = homeRepository.getDashboard()
}
