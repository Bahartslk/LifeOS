package com.lifeos.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lifeos.app.core.designsystem.theme.LifeOSTheme
import com.lifeos.app.core.navigation.LifeOSNavHost
import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.core.storage.ThemePreferenceStorage
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

/**
 * App root, shared by every platform entry point (Android's MainActivity,
 * iOS's MainViewController). Wires the DI scope, the Material 3 theme, and
 * the navigation host together — the three pieces of infrastructure this
 * bootstrap exists to establish. Reads [ThemePreferenceStorage] directly
 * (rather than through Profile's [com.lifeos.app.features.profile.domain.repository.ProfileRepository]
 * abstraction) since this is core app bootstrap, not a feature — Profile
 * is the only *writer* of the preference, but the app root is what applies it.
 */
@Composable
fun App() {
    KoinContext {
        val themePreferenceStorage = koinInject<ThemePreferenceStorage>()
        val themeMode by themePreferenceStorage.observeThemeMode().collectAsState(initial = ThemeMode.SYSTEM)
        val darkTheme = when (themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        LifeOSTheme(darkTheme = darkTheme) {
            LifeOSNavHost()
        }
    }
}
