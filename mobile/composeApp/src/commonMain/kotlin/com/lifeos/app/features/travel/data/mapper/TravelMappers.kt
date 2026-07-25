package com.lifeos.app.features.travel.data.mapper

import com.lifeos.app.core.date.AppDateFormatter
import com.lifeos.app.core.date.AppToday
import com.lifeos.app.features.travel.data.dto.CreateTripRequestDto
import com.lifeos.app.features.travel.data.dto.ItineraryItemDto
import com.lifeos.app.features.travel.data.dto.TravelDashboardDto
import com.lifeos.app.features.travel.data.dto.TripDto
import com.lifeos.app.features.travel.domain.model.AccommodationInfo
import com.lifeos.app.features.travel.domain.model.AiTripSummary
import com.lifeos.app.features.travel.domain.model.BudgetSummary
import com.lifeos.app.features.travel.domain.model.DayPeriod
import com.lifeos.app.features.travel.domain.model.ItineraryActivity
import com.lifeos.app.features.travel.domain.model.ItineraryDay
import com.lifeos.app.features.travel.domain.model.TravelListData
import com.lifeos.app.features.travel.domain.model.TravelStatistics
import com.lifeos.app.features.travel.domain.model.Trip
import com.lifeos.app.features.travel.domain.model.TripDetail
import com.lifeos.app.features.travel.domain.model.TripStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * Data-layer DTO <-> domain-model mapping, per docs/12-project-architecture.md#repository-pattern
 * and the same convention `features/auth/data/mapper/AuthMappers.kt` /
 * `features/planner/data/mapper/PlannerMappers.kt` already established.
 *
 * The backend's `Trip` has no `isAiOptimized`/weather/photo-count columns,
 * and no `AiTripSummary`/`BudgetSummary`/weather-forecast/packing/documents
 * concept at all (mobile-only, currently AI-generated-only content — see
 * this iteration's approved mismatch report); every one of those maps to a
 * neutral default (`false`, `null`, `0`, or an empty list) here. No fake
 * content is synthesized anywhere in this file.
 */
fun TripDto.toDomain(): Trip {
    val start = LocalDate.parse(startDate)
    val end = LocalDate.parse(endDate)
    val statusEnum = TripStatus.valueOf(status)
    val daysUntilStart = AppToday.date.daysUntil(start)
    return Trip(
        id = id,
        destinationCity = destination,
        destinationCountry = country,
        dateRangeLabel = formatDateRangeLabel(start, end),
        status = statusEnum,
        coverImageUrl = coverImageUrl,
        isAiOptimized = false,
        daysUntilStart = if (statusEnum == TripStatus.PLANNED && daysUntilStart >= 0) daysUntilStart else null,
        weatherTemperatureCelsius = null,
        photoCount = null,
    )
}

/** "12-18 Ekim · 7 Gün" for a same-month range, widening to include the month/year on each side once they differ — see [AppDateFormatter]'s existing label primitives, reused rather than duplicated. */
private fun formatDateRangeLabel(start: LocalDate, end: LocalDate): String {
    val dayCount = start.daysUntil(end) + 1
    val range = when {
        start == end -> AppDateFormatter.toShortLabel(start)
        start.year != end.year -> "${AppDateFormatter.toFullLabel(start)} - ${AppDateFormatter.toFullLabel(end)}"
        start.monthNumber != end.monthNumber -> "${AppDateFormatter.toShortLabel(start)} - ${AppDateFormatter.toShortLabel(end)}"
        else -> "${start.dayOfMonth}-${AppDateFormatter.toShortLabel(end)}"
    }
    return "$range · $dayCount Gün"
}

/**
 * Groups the trip's flat itinerary items by real date into [ItineraryDay]s,
 * ordered chronologically; [ItineraryActivity.period] is derived from each
 * item's real `startTime` (a null start time defaults to
 * [DayPeriod.MORNING], matching an all-day item having no more specific
 * placement). [ItineraryDay.title] is the real formatted date — never an
 * invented narrative title like the old fake data's "Varış ve Mağara Otele
 * Giriş", since the backend has no such field. `photoUrls` is always empty
 * and `bookingActionLabel` always `null` — no backend support for either.
 */
fun List<ItineraryItemDto>.toItineraryDays(): List<ItineraryDay> = this
    .groupBy { it.date }
    .toSortedMap()
    .values
    .mapIndexed { index, items ->
        ItineraryDay(
            dayNumber = index + 1,
            title = AppDateFormatter.toFullLabel(LocalDate.parse(items.first().date)),
            activities = items.sortedBy { it.orderIndex }.map { item ->
                ItineraryActivity(
                    period = periodFor(item.startTime),
                    description = item.description?.let { "${item.title} — $it" } ?: item.title,
                )
            },
            photoUrls = emptyList(),
            bookingActionLabel = null,
        )
    }

private fun periodFor(startTime: String?): DayPeriod {
    val hour = startTime?.substringBefore(":")?.toIntOrNull() ?: return DayPeriod.MORNING
    return when {
        hour < 12 -> DayPeriod.MORNING
        hour < 18 -> DayPeriod.AFTERNOON
        else -> DayPeriod.EVENING
    }
}

/**
 * Combines the trip and its itinerary into the full [TripDetail] aggregate.
 * [AccommodationInfo] (non-nullable) is best-effort mapped from the first
 * `ACCOMMODATION`-typed itinerary item if one exists, or left as blank
 * strings otherwise — an honest "nothing to show" per this iteration's
 * approved graceful-degradation policy, not a placeholder hotel name.
 * [AiTripSummary]/[BudgetSummary] (also non-nullable) are always the
 * neutral/zero defaults; [TripDetail.flight] stays `null` (already a
 * documented-as-valid "no flight yet" state — see [com.lifeos.app.features.travel.domain.model.TripDetail]'s
 * own KDoc), since a single `location` string can't be safely split into
 * separate departure/arrival airports, terminal, and gate.
 */
fun TripDto.toTripDetail(itineraryItems: List<ItineraryItemDto>): TripDetail {
    val accommodationItem = itineraryItems.firstOrNull { it.type == "ACCOMMODATION" }
    return TripDetail(
        trip = toDomain(),
        aiSummary = AiTripSummary(highlightMessage = "", weatherTemperatureCelsius = 0, windSpeedKmh = 0),
        itinerary = itineraryItems.toItineraryDays(),
        flight = null,
        accommodation = accommodationItem?.let {
            AccommodationInfo(
                hotelName = it.title,
                address = it.location.orEmpty(),
                roomType = "",
                checkInLabel = it.startTime.orEmpty(),
                checkOutLabel = it.endTime.orEmpty(),
            )
        } ?: AccommodationInfo(hotelName = "", address = "", roomType = "", checkInLabel = "", checkOutLabel = ""),
        weatherForecast = emptyList(),
        budget = BudgetSummary(
            totalBudget = 0,
            spentAmount = 0,
            accommodationCost = 0,
            transportationCost = 0,
            foodCost = 0,
            activitiesCost = 0,
        ),
        packingCategories = emptyList(),
        documents = emptyList(),
        notes = "",
    )
}

/**
 * [TripDetail] -> [CreateTripRequestDto], for [com.lifeos.app.features.travel.data.repository.TravelRepositoryImpl.saveTrip].
 *
 * Temporary workaround (see this iteration's QA report): [Trip] has no raw
 * `startDate`/`endDate` fields — only the pre-formatted [Trip.dateRangeLabel]
 * — because "Create Travel (AI)" (out of this iteration's scope) has never
 * needed real dates before. [FakeTripGenerationDataSource][com.lifeos.app.features.travel.data.datasource.FakeTripGenerationDataSource]
 * builds that label as the *exact, literal* `"$startDateLabel - $endDateLabel"`
 * echo of what the user typed into `TravelDatesSection`'s two free-text
 * fields (placeholder format "15.09.2026", i.e. "dd.MM.yyyy") — so splitting
 * on `" - "` and parsing each half in that format recovers the original
 * input losslessly, rather than reverse-parsing an ambiguous display
 * string. Returns `null` if either half doesn't parse, so the caller can
 * fail the save cleanly instead of sending a malformed request the backend
 * would reject. `title` has no mobile-side source at all (no separate
 * `Trip.title` field exists) — derived from the real destination city,
 * matching backend's own `CreateTripDto` example shape ("Kapadokya
 * Seyahati").
 */
fun TripDetail.toCreateDtoOrNull(): CreateTripRequestDto? {
    val parts = trip.dateRangeLabel.split(" - ", limit = 2)
    val startText = parts.getOrNull(0) ?: return null
    val endText = parts.getOrNull(1) ?: startText
    val startDate = parseDdMmYyyy(startText) ?: return null
    val endDate = parseDdMmYyyy(endText) ?: return null

    return CreateTripRequestDto(
        title = "${trip.destinationCity} Seyahati",
        description = null,
        destination = trip.destinationCity,
        country = trip.destinationCountry,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        coverImageUrl = trip.coverImageUrl,
    )
}

/**
 * [TravelDashboardDto] + the separately-fetched cancelled trips ->
 * [TravelListData]. `PLANNED`/`ONGOING` bucket into [TravelListData.upcomingTrips],
 * `COMPLETED`/`CANCELLED` into [TravelListData.pastTrips] — mobile's model
 * only has two buckets, so a currently-in-progress trip reads as "upcoming"
 * (not yet finished) rather than "past". [TravelStatistics.countriesVisitedCount]
 * is the real distinct-country count across every trip returned here;
 * [TravelListData.archivedTripCount]/[TravelListData.archivedYearRangeLabel]
 * are `0`/`""` — no backend "archived" concept exists, and the only screen
 * that reads them already shows a "coming soon" message when tapped.
 */
fun TravelDashboardDto.toTravelListData(cancelledTrips: List<TripDto>): TravelListData {
    val allDtos = upcomingTrips + activeTrips + completedTrips + cancelledTrips
    val upcoming = (upcomingTrips + activeTrips).map { it.toDomain() }
    val past = (completedTrips + cancelledTrips).map { it.toDomain() }
    return TravelListData(
        upcomingTrips = upcoming,
        pastTrips = past,
        statistics = TravelStatistics(
            totalTripCount = tripCount,
            countriesVisitedCount = allDtos.map { it.country }.distinct().size,
            upcomingTripCount = upcoming.size,
        ),
        archivedTripCount = 0,
        archivedYearRangeLabel = "",
    )
}

private fun parseDdMmYyyy(text: String): LocalDate? {
    val parts = text.trim().split(".")
    if (parts.size != 3) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    return try {
        LocalDate(year, month, day)
    } catch (e: IllegalArgumentException) {
        null
    }
}
