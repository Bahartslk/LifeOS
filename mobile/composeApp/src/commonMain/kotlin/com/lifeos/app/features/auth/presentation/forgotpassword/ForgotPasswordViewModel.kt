package com.lifeos.app.features.auth.presentation.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.auth.domain.usecase.RequestPasswordResetUseCase
import com.lifeos.app.features.auth.domain.validation.EmailValidator
import com.lifeos.app.features.auth.domain.validation.errorMessage
import com.lifeos.app.features.auth.presentation.AuthStrings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val requestPasswordReset: RequestPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _actions = Channel<ForgotPasswordAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = event.value, emailError = null)
            }
            ForgotPasswordEvent.SubmitClicked -> submit()
            ForgotPasswordEvent.BackToLoginClicked -> {
                viewModelScope.launch { _actions.send(ForgotPasswordAction.NavigateToLogin) }
            }
        }
    }

    private fun submit() {
        val state = _uiState.value
        val emailError = EmailValidator.validate(state.email).errorMessage(AuthStrings::emailError)

        if (emailError != null) {
            _uiState.value = state.copy(emailError = emailError)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            requestPasswordReset(state.email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure {
                    // requestPasswordReset never fails today — FR-AUTH-04's
                    // backend endpoints don't exist yet, so AuthRepositoryImpl
                    // still stubs this call (see its own KDoc); this branch is
                    // wired for once that real backend work ships.
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }
}
