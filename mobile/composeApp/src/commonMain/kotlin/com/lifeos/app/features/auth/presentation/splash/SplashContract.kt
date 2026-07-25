package com.lifeos.app.features.auth.presentation.splash

/**
 * Splash has neither a `SplashEvent` nor a `SplashUiState`: it performs one
 * automatic check and never waits for input, and [SplashScreen] renders the
 * same static UI regardless of how far that check has progressed — an
 * empty event type or an unread state flow would exist only to satisfy a
 * template, not a real need. See [SplashAction] for the one thing this
 * screen actually produces.
 */
sealed interface SplashAction {
    data object NavigateToOnboarding : SplashAction
    data object NavigateToLogin : SplashAction
    data object NavigateToHome : SplashAction
}
