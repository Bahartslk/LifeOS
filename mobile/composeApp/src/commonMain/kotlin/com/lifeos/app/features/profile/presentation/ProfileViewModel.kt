package com.lifeos.app.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.auth.domain.usecase.LogoutUseCase
import com.lifeos.app.features.auth.domain.validation.NameValidator
import com.lifeos.app.features.auth.domain.validation.ValidationError
import com.lifeos.app.features.auth.domain.validation.ValidationResult
import com.lifeos.app.features.profile.domain.model.IncorrectPasswordException
import com.lifeos.app.features.profile.domain.model.TooManyAttemptsException
import com.lifeos.app.features.profile.domain.usecase.DeleteAccountUseCase
import com.lifeos.app.features.profile.domain.usecase.GetUserProfileUseCase
import com.lifeos.app.features.profile.domain.usecase.ObserveThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SetThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SyncDeviceTimezoneUseCase
import com.lifeos.app.features.profile.domain.usecase.UpdateDisplayNameUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Profile's ViewModel.
 *
 * - The name and email show from the stored session right away, then get
 *   refreshed from the backend profile ([GetUserProfileUseCase]). If that
 *   call fails, the session values stay; the screen still works.
 * - Once the profile loads, the device time zone is copied to the backend
 *   ([SyncDeviceTimezoneUseCase]).
 * - "Profili Düzenle" opens a name dialog ([UpdateDisplayNameUseCase]).
 * - "Şifre Değiştir" opens its own screen.
 * - "Hesabı Sil" opens a password-confirmed dialog ([DeleteAccountUseCase]);
 *   on success the screen navigates to sign-in, which shows the notice.
 * - Logout runs [LogoutUseCase]; server-side revocation inside it is best
 *   effort, so only a failure to clear the local session keeps the user here.
 * - Rows with no feature behind them yet show a "coming soon" message.
 */
class ProfileViewModel(
    private val getSession: GetSessionUseCase,
    private val getUserProfile: GetUserProfileUseCase,
    private val syncDeviceTimezone: SyncDeviceTimezoneUseCase,
    private val observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
    private val updateDisplayName: UpdateDisplayNameUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _actions = Channel<ProfileAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadProfile()
        observeTheme()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.RetryClicked -> loadProfile()
            ProfileEvent.EditProfileClicked -> openEditName()
            is ProfileEvent.EditNameChanged -> updateEditName { it.copy(name = event.value, errorMessage = null) }
            ProfileEvent.EditNameSaved -> saveName()
            ProfileEvent.EditNameDismissed -> dismissEditName()
            ProfileEvent.SecurityClicked -> showMessage(ProfileStrings.SECURITY_COMING_SOON)
            ProfileEvent.ChangePasswordClicked ->
                viewModelScope.launch { _actions.send(ProfileAction.NavigateToChangePassword) }
            is ProfileEvent.AppearanceOptionSelected -> selectThemeMode(event.mode)
            ProfileEvent.LanguageClicked -> showMessage(ProfileStrings.LANGUAGE_COMING_SOON)
            ProfileEvent.AiPreferencesClicked -> showMessage(ProfileStrings.AI_PREFERENCES_COMING_SOON)
            ProfileEvent.TravelPreferencesClicked -> showMessage(ProfileStrings.TRAVEL_PREFERENCES_COMING_SOON)
            ProfileEvent.NotificationSettingsClicked -> showMessage(ProfileStrings.NOTIFICATION_SETTINGS_COMING_SOON)
            ProfileEvent.ReminderSettingsClicked -> showMessage(ProfileStrings.REMINDER_SETTINGS_COMING_SOON)
            ProfileEvent.PrivacyPolicyClicked -> showMessage(ProfileStrings.PRIVACY_POLICY_COMING_SOON)
            ProfileEvent.TermsOfServiceClicked -> showMessage(ProfileStrings.TERMS_OF_SERVICE_COMING_SOON)
            ProfileEvent.LogoutClicked -> _uiState.update { it.copy(isLogoutConfirmationVisible = true) }
            ProfileEvent.LogoutDismissed -> _uiState.update { it.copy(isLogoutConfirmationVisible = false) }
            ProfileEvent.LogoutConfirmed -> confirmLogout()
            ProfileEvent.DeleteAccountClicked -> _uiState.update { it.copy(deleteAccount = DeleteAccountDialogState()) }
            is ProfileEvent.DeletePasswordChanged ->
                updateDeleteAccount { it.copy(password = event.value, errorMessage = null) }
            ProfileEvent.DeleteAccountConfirmed -> confirmDeleteAccount()
            ProfileEvent.DeleteAccountDismissed -> dismissDeleteAccount()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val session = getSession()
            if (session == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = ProfileStrings.LOAD_ERROR_MESSAGE) }
                return@launch
            }
            _uiState.update {
                it.copy(isLoading = false, userName = session.user.fullName.displayName(), userEmail = session.user.email.displayEmail())
            }
            getUserProfile().onSuccess { profile ->
                _uiState.update {
                    it.copy(userName = profile.displayName.displayName(), userEmail = profile.email.displayEmail())
                }
                syncDeviceTimezone(profile.timezone)
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            observeThemeMode().collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    /** Updates local state immediately for a responsive toggle, then persists — [observeTheme]'s collector confirms the same value shortly after. */
    private fun selectThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        viewModelScope.launch { setThemeMode(mode) }
    }

    private fun openEditName() {
        val currentName = _uiState.value.userName.takeUnless { it == ProfileStrings.FALLBACK_NAME }.orEmpty()
        _uiState.update { it.copy(editName = EditNameDialogState(name = currentName)) }
    }

    private fun dismissEditName() {
        if (_uiState.value.editName?.isSaving == true) return
        _uiState.update { it.copy(editName = null) }
    }

    private fun saveName() {
        val dialog = _uiState.value.editName ?: return
        if (dialog.isSaving) return
        val error = nameError(dialog.name)
        if (error != null) {
            updateEditName { it.copy(errorMessage = error) }
            return
        }
        updateEditName { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            updateDisplayName(dialog.name)
                .onSuccess { profile ->
                    _uiState.update { it.copy(editName = null, userName = profile.displayName.displayName()) }
                    _actions.send(ProfileAction.ShowMessage(ProfileStrings.EDIT_NAME_SUCCESS))
                }
                .onFailure {
                    updateEditName { it.copy(isSaving = false, errorMessage = ProfileStrings.EDIT_NAME_ERROR) }
                }
        }
    }

    private fun dismissDeleteAccount() {
        if (_uiState.value.deleteAccount?.isDeleting == true) return
        _uiState.update { it.copy(deleteAccount = null) }
    }

    private fun confirmDeleteAccount() {
        val dialog = _uiState.value.deleteAccount ?: return
        if (dialog.isDeleting) return
        if (dialog.password.isEmpty()) {
            updateDeleteAccount { it.copy(errorMessage = ProfileStrings.DELETE_ACCOUNT_PASSWORD_REQUIRED) }
            return
        }
        updateDeleteAccount { it.copy(isDeleting = true, errorMessage = null) }
        viewModelScope.launch {
            deleteAccount(dialog.password)
                .onSuccess {
                    _uiState.update { it.copy(deleteAccount = null) }
                    _actions.send(ProfileAction.NavigateToLogin)
                }
                .onFailure { error ->
                    val message = when (error) {
                        is IncorrectPasswordException -> ProfileStrings.DELETE_ACCOUNT_WRONG_PASSWORD
                        is TooManyAttemptsException -> ProfileStrings.DELETE_ACCOUNT_TOO_MANY_ATTEMPTS
                        else -> ProfileStrings.DELETE_ACCOUNT_ERROR
                    }
                    updateDeleteAccount { it.copy(password = "", isDeleting = false, errorMessage = message) }
                }
        }
    }

    private fun confirmLogout() {
        if (_uiState.value.isLoggingOut) return
        _uiState.update { it.copy(isLogoutConfirmationVisible = false, isLoggingOut = true) }
        viewModelScope.launch {
            try {
                logout()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoggingOut = false) }
                _actions.send(ProfileAction.ShowMessage(ProfileStrings.LOGOUT_ERROR_MESSAGE))
                return@launch
            }
            _actions.send(ProfileAction.NavigateToLogin)
        }
    }

    private fun updateEditName(transform: (EditNameDialogState) -> EditNameDialogState) {
        _uiState.update { state -> state.copy(editName = state.editName?.let(transform)) }
    }

    private fun updateDeleteAccount(transform: (DeleteAccountDialogState) -> DeleteAccountDialogState) {
        _uiState.update { state -> state.copy(deleteAccount = state.deleteAccount?.let(transform)) }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch { _actions.send(ProfileAction.ShowMessage(message)) }
    }

    private fun String.displayName() = ifBlank { ProfileStrings.FALLBACK_NAME }

    private fun String.displayEmail() = ifBlank { ProfileStrings.FALLBACK_EMAIL }

    private companion object {
        // The backend's displayName limit (profile-fields.dto.ts).
        const val MAX_NAME_LENGTH = 100

        fun nameError(name: String): String? {
            val result = NameValidator.validate(name)
            if (result is ValidationResult.Invalid) {
                return when (result.error) {
                    ValidationError.EMPTY -> ProfileStrings.EDIT_NAME_EMPTY_ERROR
                    else -> ProfileStrings.EDIT_NAME_TOO_SHORT_ERROR
                }
            }
            return if (name.trim().length > MAX_NAME_LENGTH) ProfileStrings.EDIT_NAME_TOO_LONG_ERROR else null
        }
    }
}
