package com.lifeos.app.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.auth.domain.model.EmailAlreadyRegisteredException
import com.lifeos.app.features.auth.domain.usecase.RegisterUseCase
import com.lifeos.app.features.auth.domain.validation.EmailValidator
import com.lifeos.app.features.auth.domain.validation.NameValidator
import com.lifeos.app.features.auth.domain.validation.PasswordValidator
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
 * Only a genuine [EmailAlreadyRegisteredException] (backend 409, per
 * `AuthRepositoryImpl`) shows the "email already exists" message — any other
 * failure (network unreachable, timeout, 5xx) shows a distinct, honest
 * connection-error message instead. Previously every failure showed "email
 * already exists" regardless of cause, which misreported an unreachable
 * backend (e.g. a release build pointed at an unset/unreachable API host)
 * as a duplicate-email conflict.
 */
class RegisterViewModel(
    private val register: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _actions = Channel<RegisterAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged ->
                _uiState.update { it.copy(fullName = event.value, fullNameError = null) }
            is RegisterEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.value, emailError = null) }
            is RegisterEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value, passwordError = null) }
            is RegisterEvent.ConfirmPasswordChanged ->
                _uiState.update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }
            is RegisterEvent.TermsAcceptedChanged ->
                _uiState.update { it.copy(isTermsAccepted = event.accepted, termsError = null) }
            RegisterEvent.RegisterClicked -> submit()
            RegisterEvent.LoginClicked -> sendAction(RegisterAction.NavigateToLogin)
        }
    }

    private fun submit() {
        val state = _uiState.value

        val nameError = state.fullName.let(NameValidator::validate).errorMessage(AuthStrings::nameError)
        val emailError = state.email.let(EmailValidator::validate).errorMessage(AuthStrings::emailError)
        val passwordError = state.password.let(PasswordValidator::validate).errorMessage(AuthStrings::passwordError)
        val confirmError = PasswordValidator
            .validateConfirmation(state.password, state.confirmPassword)
            .errorMessage(AuthStrings::confirmPasswordError)
        val termsError = if (!state.isTermsAccepted) AuthStrings.TERMS_NOT_ACCEPTED_ERROR else null

        if (listOfNotNull(nameError, emailError, passwordError, confirmError, termsError).isNotEmpty()) {
            _uiState.value = state.copy(
                fullNameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmError,
                termsError = termsError,
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            register(state.fullName, state.email, state.password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _actions.send(RegisterAction.NavigateToHome)
                }
                .onFailure { throwable ->
                    // Railway's runtime logs only show what NestJS's own
                    // Logger.log calls print — there's no request-logging
                    // middleware — so a request that never left the device
                    // and one the backend received but rejected both look
                    // identical there. This line, visible in `adb logcat`,
                    // is what actually tells them apart.
                    println("[RegisterViewModel] register failed: ${throwable::class.simpleName}: ${throwable.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    val message = if (throwable is EmailAlreadyRegisteredException) {
                        AuthStrings.REGISTER_EMAIL_ALREADY_EXISTS
                    } else {
                        AuthStrings.REGISTER_CONNECTION_ERROR
                    }
                    _actions.send(RegisterAction.ShowMessage(message))
                }
        }
    }

    private fun sendAction(action: RegisterAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
