package com.lifeos.app.features.ai.data.repository

import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.ai.data.dto.DailyOverviewResultDto
import com.lifeos.app.features.ai.data.dto.DailyPriorityDto
import com.lifeos.app.features.ai.data.dto.DailySummaryResultDto
import com.lifeos.app.features.ai.data.remote.DailyBriefRemoteDataSource
import com.lifeos.app.features.ai.domain.model.DailyBrief
import com.lifeos.app.features.ai.domain.model.DailyPriority
import com.lifeos.app.features.ai.domain.model.DailySummary
import com.lifeos.app.features.ai.domain.repository.DailyBriefRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

/**
 * Fetches the Daily Brief with a single backend call (`/daily-brief/overview`).
 *
 * - The backend sends `null` for a part the AI got wrong; that part stays
 *   `null` here and the others are shown. The call only fails when the
 *   request fails or all three parts are `null`.
 * - A complete brief is cached in memory for the current day ([today]), so
 *   switching tabs doesn't spend a new AI call. A partial brief is not cached,
 *   so the next load asks again.
 * - [mutex] keeps two overlapping loads from sending duplicate AI requests.
 */
class DailyBriefRepositoryImpl(
    private val remoteDataSource: DailyBriefRemoteDataSource,
    private val today: () -> LocalDate = { AppToday.date },
) : DailyBriefRepository {

    private val mutex = Mutex()
    private var cached: CachedBrief? = null

    override suspend fun getDailyBrief(forceRefresh: Boolean): Result<DailyBrief> = mutex.withLock {
        val date = today()
        cached?.takeIf { !forceRefresh && it.date == date }?.let { return@withLock Result.success(it.brief) }

        val brief = try {
            remoteDataSource.getOverview().result.toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return@withLock Result.failure(e)
        }

        when {
            brief.isComplete -> {
                cached = CachedBrief(date, brief)
                Result.success(brief)
            }
            brief.summary == null && brief.priorities == null && brief.motivation == null ->
                Result.failure(IllegalStateException("Daily brief is empty."))
            else -> Result.success(brief)
        }
    }

    private data class CachedBrief(val date: LocalDate, val brief: DailyBrief)
}

private fun DailyOverviewResultDto.toDomain() = DailyBrief(
    summary = summary?.toDomain(),
    priorities = priorities?.toDomain(),
    motivation = motivation?.message?.trim()?.takeIf { it.isNotEmpty() },
)

private fun DailySummaryResultDto.toDomain() = DailySummary(
    suggestedFocus = suggestedFocus.trim(),
    keyEvents = keyEvents.map { it.trim() }.filter { it.isNotEmpty() },
)

private fun List<DailyPriorityDto>.toDomain(): List<DailyPriority> =
    sortedBy { it.suggestedRank }
        .map { DailyPriority(rank = it.suggestedRank, taskTitle = it.taskTitle.trim(), reason = it.reason.trim()) }
