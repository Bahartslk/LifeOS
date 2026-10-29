package com.lifeos.app.features.profile.presentation.changepassword

/** Field errors sit under their field; [errorMessage] is for failures that aren't about one field (429, no connection). */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

sealed interface ChangePasswordEvent {
    data class CurrentPasswordChanged(val value: String) : ChangePasswordEvent
    data class NewPasswordChanged(val value: String) : ChangePasswordEvent
    data class ConfirmPasswordChanged(val value: String) : ChangePasswordEvent
    data object SubmitClicked : ChangePasswordEvent
}

sealed interface ChangePasswordAction {
    /** The password changed; the screen closes and Profile shows the confirmation. */
    data object PasswordChanged : ChangePasswordAction
}
