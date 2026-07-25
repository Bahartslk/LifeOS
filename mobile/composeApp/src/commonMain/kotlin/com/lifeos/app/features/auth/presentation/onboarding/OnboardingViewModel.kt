package com.lifeos.app.features.auth.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.auth.domain.usecase.CompleteOnboardingUseCase
import com.lifeos.app.features.auth.presentation.AuthStrings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState(pages = onboardingPages()))
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _actions = Channel<OnboardingAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.PageChanged -> {
                _uiState.value = _uiState.value.copy(currentPage = event.page)
            }
            OnboardingEvent.SkipClicked -> completeAndNavigate()
            OnboardingEvent.GetStartedClicked -> completeAndNavigate()
            OnboardingEvent.NextClicked -> {
                val state = _uiState.value
                if (state.isLastPage) {
                    completeAndNavigate()
                } else {
                    val nextPage = state.currentPage + 1
                    _uiState.value = state.copy(currentPage = nextPage)
                    sendAction(OnboardingAction.ScrollToPage(nextPage))
                }
            }
        }
    }

    private fun completeAndNavigate() {
        viewModelScope.launch {
            completeOnboarding()
            _actions.send(OnboardingAction.NavigateToLogin)
        }
    }

    private fun sendAction(action: OnboardingAction) {
        viewModelScope.launch { _actions.send(action) }
    }

    private fun onboardingPages(): List<OnboardingPageContent> = listOf(
        OnboardingPageContent(
            eyebrow = AuthStrings.ONBOARDING_PAGE1_EYEBROW,
            title = AuthStrings.ONBOARDING_PAGE1_TITLE,
            description = AuthStrings.ONBOARDING_PAGE1_DESCRIPTION,
        ),
        OnboardingPageContent(
            eyebrow = null,
            title = AuthStrings.ONBOARDING_PAGE2_TITLE,
            description = AuthStrings.ONBOARDING_PAGE2_DESCRIPTION,
        ),
        OnboardingPageContent(
            eyebrow = AuthStrings.ONBOARDING_PAGE3_EYEBROW,
            title = AuthStrings.ONBOARDING_PAGE3_TITLE,
            description = AuthStrings.ONBOARDING_PAGE3_DESCRIPTION,
        ),
    )
}
