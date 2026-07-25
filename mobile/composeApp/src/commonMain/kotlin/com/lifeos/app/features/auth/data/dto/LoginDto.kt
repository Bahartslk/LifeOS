package com.lifeos.app.features.auth.data.dto

import kotlinx.serialization.Serializable

/** Matches `POST /api/v1/auth/login`, per docs/15-api-design.md#authentication. */
@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val email: String,
    val fullName: String,
)
