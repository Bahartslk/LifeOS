package com.lifeos.app.features.auth.domain.model

/**
 * A signed-in session: the tokens the network layer needs
 * (docs/12-project-architecture.md#jwt-strategy) plus the minimal user
 * identity collected at login/register. Framework-agnostic — no
 * serialization or storage annotations belong here; that's the data layer's
 * concern (see data/dto and data/local).
 */
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUser,
)

data class AuthUser(
    val email: String,
    val fullName: String,
)
