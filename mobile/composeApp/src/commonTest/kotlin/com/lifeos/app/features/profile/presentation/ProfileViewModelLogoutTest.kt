package com.lifeos.app.features.profile.presentation

import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.AuthUser
import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.auth.domain.usecase.LogoutUseCase
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import com.lifeos.app.features.profile.domain.usecase.ObserveThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val viewModel = createViewModel()

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
        val viewModel = createViewModel()

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
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.LogoutConfirmed)

        assertEquals(ProfileAction.ShowMessage(ProfileStrings.LOGOUT_ERROR_MESSAGE), viewModel.actions.first())
        assertFalse(viewModel.uiState.value.isLoggingOut)
    }

    private fun createViewModel(): ProfileViewModel {
        val profileRepository = FakeProfileRepository()
        return ProfileViewModel(
            getSession = GetSessionUseCase(authRepository),
            observeThemeMode = ObserveThemeModeUseCase(profileRepository),
            setThemeMode = SetThemeModeUseCase(profileRepository),
            logout = LogoutUseCase(authRepository),
        )
    }

    private class FakeAuthRepository : AuthRepository {
        var clearSessionCalls = 0
        var onClearSession: suspend () -> Unit = {}

        override suspend fun login(email: String, password: String): Result<AuthSession> = error("unused")
        override suspend fun register(fullName: String, email: String, password: String): Result<AuthSession> =
            error("unused")
        override suspend fun requestPasswordReset(email: String): Result<Unit> = error("unused")
        override suspend fun getSession(): AuthSession =
            AuthSession(accessToken = "a", refreshToken = "r", user = AuthUser(email = "u@example.com", fullName = "U"))

        override suspend fun clearSession() {
            clearSessionCalls++
            onClearSession()
        }
    }

    private class FakeProfileRepository : ProfileRepository {
        private val mode = MutableStateFlow(ThemeMode.SYSTEM)
        override fun observeThemeMode(): Flow<ThemeMode> = mode
        override suspend fun setThemeMode(mode: ThemeMode) {
            this.mode.value = mode
        }
    }
}
