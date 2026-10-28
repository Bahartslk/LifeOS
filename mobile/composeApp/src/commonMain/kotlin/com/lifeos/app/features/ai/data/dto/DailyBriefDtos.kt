package com.lifeos.app.features.ai.data.dto

import kotlinx.serialization.Serializable

/**
 * Response of the backend's `POST /daily-brief/overview`
 * (`backend/src/modules/daily-brief/dto/daily-overview-response.dto.ts`):
 * summary, priorities and motivation from one AI call. The backend checks
 * each part on its own and sends `null` for a part the AI got wrong, so every
 * part is nullable here. The shared AI envelope fields (`provider`, `model`,
 * `usage`, `latencyMs`) next to `result` are not used on mobile;
 * `ignoreUnknownKeys = true` in `HttpClientFactory.json` makes leaving them
 * off safe.
 */
@Serializable
data class DailyOverviewResponseDto(val result: DailyOverviewResultDto)

@Serializable
data class DailyOverviewResultDto(
    val summary: DailySummaryResultDto? = null,
    val priorities: List<DailyPriorityDto>? = null,
    val motivation: DailyMotivationResultDto? = null,
)

@Serializable
data class DailySummaryResultDto(
    val todayPriorities: List<String> = emptyList(),
    val keyEvents: List<String> = emptyList(),
    val suggestedFocus: String = "",
)

@Serializable
data class DailyPriorityDto(
    val taskTitle: String,
    val suggestedRank: Int,
    val reason: String = "",
)

@Serializable
data class DailyMotivationResultDto(val message: String = "")
