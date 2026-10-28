package com.lifeos.app.features.ai.data.dto

import kotlinx.serialization.Serializable

/**
 * Responses of the backend's `POST /daily-brief/{summary,priorities,motivation}`
 * endpoints (`backend/src/modules/daily-brief/dto/`). Each one carries the
 * shared AI envelope fields (`provider`, `model`, `usage`, `latencyMs`) next
 * to `result`; only `result` is used on mobile, and `ignoreUnknownKeys = true`
 * in `HttpClientFactory.json` makes leaving the rest off safe.
 */
@Serializable
data class DailySummaryResponseDto(val result: DailySummaryResultDto)

@Serializable
data class DailySummaryResultDto(
    val todayPriorities: List<String> = emptyList(),
    val keyEvents: List<String> = emptyList(),
    val suggestedFocus: String = "",
)

@Serializable
data class DailyPrioritiesResponseDto(val result: DailyPrioritiesResultDto)

@Serializable
data class DailyPrioritiesResultDto(val priorities: List<DailyPriorityDto> = emptyList())

@Serializable
data class DailyPriorityDto(
    val taskTitle: String,
    val suggestedRank: Int,
    val reason: String = "",
)

@Serializable
data class DailyMotivationResponseDto(val result: DailyMotivationResultDto)

@Serializable
data class DailyMotivationResultDto(val message: String = "")
