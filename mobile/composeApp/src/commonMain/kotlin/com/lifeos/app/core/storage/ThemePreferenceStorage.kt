package com.lifeos.app.core.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A user's manual theme override. `SYSTEM` (the default) means "follow the
 * OS setting" — the behavior [com.lifeos.app.core.designsystem.theme.LifeOSTheme]
 * already has today via `isSystemInDarkTheme()`.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * Persists the user's theme preference, per
 * docs/12-project-architecture.md#repository-pattern (core infra, not a
 * feature repository). No screen reads or writes this yet — Profile/Settings
 * (docs/05-screen-inventory.md#profile, "Karanlık Mod" toggle) is the future
 * owner of the UI that calls [setThemeMode]. It's built now purely so the
 * storage key exists and is ready when that screen ships, per this sprint's
 * local-storage scope.
 */
class ThemePreferenceStorage(private val preferencesStorage: PreferencesStorage) {

    fun observeThemeMode(): Flow<ThemeMode> =
        preferencesStorage.observeString(KEY_THEME_MODE).map { stored ->
            stored?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        preferencesStorage.putString(KEY_THEME_MODE, mode.name)
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
