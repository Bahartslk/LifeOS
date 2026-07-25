package com.lifeos.app.features.planner.domain.usecase

import com.lifeos.app.features.planner.domain.model.CreateTaskRequest
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.repository.PlannerRepository

class CreateTaskUseCase(private val plannerRepository: PlannerRepository) {
    suspend operator fun invoke(request: CreateTaskRequest): Result<Task> = plannerRepository.createTask(request)
}
