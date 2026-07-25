package com.lifeos.app.features.travel.data.dto

import kotlinx.serialization.Serializable

/** `POST /travel/trips`'s request body — mirrors backend's `CreateTripDto` exactly. */
@Serializable
data class CreateTripRequestDto(
    val title: String,
    val description: String? = null,
    val destination: String,
    val country: String,
    val startDate: String,
    val endDate: String,
    val coverImageUrl: String? = null,
)
