package com.lifeos.app.features.auth.presentation.resetpassword

/** [errorMessage] is for failures not about one field (rate limit, no connection); [infoMessage] confirms a resent code. */
data class ResetPasswordUiState(
    val email: String,
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val codeError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
)

sealed interface ResetPasswordEvent {
    data class CodeChanged(val value: String) : ResetPasswordEvent
    data class NewPasswordChanged(val value: String) : ResetPasswordEvent
    data class ConfirmPasswordChanged(val value: String) : ResetPasswordEvent
    data object SubmitClicked : ResetPasswordEvent
    data object ResendClicked : ResetPasswordEvent
}

sealed interface ResetPasswordAction {
    /** The password was reset; sign-in shows the notice. */
    data object NavigateToLogin : ResetPasswordAction
}
