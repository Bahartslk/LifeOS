package com.lifeos.app.features.auth.data.dto

import kotlinx.serialization.Serializable

/** Matches `POST /api/v1/auth/forgot-password`, per docs/15-api-design.md#authentication. */
@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)
