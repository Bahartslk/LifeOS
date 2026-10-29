package com.lifeos.app.features.profile.presentation

import com.lifeos.app.core.storage.ThemeMode

/**
 * The same loading/loaded/failed shape every other dashboard-style screen
 * in this app uses. [userName]/[userEmail] first come from the stored
 * session, then from the backend profile once it loads. [editName] and
 * [deleteAccount] are the two dialogs' state; `null` means closed.
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val userEmail: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLogoutConfirmationVisible: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null,
    val editName: EditNameDialogState? = null,
    val deleteAccount: DeleteAccountDialogState? = null,
)

data class EditNameDialogState(
    val name: String,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

data class DeleteAccountDialogState(
    val password: String = "",
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
)

sealed interface ProfileEvent {
    data object RetryClicked : ProfileEvent
    data object EditProfileClicked : ProfileEvent
    data class EditNameChanged(val value: String) : ProfileEvent
    data object EditNameSaved : ProfileEvent
    data object EditNameDismissed : ProfileEvent
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
    data object DeleteAccountClicked : ProfileEvent
    data class DeletePasswordChanged(val value: String) : ProfileEvent
    data object DeleteAccountConfirmed : ProfileEvent
    data object DeleteAccountDismissed : ProfileEvent
}

sealed interface ProfileAction {
    data class ShowMessage(val message: String) : ProfileAction
    data object NavigateToLogin : ProfileAction
    data object NavigateToChangePassword : ProfileAction
}
