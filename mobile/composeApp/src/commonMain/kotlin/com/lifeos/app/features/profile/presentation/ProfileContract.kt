package com.lifeos.app.features.profile.presentation

import com.lifeos.app.core.storage.ThemeMode

/**
 * A load-once, display screen — the same loading/loaded/failed shape every
 * other dashboard-style screen in this app uses. [userName]/[userEmail]
 * come from Authentication's existing session (read-only, per this
 * sprint's "no real editing" scope); [themeMode] is the one field on this
 * screen backed by real, mutable persistence.
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLogoutConfirmationVisible: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface ProfileEvent {
    data object RetryClicked : ProfileEvent
    data object EditProfileClicked : ProfileEvent
    data object SecurityClicked : ProfileEvent
    data object ChangePasswordClicked : ProfileEvent
    data class AppearanceOptionSelected(val mode: ThemeMode) : ProfileEvent
    data object LanguageClicked : ProfileEvent
    data object AiPreferencesClicked : ProfileEvent
    data object TravelPreferencesClicked : ProfileEvent
    data object NotificationSettingsClicked : ProfileEvent
    data object ReminderSettingsClicked : ProfileEvent
    data object PrivacyPolicyClicked : ProfileEvent
    data object TermsOfServiceClicked : ProfileEvent
    data object LogoutClicked : ProfileEvent
    data object LogoutConfirmed : ProfileEvent
    data object LogoutDismissed : ProfileEvent
}

sealed interface ProfileAction {
    data class ShowMessage(val message: String) : ProfileAction
    data object NavigateToLogin : ProfileAction
}
