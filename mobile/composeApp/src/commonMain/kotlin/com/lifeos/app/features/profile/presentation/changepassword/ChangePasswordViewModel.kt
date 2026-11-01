package com.lifeos.app.features.profile.presentation.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.auth.domain.validation.PasswordValidator
import com.lifeos.app.features.auth.domain.validation.ValidationError
import com.lifeos.app.features.auth.domain.validation.errorOrNull
import com.lifeos.app.features.profile.domain.model.IncorrectPasswordException
import com.lifeos.app.features.profile.domain.model.TooManyAttemptsException
import com.lifeos.app.features.profile.domain.usecase.ChangePasswordUseCase
import com.lifeos.app.features.profile.presentation.ProfileStrings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The new password follows sign-up's rules ([PasswordValidator], shared with
 * Register) and must differ from the current one. A wrong current password
 * (403) is shown under that field; on success the screen closes.
 */
class ChangePasswordViewModel(
    private val changePassword: ChangePasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _actions = Channel<ChangePasswordAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: ChangePasswordEvent) {
        when (event) {
            is ChangePasswordEvent.CurrentPasswordChanged -> _uiState.update {
                it.copy(currentPassword = event.value, currentPasswordError = null, errorMessage = null)
            }
            is ChangePasswordEvent.NewPasswordChanged -> _uiState.update {
                it.copy(newPassword = event.value, newPasswordError = null, errorMessage = null)
            }
            is ChangePasswordEvent.ConfirmPasswordChanged -> _uiState.update {
                it.copy(confirmPassword = event.value, confirmPasswordError = null, errorMessage = null)
            }
            ChangePasswordEvent.SubmitClicked -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSaving) return
        val validated = state.copy(
            currentPasswordError = if (state.currentPassword.isEmpty()) ProfileStrings.CURRENT_PASSWORD_REQUIRED else null,
            newPasswordError = newPasswordError(state.currentPassword, state.newPassword),
            confirmPasswordError = confirmPasswordError(state.newPassword, state.confirmPassword),
            errorMessage = null,
        )
        if (validated.currentPasswordError != null || validated.newPasswordError != null ||
            validated.confirmPasswordError != null
        ) {
            _uiState.value = validated
            return
        }

        _uiState.value = validated.copy(isSaving = true)
        viewModelScope.launch {
            changePassword(state.currentPassword, state.newPassword)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _actions.send(ChangePasswordAction.PasswordChanged)
                }
                .onFailure { error ->
                    _uiState.update {
                        when (error) {
                            is IncorrectPasswordException -> it.copy(
                                isSaving = false,
                                currentPassword = "",
                                currentPasswordError = ProfileStrings.CURRENT_PASSWORD_WRONG,
                            )
                            is TooManyAttemptsException ->
                                it.copy(isSaving = false, errorMessage = ProfileStrings.CHANGE_PASSWORD_TOO_MANY_ATTEMPTS)
                            else -> it.copy(isSaving = false, errorMessage = ProfileStrings.CHANGE_PASSWORD_ERROR)
                        }
                    }
                }
        }
    }

    private companion object {
        fun newPasswordError(currentPassword: String, newPassword: String): String? =
            when (PasswordValidator.validate(newPassword).errorOrNull()) {
                ValidationError.EMPTY -> ProfileStrings.NEW_PASSWORD_REQUIRED
                ValidationError.PASSWORD_TOO_SHORT -> ProfileStrings.NEW_PASSWORD_TOO_SHORT
                ValidationError.PASSWORD_TOO_LONG -> ProfileStrings.NEW_PASSWORD_TOO_LONG
                null -> if (newPassword == currentPassword) ProfileStrings.NEW_PASSWORD_SAME_AS_CURRENT else null
                else -> ProfileStrings.NEW_PASSWORD_TOO_SHORT
            }

        fun confirmPasswordError(newPassword: String, confirmation: String): String? =
            when (PasswordValidator.validateConfirmation(newPassword, confirmation).errorOrNull()) {
                null -> null
                ValidationError.EMPTY -> ProfileStrings.CONFIRM_PASSWORD_REQUIRED
                else -> ProfileStrings.PASSWORDS_DO_NOT_MATCH
            }
    }
}
