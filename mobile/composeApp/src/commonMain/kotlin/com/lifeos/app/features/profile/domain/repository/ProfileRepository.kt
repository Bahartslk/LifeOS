package com.lifeos.app.features.profile.domain.repository

import com.lifeos.app.core.storage.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Abstracts Profile's one genuinely persisted piece of state — the user's
 * theme override — per docs/12-project-architecture.md#repository-pattern.
 * [ProfileViewModel][com.lifeos.app.features.profile.presentation.ProfileViewModel]
 * depends only on this interface; the concrete implementation
 * ([com.lifeos.app.features.profile.data.repository.ProfileRepositoryImpl])
 * is supplied via Koin.
 *
 * Reuses [ThemeMode] directly rather than a Profile-local duplicate — it's
 * a `core/storage` type, the same shared-infrastructure layer Planner's own
 * domain models already depend on directly (e.g. `kotlinx.datetime.LocalDate`
 * in [com.lifeos.app.features.planner.domain.model.TaskDueDate]), not a
 * feature-specific data type this interface would otherwise be leaking.
 *
 * Every other row on the Profile screen (Edit Profile, Security, Change
 * Password, Language, AI/Travel Preferences, Notification/Reminder
 * settings, Privacy Policy, Terms of Service, Logout) is a placeholder
 * action with nothing to persist — see [com.lifeos.app.features.profile.presentation.ProfileViewModel]'s
 * KDoc for why none of them need a repository method here, per this
 * sprint's "avoid unnecessary repositories" scope.
 */
interface ProfileRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
