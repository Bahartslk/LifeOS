package com.lifeos.app.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.auth.domain.model.InvalidCredentialsException
import com.lifeos.app.features.auth.domain.usecase.LoginUseCase
import com.lifeos.app.features.auth.domain.validation.EmailValidator
import com.lifeos.app.features.auth.domain.validation.PasswordValidator
import com.lifeos.app.features.auth.domain.validation.errorMessage
import com.lifeos.app.features.auth.presentation.AuthStrings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val login: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _actions = Channel<LoginAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = event.value, emailError = null)
            }
            is LoginEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.value, passwordError = null)
            }
            LoginEvent.LoginClicked -> submit()
            LoginEvent.RegisterClicked -> sendAction(LoginAction.NavigateToRegister)
            LoginEvent.ForgotPasswordClicked -> sendAction(LoginAction.NavigateToForgotPassword)
            LoginEvent.ContinueWithGoogleClicked -> sendAction(LoginAction.ShowMessage(AuthStrings.LOGIN_COMING_SOON))
        }
    }

    private fun submit() {
        val state = _uiState.value
        val emailResult = EmailValidator.validate(state.email)
        val passwordResult = PasswordValidator.validate(state.password)

        val emailError = emailResult.errorMessage(AuthStrings::emailError)
        val passwordError = passwordResult.errorMessage(AuthStrings::passwordError)

        if (emailError != null || passwordError != null) {
            _uiState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, emailError = null, passwordError = null)
            login(state.email, state.password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _actions.send(LoginAction.NavigateToHome)
                }
                .onFailure { throwable ->
                    // AuthRepositoryImpl maps a 401 to InvalidCredentialsException;
                    // any other failure (network unreachable, timeout, 5xx) gets its
                    // own honest message instead of also claiming bad credentials.
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    val message = if (throwable is InvalidCredentialsException) {
                        AuthStrings.LOGIN_INVALID_CREDENTIALS
                    } else {
                        AuthStrings.LOGIN_CONNECTION_ERROR
                    }
                    _actions.send(LoginAction.ShowMessage(message))
                }
        }
    }

    private fun sendAction(action: LoginAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
