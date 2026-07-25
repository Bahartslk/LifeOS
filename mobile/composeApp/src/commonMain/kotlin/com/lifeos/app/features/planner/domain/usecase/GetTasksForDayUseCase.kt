package com.lifeos.app.features.planner.domain.usecase

import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.repository.PlannerRepository
import kotlinx.datetime.LocalDate

class GetTasksForDayUseCase(private val plannerRepository: PlannerRepository) {
    suspend operator fun invoke(date: LocalDate): Result<List<Task>> = plannerRepository.getTasksForDay(date)
}
