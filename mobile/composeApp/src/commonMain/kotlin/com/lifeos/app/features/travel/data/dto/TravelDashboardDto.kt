package com.lifeos.app.features.travel.data.dto

import kotlinx.serialization.Serializable

/**
 * `GET /travel/dashboard`'s response — only the fields mobile's
 * [com.lifeos.app.features.travel.domain.model.TravelListData] actually
 * uses (the three status-bucketed trip lists and the total count). Backend's
 * `TravelDashboardResponseDto` also has `nextDestination`/
 * `upcomingItineraryItems`; omitted here since nothing on mobile consumes
 * them yet and `HttpClientFactory.json`'s `ignoreUnknownKeys = true` makes
 * leaving them off this DTO safe, not a partial/lossy decode.
 *
 * `CANCELLED` trips appear in none of these three lists (see the backend's
 * own doc comment on `TravelService.getDashboard`) but still count toward
 * [tripCount] — [com.lifeos.app.features.travel.data.remote.TravelRemoteDataSource]
 * fetches them separately via a filtered `GET /travel/trips?status=CANCELLED`
 * call so Travel List's "past trips" bucket doesn't silently drop them.
 */
@Serializable
data class TravelDashboardDto(
    val upcomingTrips: List<TripDto>,
    val activeTrips: List<TripDto>,
    val completedTrips: List<TripDto>,
    val tripCount: Int,
)
