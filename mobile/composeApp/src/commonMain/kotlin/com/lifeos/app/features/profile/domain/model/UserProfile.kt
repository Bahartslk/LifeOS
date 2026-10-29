package com.lifeos.app.features.profile.domain.model

/** The signed-in user's profile, as the backend stores it. */
data class UserProfile(
    val email: String,
    val displayName: String,
    val timezone: String,
)
