package com.lifeos.app.features.auth.presentation.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.auth.domain.model.PasswordResetRateLimitedException
import com.lifeos.app.features.auth.domain.usecase.RequestPasswordResetUseCase
import com.lifeos.app.features.auth.domain.validation.EmailValidator
import com.lifeos.app.features.auth.domain.validation.errorMessage
import com.lifeos.app.features.auth.presentation.AuthStrings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Asks the backend to email a reset code. The backend answers the same way
 * whether or not the address has an account, so a success always moves on
 * to the code screen.
 */
class ForgotPasswordViewModel(
    private val requestPasswordReset: RequestPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _actions = Channel<ForgotPasswordAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> _uiState.update {
                it.copy(email = event.value, emailError = null, errorMessage = null)
            }
            ForgotPasswordEvent.SubmitClicked -> submit()
            ForgotPasswordEvent.BackToLoginClicked -> {
                viewModelScope.launch { _actions.send(ForgotPasswordAction.NavigateToLogin) }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isLoading) return
        // Trimmed first: keyboards often add a trailing space after an address.
        val email = state.email.trim()
        val emailError = EmailValidator.validate(email).errorMessage(AuthStrings::emailError)
        if (emailError != null) {
            _uiState.value = state.copy(emailError = emailError)
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            requestPasswordReset(email)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _actions.send(ForgotPasswordAction.NavigateToResetPassword(email))
                }
                .onFailure { error ->
                    val message = if (error is PasswordResetRateLimitedException) {
                        AuthStrings.PASSWORD_RESET_TOO_MANY_ATTEMPTS
                    } else {
                        AuthStrings.FORGOT_PASSWORD_ERROR
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }
}
