package com.lifeos.app.features.planner.domain.usecase

import com.lifeos.app.features.planner.domain.model.TaskDetail
import com.lifeos.app.features.planner.domain.repository.PlannerRepository

class GetTaskDetailUseCase(private val plannerRepository: PlannerRepository) {
    suspend operator fun invoke(taskId: String): Result<TaskDetail> = plannerRepository.getTaskDetail(taskId)
}
