package com.lifeos.app.features.travel.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.features.travel.data.mapper.toCreateDtoOrNull
import com.lifeos.app.features.travel.data.mapper.toDomain
import com.lifeos.app.features.travel.data.mapper.toTravelListData
import com.lifeos.app.features.travel.data.mapper.toTripDetail
import com.lifeos.app.features.travel.data.remote.TravelRemoteDataSource
import com.lifeos.app.features.travel.domain.model.TravelListData
import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.TripNotFoundException
import com.lifeos.app.features.travel.domain.model.UpdateTripRequest
import com.lifeos.app.features.travel.domain.repository.TravelRepository
import io.ktor.http.HttpStatusCode

/**
 * Real implementation of [TravelRepository], backed by the real
 * `/api/v1/travel` endpoints via [TravelRemoteDataSource] — replaces the
 * former `FakeTravelRepository` per that class's own "Replacing this with
 * the real backend" plan. [TravelRepository]'s signature (aside from this
 * iteration's two approved changes — [saveTrip] returning the persisted
 * id, and the new [updateTrip]/[deleteTrip]), and every use case/ViewModel
 * that depends on it, are exactly as they were.
 *
 * [com.lifeos.app.features.travel.domain.repository.TripGenerationRepository]
 * (the "Create Travel (AI)" draft generator) is untouched and out of this
 * iteration's scope — this class only ever reads or persists trips that
 * already exist as a concrete [TripDetail].
 */
class TravelRepositoryImpl(
    private val remoteDataSource: TravelRemoteDataSource,
) : TravelRepository {

    override suspend fun getTravelList(): Result<TravelListData> = try {
        val dashboard = remoteDataSource.getDashboard()
        val cancelledTrips = remoteDataSource.getCancelledTrips()
        Result.success(dashboard.toTravelListData(cancelledTrips))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTripDetail(tripId: String): Result<TripDetail> = try {
        val trip = remoteDataSource.getTrip(tripId)
        val itinerary = remoteDataSource.getItinerary(tripId)
        Result.success(trip.toTripDetail(itinerary))
    } catch (e: ApiException) {
        Result.failure(e.toDomainOrSelf(tripId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun saveTrip(detail: TripDetail): Result<String> {
        val request = detail.toCreateDtoOrNull()
            ?: return Result.failure(
                IllegalArgumentException(
                    "Could not parse trip dates (\"${detail.trip.dateRangeLabel}\") into a real date range.",
                ),
            )
        return try {
            val created = remoteDataSource.createTrip(request)
            Result.success(created.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTrip(tripId: String, request: UpdateTripRequest): Result<Unit> = try {
        remoteDataSource.updateTrip(tripId, request)
        Result.success(Unit)
    } catch (e: ApiException) {
        Result.failure(e.toDomainOrSelf(tripId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteTrip(tripId: String): Result<Unit> = try {
        remoteDataSource.deleteTrip(tripId)
        Result.success(Unit)
    } catch (e: ApiException) {
        Result.failure(e.toDomainOrSelf(tripId))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /** A 404 from any per-trip endpoint means "not this caller's trip" — surfaced as [TripNotFoundException]. */
    private fun ApiException.toDomainOrSelf(tripId: String): Exception =
        if (statusCode == HttpStatusCode.NotFound.value) TripNotFoundException(tripId) else this
}
