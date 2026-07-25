package com.lifeos.app.features.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.auth.domain.usecase.ObserveOnboardingCompletedUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Splash's only responsibility: decide, once, which of the three graphs to
 * land on. Per docs/04-user-flow.md#entry-points, extended here with the
 * onboarding-completed check the original flow diagram simplified away —
 * without it, a returning user with no active session would see onboarding
 * on every cold launch, defeating the point of persisting it.
 */
class SplashViewModel(
    private val getSession: GetSessionUseCase,
    private val observeOnboardingCompleted: ObserveOnboardingCompletedUseCase,
) : ViewModel() {

    private val _actions = Channel<SplashAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        checkStartDestination()
    }

    private fun checkStartDestination() {
        viewModelScope.launch {
            val session = getSession()
            val action = when {
                session != null -> SplashAction.NavigateToHome
                !observeOnboardingCompleted().first() -> SplashAction.NavigateToOnboarding
                else -> SplashAction.NavigateToLogin
            }
            _actions.send(action)
        }
    }
}
