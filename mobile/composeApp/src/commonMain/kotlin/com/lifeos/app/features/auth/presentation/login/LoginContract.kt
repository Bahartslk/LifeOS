package com.lifeos.app.features.auth.presentation.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
) {
    val isSubmitEnabled: Boolean get() = !isLoading
}

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object LoginClicked : LoginEvent
    data object RegisterClicked : LoginEvent
    data object ForgotPasswordClicked : LoginEvent
    data object ContinueWithGoogleClicked : LoginEvent
}

sealed interface LoginAction {
    data object NavigateToRegister : LoginAction
    data object NavigateToForgotPassword : LoginAction
    data object NavigateToHome : LoginAction
    data class ShowMessage(val message: String) : LoginAction
}
