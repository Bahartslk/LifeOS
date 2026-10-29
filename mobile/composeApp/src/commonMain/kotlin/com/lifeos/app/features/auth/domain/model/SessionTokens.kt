package com.lifeos.app.features.auth.domain.model

/** A fresh access/refresh token pair the backend issued for the current session (e.g. after a password change). */
data class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
)
