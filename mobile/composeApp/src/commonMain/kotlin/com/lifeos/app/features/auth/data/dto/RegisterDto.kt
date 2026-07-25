package com.lifeos.app.features.auth.data.dto

import kotlinx.serialization.Serializable

/**
 * Matches `POST /api/v1/auth/register`, per docs/15-api-design.md#authentication.
 * `displayName`, not `fullName` — the wire field name the backend's
 * `RegisterDto` actually declares (`backend/src/modules/auth/dto/register.dto.ts`);
 * the mapper at the repository boundary is what keeps the rest of this
 * feature's `fullName` naming intact. The response is a bare token pair
 * (backend's `AuthTokensDto`, no echoed user) — see [AuthTokensResponseDto].
 */
@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String,
)
