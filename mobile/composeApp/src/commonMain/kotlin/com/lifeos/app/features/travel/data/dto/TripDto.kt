package com.lifeos.app.features.travel.data.dto

import kotlinx.serialization.Serializable

/**
 * The wire shape of `TripResponseDto` (backend's `travel/dto/trip-response.dto.ts`).
 * `status` stays a plain string rather than reusing `domain.model.TripStatus`
 * directly — that enum has no `kotlinx.serialization` annotation (domain
 * must not depend on a serialization library); `features/travel/data/mapper/TravelMappers.kt`
 * converts it via `valueOf`, safe because the values are identical between
 * mobile and backend.
 */
@Serializable
data class TripDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val destination: String,
    val country: String,
    val startDate: String,
    val endDate: String,
    val status: String,
    val coverImageUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
)
