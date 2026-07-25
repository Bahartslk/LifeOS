package com.lifeos.app.features.auth.data.dto

import kotlinx.serialization.Serializable

/**
 * Matches backend's bare `AuthTokensDto` — the response shape for
 * `POST /api/v1/auth/register` and `POST /api/v1/auth/refresh` (no user
 * info; see [LoginResponseDto] for the one endpoint that also returns one).
 */
@Serializable
data class TokenPairDto(
    val accessToken: String,
    val refreshToken: String,
)

/** Matches backend's `PublicUserDto` — the only shape a `User` is ever sent over the API in. */
@Serializable
data class PublicUserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: String,
)

/** Matches backend's `LoginResponseDto` — a [TokenPairDto] plus the signed-in [user]. */
@Serializable
data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: PublicUserDto,
)

/** Matches `{ refreshToken }`, the shared body for both `POST /auth/refresh` and `POST /auth/logout`. */
@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

/** Matches backend's `LogoutResponseDto`. */
@Serializable
data class LogoutResponseDto(
    val success: Boolean,
)
