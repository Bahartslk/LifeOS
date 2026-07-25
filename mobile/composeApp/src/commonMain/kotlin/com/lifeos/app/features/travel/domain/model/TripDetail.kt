package com.lifeos.app.features.travel.domain.model

/**
 * The Travel Detail aggregate, per docs/07-functional-requirements.md#travel
 * (trip itinerary and detail). Deliberately wraps the existing [Trip]
 * rather than duplicating its fields — the Hero Section's destination
 * name, country, dates, countdown, and AI badge all come straight from
 * [trip], per this task's "Reuse Trip wherever possible" rule. Everything
 * here is detail-specific information [Trip] has no reason to carry for
 * every list-row use (list rows never show a flight number or a packing
 * checklist).
 *
 * [flight] is nullable: a trip fresh out of "Create Travel (AI)" is a
 * generated draft with no booked flight yet, which is a real, honest state
 * — not a missing-data bug — so the type reflects it rather than forcing a
 * placeholder [FlightInfo]. [documents] being an empty list already covers
 * the equivalent "no documents yet" case without a type change.
 */
data class TripDetail(
    val trip: Trip,
    val aiSummary: AiTripSummary,
    val itinerary: List<ItineraryDay>,
    val flight: FlightInfo?,
    val accommodation: AccommodationInfo,
    val weatherForecast: List<DailyWeather>,
    val budget: BudgetSummary,
    val packingCategories: List<PackingCategory>,
    val documents: List<TravelDocument>,
    val notes: String,
)
