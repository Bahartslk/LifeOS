package com.lifeos.app.features.planner.domain.usecase

import com.lifeos.app.features.planner.domain.repository.PlannerRepository

class ToggleSubtaskCompletionUseCase(private val plannerRepository: PlannerRepository) {
    suspend operator fun invoke(taskId: String, subtaskId: String): Result<Unit> =
        plannerRepository.toggleSubtaskCompletion(taskId, subtaskId)
}
