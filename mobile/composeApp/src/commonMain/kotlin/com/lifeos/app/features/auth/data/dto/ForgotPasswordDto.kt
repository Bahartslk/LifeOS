package com.lifeos.app.features.auth.data.dto

import kotlinx.serialization.Serializable

/** Matches `POST /api/v1/auth/forgot-password`, per docs/15-api-design.md#password-reset. */
@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)

/** Matches `POST /api/v1/auth/reset-password`. */
@Serializable
data class ResetPasswordRequestDto(
    val email: String,
    val code: String,
    val newPassword: String,
)

/** Both password reset routes answer `{ success: true }`. */
@Serializable
data class PasswordResetResponseDto(
    val success: Boolean,
)
