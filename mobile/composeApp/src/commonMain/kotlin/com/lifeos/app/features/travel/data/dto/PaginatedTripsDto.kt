package com.lifeos.app.features.travel.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /travel/trips`'s response shape — mirrors backend's
 * `PaginatedTripsResponseDto`/`PaginationMetaDto` exactly. Used only for the
 * `status=CANCELLED` filtered call [com.lifeos.app.features.travel.data.remote.TravelRemoteDataSource]
 * makes to fill the one gap `GET /travel/dashboard` leaves — see
 * [TravelDashboardDto]'s KDoc.
 */
@Serializable
data class PaginatedTripsDto(
    val data: List<TripDto>,
    val meta: PaginationMetaDto,
)

@Serializable
data class PaginationMetaDto(
    val nextCursor: String? = null,
    val limit: Int,
    val hasMore: Boolean,
)
