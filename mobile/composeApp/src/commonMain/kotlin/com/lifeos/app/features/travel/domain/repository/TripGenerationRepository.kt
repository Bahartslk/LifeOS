package com.lifeos.app.features.travel.domain.repository

import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.TripGenerationRequest

/**
 * Abstracts AI trip generation, per this task's "isolate AI-specific logic
 * from the UI" requirement — [com.lifeos.app.features.travel.presentation.CreateTripViewModel]
 * depends only on this interface, never on how a draft is actually produced.
 *
 * A separate interface from [TravelRepository] (rather than one more method
 * on it): generating a *new* draft from a free-form request is a distinct
 * capability from reading/saving *existing* trips, and — critically for
 * "minimal changes to add a real LLM provider" — swapping the
 * implementation here must never risk touching `getTravelList`/
 * `getTripDetail`/`saveTrip`'s already-working fake behavior.
 *
 * See [com.lifeos.app.features.travel.data.repository.FakeTripGenerationRepository]'s
 * KDoc for the concrete plan to replace the fake implementation with a real
 * backend-backed one.
 */
interface TripGenerationRepository {
    suspend fun generateTrip(request: TripGenerationRequest): Result<TripDetail>
}
