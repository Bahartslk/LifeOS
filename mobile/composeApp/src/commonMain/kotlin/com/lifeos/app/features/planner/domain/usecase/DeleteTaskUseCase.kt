package com.lifeos.app.features.planner.domain.usecase

import com.lifeos.app.features.planner.domain.repository.PlannerRepository

class DeleteTaskUseCase(private val plannerRepository: PlannerRepository) {
    suspend operator fun invoke(taskId: String): Result<Unit> = plannerRepository.deleteTask(taskId)
}
