package com.lifeos.app.features.travel.data.repository

import com.lifeos.app.features.travel.data.datasource.FakeTravelDataSource
import com.lifeos.app.features.travel.data.datasource.FakeTripGenerationDataSource
import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.TripGenerationRequest
import com.lifeos.app.features.travel.domain.repository.TripGenerationRepository
import kotlinx.coroutines.delay

/**
 * Temporary implementation of [TripGenerationRepository], per this task's
 * "do not call a real AI service" constraint — simulates the multi-step
 * latency an LLM call would have (longer than [FakeTravelRepository]'s
 * plain network delay, since generation is doing more "work"), then returns
 * [FakeTripGenerationDataSource]'s templated content.
 *
 * ## Replacing this with a real LLM provider
 * Nothing outside this one class needs to change — [TripGenerationRepository]
 * is the seam:
 * 1. Add a `TripGenerationRemoteDataSource` using the shared `core/network`
 *    `HttpClient` to call a backend endpoint (e.g. `POST /api/v1/ai/trip-drafts`)
 *    — per docs/09-ai-features.md and docs/12-project-architecture.md#ai-integration-architecture,
 *    the mobile app must never call Gemini/OpenAI/etc. directly; the
 *    backend's `AiProvider` abstraction owns that, exactly so a provider
 *    swap never touches mobile code at all.
 * 2. Rename this class to `TripGenerationRepositoryImpl`, replace the call
 *    to [FakeTripGenerationDataSource] with the remote call, and map the
 *    backend's response DTO into [TripDetail] via a mapper.
 * 3. Update the binding in `features/travel/di/TravelModule.kt`.
 *
 * [travelDataSource] is only used for [FakeTravelDataSource.nextGeneratedTripId] —
 * reused rather than duplicating id-generation logic in a second fake data
 * source; the real implementation would simply use the id the backend
 * assigns instead.
 */
class FakeTripGenerationRepository(
    private val dataSource: FakeTripGenerationDataSource,
    private val travelDataSource: FakeTravelDataSource,
) : TripGenerationRepository {

    override suspend fun generateTrip(request: TripGenerationRequest): Result<TripDetail> {
        delay(FAKE_GENERATION_DELAY_MS)
        val detail = dataSource.generateTripDetail(request, tripId = travelDataSource.nextGeneratedTripId())
        return Result.success(detail)
    }

    private companion object {
        const val FAKE_GENERATION_DELAY_MS = 2500L
    }
}
