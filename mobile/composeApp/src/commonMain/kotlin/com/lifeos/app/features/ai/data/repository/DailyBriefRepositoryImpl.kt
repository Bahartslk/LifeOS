package com.lifeos.app.features.ai.data.repository

import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.ai.data.dto.DailyPrioritiesResponseDto
import com.lifeos.app.features.ai.data.dto.DailySummaryResponseDto
import com.lifeos.app.features.ai.data.remote.DailyBriefRemoteDataSource
import com.lifeos.app.features.ai.domain.model.DailyBrief
import com.lifeos.app.features.ai.domain.model.DailyPriority
import com.lifeos.app.features.ai.domain.model.DailySummary
import com.lifeos.app.features.ai.domain.repository.DailyBriefRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

/**
 * Fetches the three Daily Brief parts in parallel and combines them.
 *
 * - A part that fails is left `null`; the call only fails when all three do.
 * - A complete brief is cached in memory for the current day ([today]), so
 *   switching tabs doesn't spend new AI calls. A partial brief is not cached,
 *   so the next load tries the missing parts again.
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

        val (summary, priorities, motivation) = coroutineScope {
            val summary = async { attempt { remoteDataSource.getSummary().toDomain() } }
            val priorities = async { attempt { remoteDataSource.getPriorities().toDomain() } }
            val motivation = async { attempt { remoteDataSource.getMotivation().result.message.trim() } }
            Triple(summary.await(), priorities.await(), motivation.await())
        }

        val brief = DailyBrief(
            summary = summary.getOrNull(),
            priorities = priorities.getOrNull(),
            motivation = motivation.getOrNull()?.takeIf { it.isNotEmpty() },
        )
        when {
            brief.isComplete -> {
                cached = CachedBrief(date, brief)
                Result.success(brief)
            }
            brief.summary == null && brief.priorities == null && brief.motivation == null ->
                Result.failure(summary.exceptionOrNull() ?: IllegalStateException("Daily brief is empty."))
            else -> Result.success(brief)
        }
    }

    /** Like [runCatching], but never swallows coroutine cancellation. */
    private inline fun <T> attempt(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private data class CachedBrief(val date: LocalDate, val brief: DailyBrief)
}

private fun DailySummaryResponseDto.toDomain() = DailySummary(
    suggestedFocus = result.suggestedFocus.trim(),
    keyEvents = result.keyEvents.map { it.trim() }.filter { it.isNotEmpty() },
)

private fun DailyPrioritiesResponseDto.toDomain(): List<DailyPriority> =
    result.priorities
        .sortedBy { it.suggestedRank }
        .map { DailyPriority(rank = it.suggestedRank, taskTitle = it.taskTitle.trim(), reason = it.reason.trim()) }
