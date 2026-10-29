package com.lifeos.app.features.profile.domain.repository

import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in user's profile and preferences, per
 * docs/12-project-architecture.md#repository-pattern. Backed by the
 * backend's `/users` routes; the theme is also kept on the device so it
 * applies before (and without) any network call.
 *
 * Reuses [ThemeMode] directly rather than a Profile-local duplicate: it's a
 * `core/storage` type, shared infrastructure rather than a feature type.
 */
interface ProfileRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    /** Saves the theme on the device, then copies it to the backend (best effort). */
    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun getProfile(): UserProfile

    suspend fun updateDisplayName(displayName: String): UserProfile

    suspend fun updateTimezone(timezone: String)

    /**
     * Permanently deletes the account and all of its data.
     * Throws [com.lifeos.app.features.profile.domain.model.IncorrectPasswordException] on a wrong password and
     * [com.lifeos.app.features.profile.domain.model.TooManyAttemptsException] when rate limited.
     */
    suspend fun deleteAccount(password: String)
}
