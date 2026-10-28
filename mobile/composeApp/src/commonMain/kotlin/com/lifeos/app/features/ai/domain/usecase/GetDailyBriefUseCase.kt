package com.lifeos.app.features.ai.domain.usecase

import com.lifeos.app.features.ai.domain.model.DailyBrief
import com.lifeos.app.features.ai.domain.repository.DailyBriefRepository

class GetDailyBriefUseCase(private val dailyBriefRepository: DailyBriefRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<DailyBrief> =
        dailyBriefRepository.getDailyBrief(forceRefresh)
}
