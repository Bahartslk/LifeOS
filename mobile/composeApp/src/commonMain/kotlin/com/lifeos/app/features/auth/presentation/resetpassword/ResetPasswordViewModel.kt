package com.lifeos.app.features.auth.presentation.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.auth.domain.model.InvalidResetCodeException
import com.lifeos.app.features.auth.domain.model.PasswordResetRateLimitedException
import com.lifeos.app.features.auth.domain.usecase.RequestPasswordResetUseCase
import com.lifeos.app.features.auth.domain.usecase.ResetPasswordUseCase
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
 * Enter the emailed 6-digit code and a new password (the sign-up rules,
 * [PasswordValidator]). A wrong or expired code is shown under the code
 * field; "Kodu tekrar gönder" asks for a new one.
 */
class ResetPasswordViewModel(
    email: String,
    private val resetPassword: ResetPasswordUseCase,
    private val requestPasswordReset: RequestPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState(email = email))
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    private val _actions = Channel<ResetPasswordAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.CodeChanged -> _uiState.update {
                it.copy(code = event.value.filter(Char::isDigit).take(CODE_LENGTH), codeError = null, errorMessage = null)
            }
            is ResetPasswordEvent.NewPasswordChanged -> _uiState.update {
                it.copy(newPassword = event.value, newPasswordError = null, errorMessage = null)
            }
            is ResetPasswordEvent.ConfirmPasswordChanged -> _uiState.update {
                it.copy(confirmPassword = event.value, confirmPasswordError = null, errorMessage = null)
            }
            ResetPasswordEvent.SubmitClicked -> submit()
            ResetPasswordEvent.ResendClicked -> resend()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val validated = state.copy(
            codeError = if (state.code.length == CODE_LENGTH) null else AuthStrings.RESET_PASSWORD_CODE_INVALID_FORMAT,
            newPasswordError = PasswordValidator.validate(state.newPassword).errorMessage(AuthStrings::passwordError),
            confirmPasswordError = PasswordValidator.validateConfirmation(state.newPassword, state.confirmPassword)
                .errorMessage(AuthStrings::confirmPasswordError),
            errorMessage = null,
            infoMessage = null,
        )
        if (validated.codeError != null || validated.newPasswordError != null || validated.confirmPasswordError != null) {
            _uiState.value = validated
            return
        }

        _uiState.value = validated.copy(isSubmitting = true)
        viewModelScope.launch {
            resetPassword(state.email, state.code, state.newPassword)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _actions.send(ResetPasswordAction.NavigateToLogin)
                }
                .onFailure { error ->
                    _uiState.update {
                        when (error) {
                            is InvalidResetCodeException ->
                                it.copy(isSubmitting = false, code = "", codeError = AuthStrings.RESET_PASSWORD_CODE_WRONG)
                            is PasswordResetRateLimitedException ->
                                it.copy(isSubmitting = false, errorMessage = AuthStrings.PASSWORD_RESET_TOO_MANY_ATTEMPTS)
                            else -> it.copy(isSubmitting = false, errorMessage = AuthStrings.RESET_PASSWORD_ERROR)
                        }
                    }
                }
        }
    }

    private fun resend() {
        val state = _uiState.value
        if (state.isResending) return
        _uiState.value = state.copy(isResending = true, errorMessage = null, infoMessage = null)
        viewModelScope.launch {
            requestPasswordReset(state.email)
                .onSuccess {
                    _uiState.update {
                        it.copy(isResending = false, code = "", codeError = null, infoMessage = AuthStrings.RESET_PASSWORD_RESENT)
                    }
                }
                .onFailure { error ->
                    val message = if (error is PasswordResetRateLimitedException) {
                        AuthStrings.PASSWORD_RESET_TOO_MANY_ATTEMPTS
                    } else {
                        AuthStrings.FORGOT_PASSWORD_ERROR
                    }
                    _uiState.update { it.copy(isResending = false, errorMessage = message) }
                }
        }
    }

    private companion object {
        const val CODE_LENGTH = 6
    }
}
