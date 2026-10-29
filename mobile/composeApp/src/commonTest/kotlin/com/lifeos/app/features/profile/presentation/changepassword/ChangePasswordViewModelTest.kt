package com.lifeos.app.features.profile.presentation.changepassword

import com.lifeos.app.features.auth.domain.model.SessionTokens
import com.lifeos.app.features.profile.domain.model.IncorrectPasswordException
import com.lifeos.app.features.profile.domain.model.TooManyAttemptsException
import com.lifeos.app.features.profile.domain.usecase.ChangePasswordUseCase
import com.lifeos.app.features.profile.presentation.FakeAuthRepository
import com.lifeos.app.features.profile.presentation.FakeProfileRepository
import com.lifeos.app.features.profile.presentation.ProfileStrings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    private val authRepository = FakeAuthRepository()
    private val profileRepository = FakeProfileRepository()
    private val viewModel by lazy { ChangePasswordViewModel(ChangePasswordUseCase(profileRepository, authRepository)) }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fill(current: String = "old-password", new: String = "new-password", confirm: String = new) {
        viewModel.onEvent(ChangePasswordEvent.CurrentPasswordChanged(current))
        viewModel.onEvent(ChangePasswordEvent.NewPasswordChanged(new))
        viewModel.onEvent(ChangePasswordEvent.ConfirmPasswordChanged(confirm))
    }

    @Test
    fun success_changesThePassword_swapsToTheNewTokens_andCloses() = runTest {
        fill()
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)

        assertEquals(ChangePasswordAction.PasswordChanged, viewModel.actions.first())
        assertEquals(listOf("old-password" to "new-password"), profileRepository.passwordChanges)
        assertEquals(SessionTokens(accessToken = "new-access", refreshToken = "new-refresh"), authRepository.sessionTokens)
    }

    @Test
    fun emptyFields_showTheirErrors_withoutCallingTheBackend() {
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)

        val state = viewModel.uiState.value
        assertEquals(ProfileStrings.CURRENT_PASSWORD_REQUIRED, state.currentPasswordError)
        assertEquals(ProfileStrings.NEW_PASSWORD_REQUIRED, state.newPasswordError)
        assertEquals(ProfileStrings.CONFIRM_PASSWORD_REQUIRED, state.confirmPasswordError)
        assertTrue(profileRepository.passwordChanges.isEmpty())
    }

    @Test
    fun aShortNewPassword_followsTheSignUpRule() {
        fill(new = "short")
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)

        assertEquals(ProfileStrings.NEW_PASSWORD_TOO_SHORT, viewModel.uiState.value.newPasswordError)
        assertTrue(profileRepository.passwordChanges.isEmpty())
    }

    @Test
    fun aMismatchedConfirmation_isRejected() {
        fill(confirm = "something-else")
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)

        assertEquals(ProfileStrings.PASSWORDS_DO_NOT_MATCH, viewModel.uiState.value.confirmPasswordError)
        assertTrue(profileRepository.passwordChanges.isEmpty())
    }

    @Test
    fun reusingTheCurrentPassword_isRejected() {
        fill(current = "same-password", new = "same-password")
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)

        assertEquals(ProfileStrings.NEW_PASSWORD_SAME_AS_CURRENT, viewModel.uiState.value.newPasswordError)
        assertTrue(profileRepository.passwordChanges.isEmpty())
    }

    @Test
    fun aWrongCurrentPassword_isShownUnderThatField_andTheSessionIsKept() {
        profileRepository.changePasswordFailure = IncorrectPasswordException()
        fill()
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)

        val state = viewModel.uiState.value
        assertEquals(ProfileStrings.CURRENT_PASSWORD_WRONG, state.currentPasswordError)
        assertEquals("", state.currentPassword)
        assertEquals(false, state.isSaving)
        assertEquals(SessionTokens(accessToken = "a", refreshToken = "r"), authRepository.sessionTokens)
    }

    @Test
    fun rateLimiting_showsTheTooManyAttemptsMessage() {
        profileRepository.changePasswordFailure = TooManyAttemptsException()
        fill()
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)

        assertEquals(ProfileStrings.CHANGE_PASSWORD_TOO_MANY_ATTEMPTS, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun aNetworkFailure_showsTheGenericError_andEditingClearsIt() {
        profileRepository.changePasswordFailure = IOException("offline")
        fill()
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)
        assertEquals(ProfileStrings.CHANGE_PASSWORD_ERROR, viewModel.uiState.value.errorMessage)

        viewModel.onEvent(ChangePasswordEvent.NewPasswordChanged("another-password"))

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun aSecondSubmit_whileSaving_isIgnored() = runTest {
        val gate = CompletableDeferred<Unit>()
        profileRepository.onChangePassword = { gate.await() }
        fill()

        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)
        viewModel.onEvent(ChangePasswordEvent.SubmitClicked)
        assertTrue(viewModel.uiState.value.isSaving)
        gate.complete(Unit)

        assertEquals(ChangePasswordAction.PasswordChanged, viewModel.actions.first())
        assertEquals(1, profileRepository.passwordChanges.size)
    }
}
