package com.lifeos.app.features.planner.domain.usecase

import com.lifeos.app.features.planner.domain.model.PlannerCalendarMonth
import com.lifeos.app.features.planner.domain.repository.PlannerRepository

class GetCalendarMonthUseCase(private val plannerRepository: PlannerRepository) {
    suspend operator fun invoke(year: Int, month: Int): Result<PlannerCalendarMonth> =
        plannerRepository.getCalendarMonth(year, month)
}
