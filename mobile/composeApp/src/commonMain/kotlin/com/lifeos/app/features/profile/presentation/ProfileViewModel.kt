package com.lifeos.app.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.profile.domain.usecase.ObserveThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Profile's own ViewModel. Reads Authentication's existing session
 * ([GetSessionUseCase], reused directly — no `ProfileUser`/duplicate
 * identity model per this sprint's scope) for [ProfileUiState.userName]/
 * [ProfileUiState.userEmail], read-only ("Do NOT implement real editing").
 * [com.lifeos.app.features.profile.domain.repository.ProfileRepository]
 * is the single genuinely mutable, persisted piece of state this screen
 * owns — the theme override — via [ObserveThemeModeUseCase]/[SetThemeModeUseCase].
 *
 * Every other event ([ProfileEvent.EditProfileClicked], [ProfileEvent.SecurityClicked],
 * [ProfileEvent.ChangePasswordClicked], [ProfileEvent.LanguageClicked],
 * [ProfileEvent.AiPreferencesClicked], [ProfileEvent.TravelPreferencesClicked],
 * [ProfileEvent.NotificationSettingsClicked], [ProfileEvent.ReminderSettingsClicked],
 * [ProfileEvent.PrivacyPolicyClicked], [ProfileEvent.TermsOfServiceClicked])
 * resolves to a "coming soon" message — the same graceful-degradation
 * pattern every other not-yet-implemented action in this app already uses.
 * [ProfileEvent.LogoutClicked] shows a real [com.lifeos.app.core.designsystem.components.ConfirmationDialog]
 * (this screen should *feel* complete), but confirming it never calls
 * [com.lifeos.app.features.auth.domain.repository.AuthRepository.clearSession]
 * or navigates away — "Do NOT implement real logout" is this sprint's
 * explicit, repeated instruction.
 */
class ProfileViewModel(
    private val getSession: GetSessionUseCase,
    private val observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _actions = Channel<ProfileAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadSession()
        observeTheme()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.RetryClicked -> loadSession()
            ProfileEvent.EditProfileClicked -> showMessage(ProfileStrings.EDIT_PROFILE_COMING_SOON)
            ProfileEvent.SecurityClicked -> showMessage(ProfileStrings.SECURITY_COMING_SOON)
            ProfileEvent.ChangePasswordClicked -> showMessage(ProfileStrings.CHANGE_PASSWORD_COMING_SOON)
            is ProfileEvent.AppearanceOptionSelected -> selectThemeMode(event.mode)
            ProfileEvent.LanguageClicked -> showMessage(ProfileStrings.LANGUAGE_COMING_SOON)
            ProfileEvent.AiPreferencesClicked -> showMessage(ProfileStrings.AI_PREFERENCES_COMING_SOON)
            ProfileEvent.TravelPreferencesClicked -> showMessage(ProfileStrings.TRAVEL_PREFERENCES_COMING_SOON)
            ProfileEvent.NotificationSettingsClicked -> showMessage(ProfileStrings.NOTIFICATION_SETTINGS_COMING_SOON)
            ProfileEvent.ReminderSettingsClicked -> showMessage(ProfileStrings.REMINDER_SETTINGS_COMING_SOON)
            ProfileEvent.PrivacyPolicyClicked -> showMessage(ProfileStrings.PRIVACY_POLICY_COMING_SOON)
            ProfileEvent.TermsOfServiceClicked -> showMessage(ProfileStrings.TERMS_OF_SERVICE_COMING_SOON)
            ProfileEvent.LogoutClicked -> {
                _uiState.value = _uiState.value.copy(isLogoutConfirmationVisible = true)
            }
            ProfileEvent.LogoutDismissed -> {
                _uiState.value = _uiState.value.copy(isLogoutConfirmationVisible = false)
            }
            ProfileEvent.LogoutConfirmed -> confirmLogout()
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val session = getSession()
            _uiState.value = if (session != null) {
                _uiState.value.copy(
                    isLoading = false,
                    userName = session.user.fullName.ifBlank { ProfileStrings.FALLBACK_NAME },
                    userEmail = session.user.email.ifBlank { ProfileStrings.FALLBACK_EMAIL },
                )
            } else {
                _uiState.value.copy(isLoading = false, errorMessage = ProfileStrings.LOAD_ERROR_MESSAGE)
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            observeThemeMode().collect { mode ->
                _uiState.value = _uiState.value.copy(themeMode = mode)
            }
        }
    }

    /** Updates local state immediately for a responsive toggle, then persists — [observeTheme]'s collector confirms the same value shortly after. */
    private fun selectThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        viewModelScope.launch { setThemeMode(mode) }
    }

    private fun confirmLogout() {
        _uiState.value = _uiState.value.copy(isLogoutConfirmationVisible = false)
        showMessage(ProfileStrings.LOGOUT_COMING_SOON)
    }

    private fun showMessage(message: String) {
        sendAction(ProfileAction.ShowMessage(message))
    }

    private fun sendAction(action: ProfileAction) {
        viewModelScope.launch { _actions.send(action) }
    }
}
