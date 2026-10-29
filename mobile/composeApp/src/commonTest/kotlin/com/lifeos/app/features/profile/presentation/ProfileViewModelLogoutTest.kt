package com.lifeos.app.features.profile.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelLogoutTest {

    private val authRepository = FakeAuthRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun logoutConfirmed_clearsSession_andNavigatesToLogin() = runTest {
        val viewModel = createProfileViewModel(authRepository)

        viewModel.onEvent(ProfileEvent.LogoutClicked)
        viewModel.onEvent(ProfileEvent.LogoutConfirmed)

        assertEquals(ProfileAction.NavigateToLogin, viewModel.actions.first())
        assertEquals(1, authRepository.clearSessionCalls)
        assertFalse(viewModel.uiState.value.isLogoutConfirmationVisible)
    }

    @Test
    fun logoutConfirmedTwice_whileInProgress_logsOutOnlyOnce() = runTest {
        val gate = CompletableDeferred<Unit>()
        authRepository.onClearSession = { gate.await() }
        val viewModel = createProfileViewModel(authRepository)

        viewModel.onEvent(ProfileEvent.LogoutConfirmed)
        viewModel.onEvent(ProfileEvent.LogoutConfirmed)
        assertTrue(viewModel.uiState.value.isLoggingOut)
        gate.complete(Unit)

        assertEquals(ProfileAction.NavigateToLogin, viewModel.actions.first())
        assertEquals(1, authRepository.clearSessionCalls)
    }

    @Test
    fun logoutFailure_staysOnProfile_andShowsError() = runTest {
        authRepository.onClearSession = { throw IllegalStateException("storage unavailable") }
        val viewModel = createProfileViewModel(authRepository)

        viewModel.onEvent(ProfileEvent.LogoutConfirmed)

        assertEquals(ProfileAction.ShowMessage(ProfileStrings.LOGOUT_ERROR_MESSAGE), viewModel.actions.first())
        assertFalse(viewModel.uiState.value.isLoggingOut)
    }
}
