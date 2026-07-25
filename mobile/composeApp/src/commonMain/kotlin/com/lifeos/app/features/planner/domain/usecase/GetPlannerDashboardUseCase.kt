package com.lifeos.app.features.planner.domain.usecase

import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.repository.PlannerRepository

class GetPlannerDashboardUseCase(private val plannerRepository: PlannerRepository) {
    suspend operator fun invoke(): Result<PlannerDashboard> = plannerRepository.getDashboard()
}
