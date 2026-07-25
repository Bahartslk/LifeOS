package com.lifeos.app.features.travel.domain.model

/**
 * "Flight Information" (this task's requirement; travel-details.png itself
 * only shows a "Manage Booking" button, not a flight detail breakdown — see
 * this feature's "Deviations from Stitch" note). [gate] is nullable per the
 * task's explicit "Gate (placeholder)" spec: airlines commonly don't assign
 * a gate until close to departure.
 */
data class FlightInfo(
    val airline: String,
    val flightNumber: String,
    val departureAirport: String,
    val departureTimeLabel: String,
    val arrivalAirport: String,
    val arrivalTimeLabel: String,
    val terminal: String,
    val gate: String?,
)
