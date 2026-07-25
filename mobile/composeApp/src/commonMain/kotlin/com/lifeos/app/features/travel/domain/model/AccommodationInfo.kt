package com.lifeos.app.features.travel.domain.model

/** "Accommodation" (this task's requirement): the cave hotel in travel-details.png. */
data class AccommodationInfo(
    val hotelName: String,
    val address: String,
    val roomType: String,
    val checkInLabel: String,
    val checkOutLabel: String,
)
