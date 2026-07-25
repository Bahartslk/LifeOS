package com.lifeos.app.features.profile.data.repository

import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.core.storage.ThemePreferenceStorage
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

/**
 * The real (not fake) implementation of [ProfileRepository] — theme
 * preference is genuine on-device persistence via [ThemePreferenceStorage]
 * (built during the mobile bootstrap specifically so this screen could
 * activate it, per its own KDoc), not simulated network latency the way
 * every other feature's `Fake*Repository` is. No backend involved, per
 * this sprint's "no backend, no persistence beyond the current project
 * architecture" scope — [ThemePreferenceStorage] already *is* that
 * architecture's local-persistence layer.
 *
 * Named `Impl`, not `Fake`, matching [com.lifeos.app.features.auth.data.repository.OnboardingRepositoryImpl]'s
 * exact precedent for the same reason: it wraps real local storage, not a
 * network stand-in awaiting a backend swap.
 */
class ProfileRepositoryImpl(
    private val themePreferenceStorage: ThemePreferenceStorage,
) : ProfileRepository {

    override fun observeThemeMode(): Flow<ThemeMode> = themePreferenceStorage.observeThemeMode()

    override suspend fun setThemeMode(mode: ThemeMode) {
        themePreferenceStorage.setThemeMode(mode)
    }
}
