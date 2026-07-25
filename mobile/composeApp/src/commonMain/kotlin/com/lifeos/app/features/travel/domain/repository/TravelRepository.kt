package com.lifeos.app.features.travel.domain.repository

import com.lifeos.app.features.travel.domain.model.TravelListData
import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.UpdateTripRequest

/**
 * Abstracts fetching and persisting Travel data, per
 * docs/12-project-architecture.md#repository-pattern. ViewModels depend
 * only on this interface; [com.lifeos.app.features.travel.data.repository.TravelRepositoryImpl]
 * is bound in `di/TravelModule.kt` (Iteration 3 of the backend integration
 * migration, replacing the former `FakeTravelRepository`).
 *
 * [getTripDetail] and [saveTrip] extend this same interface rather than
 * new repositories, per this task's "reuse the existing Travel module"
 * scope — one repository for the whole Travel feature (list, detail, and
 * saving), matching how docs/15-api-design.md already groups list and
 * detail under the same `/trips` resource (`GET /trips`, `GET /trips/:id`,
 * and — for [saveTrip] — `POST /trips`).
 *
 * [saveTrip] returns the persisted trip's real id (Iteration 3's change
 * from `Result<Unit>`) — a real backend always assigns the id server-side
 * on creation, unlike the former fake implementation where the id was
 * already known client-side before saving; [com.lifeos.app.features.travel.presentation.CreateTripViewModel]
 * needs the real id to navigate to the newly created trip's detail screen.
 *
 * [updateTrip]/[deleteTrip] (Iteration 3's addition) have no UI trigger yet
 * — added purely to implement the backend capability, the same
 * capability-without-a-UI-hook precedent [com.lifeos.app.features.auth.domain.usecase.LogoutUseCase]
 * already established for Authentication.
 *
 * Generating a *new* draft is deliberately a separate concern — see
 * [com.lifeos.app.features.travel.domain.repository.TripGenerationRepository] —
 * this interface only ever reads or persists trips that already exist as a
 * concrete [TripDetail], whether fetched from a backend or produced by
 * generation and confirmed via [saveTrip].
 */
interface TravelRepository {
    suspend fun getTravelList(): Result<TravelListData>

    suspend fun getTripDetail(tripId: String): Result<TripDetail>

    suspend fun saveTrip(detail: TripDetail): Result<String>

    suspend fun updateTrip(tripId: String, request: UpdateTripRequest): Result<Unit>

    suspend fun deleteTrip(tripId: String): Result<Unit>
}
