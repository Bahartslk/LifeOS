package com.lifeos.app.features.auth.presentation.session

import androidx.lifecycle.ViewModel
import com.lifeos.app.features.auth.domain.usecase.ObserveSessionExpiredUseCase
import kotlinx.coroutines.flow.Flow

/**
 * Lets the root navigation react when the session expires while the app is
 * running: [sessionExpired] emits, and the nav host returns to sign-in with a
 * cleared back stack, so no screen is left showing errors it can't recover
 * from.
 */
class SessionGuardViewModel(observeSessionExpired: ObserveSessionExpiredUseCase) : ViewModel() {
    val sessionExpired: Flow<Unit> = observeSessionExpired()
}
