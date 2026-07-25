package com.lifeos.app.features.auth.presentation.register

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isTermsAccepted: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
    val isLoading: Boolean = false,
) {
    val isSubmitEnabled: Boolean get() = !isLoading
}

sealed interface RegisterEvent {
    data class FullNameChanged(val value: String) : RegisterEvent
    data class EmailChanged(val value: String) : RegisterEvent
    data class PasswordChanged(val value: String) : RegisterEvent
    data class ConfirmPasswordChanged(val value: String) : RegisterEvent
    data class TermsAcceptedChanged(val accepted: Boolean) : RegisterEvent
    data object RegisterClicked : RegisterEvent
    data object LoginClicked : RegisterEvent
}

sealed interface RegisterAction {
    data object NavigateToLogin : RegisterAction
    data object NavigateToHome : RegisterAction
    data class ShowMessage(val message: String) : RegisterAction
}
