package com.lifeos.app.features.travel.data.dto

import kotlinx.serialization.Serializable

/** The wire shape of `ItineraryItemResponseDto` (backend's `travel/dto/itinerary-item-response.dto.ts`). `type` stays a plain string for the same reason as `TripDto.status` — see that file's KDoc. */
@Serializable
data class ItineraryItemDto(
    val id: String,
    val tripId: String,
    val title: String,
    val description: String? = null,
    val date: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val location: String? = null,
    val orderIndex: Int,
    val type: String,
    val transportationType: String? = null,
    val accommodationType: String? = null,
    val taskId: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
