package com.lifeos.app.features.travel.domain.model

/**
 * The aggregate root for the Travel List screen, per
 * docs/07-functional-requirements.md#travel (FR-TRAVEL-02: "view a list of
 * trips, grouped by upcoming, ongoing, and past"). Mirrors `GET
 * /api/v1/trips` conceptually — see
 * [com.lifeos.app.features.travel.data.repository.FakeTravelRepository]
 * for the real-backend replacement plan.
 */
data class TravelListData(
    val upcomingTrips: List<Trip>,
    val pastTrips: List<Trip>,
    val statistics: TravelStatistics,
    val archivedTripCount: Int,
    val archivedYearRangeLabel: String,
)

/** "Quick Statistics" (travel-list.png requirements): total/countries/upcoming counts. */
data class TravelStatistics(
    val totalTripCount: Int,
    val countriesVisitedCount: Int,
    val upcomingTripCount: Int,
)
