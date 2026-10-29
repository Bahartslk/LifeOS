package com.lifeos.app.features.auth.presentation.forgotpassword

/** [errorMessage] is for failures not about the field itself (rate limit, no connection). */
data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
) {
    val isSubmitEnabled: Boolean get() = !isLoading
}

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val value: String) : ForgotPasswordEvent
    data object SubmitClicked : ForgotPasswordEvent
    data object BackToLoginClicked : ForgotPasswordEvent
}

sealed interface ForgotPasswordAction {
    data object NavigateToLogin : ForgotPasswordAction

    /** The code was requested; continue on the screen where it is entered. */
    data class NavigateToResetPassword(val email: String) : ForgotPasswordAction
}
