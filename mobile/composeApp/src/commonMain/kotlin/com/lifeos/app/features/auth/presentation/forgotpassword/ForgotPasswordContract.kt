package com.lifeos.app.features.auth.presentation.forgotpassword

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
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
}
