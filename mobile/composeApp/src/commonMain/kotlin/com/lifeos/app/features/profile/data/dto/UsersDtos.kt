package com.lifeos.app.features.profile.data.dto

import kotlinx.serialization.Serializable

/**
 * The fields of `GET /users/me`'s response this app reads. The backend sends
 * more (bio, language, notification preferences, ...); the shared JSON
 * config ignores unknown keys.
 */
@Serializable
data class UserProfileDto(
    val email: String,
    val displayName: String,
    val timezone: String,
    val themePreference: String,
)

@Serializable
data class UpdateDisplayNameRequestDto(val displayName: String)

/**
 * One request type per preference field: the shared JSON config encodes
 * defaults, so a nullable "unchanged" field would reach the backend as an
 * explicit `null` instead of being left out.
 */
@Serializable
data class UpdateThemePreferenceRequestDto(val themePreference: String)

@Serializable
data class UpdateTimezoneRequestDto(val timezone: String)

@Serializable
data class DeleteAccountRequestDto(val password: String)

@Serializable
data class DeleteAccountResponseDto(val success: Boolean)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String,
)

/** `PATCH /users/me/password`'s response: the new session's tokens. */
@Serializable
data class SessionTokensDto(
    val accessToken: String,
    val refreshToken: String,
)
