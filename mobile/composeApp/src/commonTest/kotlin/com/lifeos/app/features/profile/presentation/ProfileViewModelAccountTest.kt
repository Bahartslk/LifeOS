package com.lifeos.app.features.profile.presentation

import com.lifeos.app.features.auth.data.SessionExpiryRepositoryImpl
import com.lifeos.app.features.auth.domain.model.SignInNotice
import com.lifeos.app.features.profile.domain.model.IncorrectPasswordException
import com.lifeos.app.features.profile.domain.model.TooManyAttemptsException
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

/** Profile's backend data, name editing and account deletion. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelAccountTest {

    private val authRepository = FakeAuthRepository()
    private val profileRepository = FakeProfileRepository()
    private val sessionExpiry = SessionExpiryRepositoryImpl()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(deviceTimezone: String = "Europe/Istanbul") =
        createProfileViewModel(authRepository, profileRepository, sessionExpiry, deviceTimezone)

    // Loading

    @Test
    fun load_showsTheBackendProfile() {
        val state = createViewModel().uiState.value

        assertEquals("Backend Name", state.userName)
        assertEquals("u@example.com", state.userEmail)
        assertNull(state.errorMessage)
    }

    @Test
    fun load_keepsTheSessionName_whenTheBackendIsUnreachable() {
        profileRepository.getProfileFailure = IOException("offline")

        val state = createViewModel().uiState.value

        assertEquals("Session Name", state.userName)
        assertNull(state.errorMessage)
    }

    @Test
    fun load_copiesADifferentDeviceTimezoneToTheBackend() {
        createViewModel(deviceTimezone = "Europe/Berlin")

        assertEquals(listOf("Europe/Berlin"), profileRepository.updatedTimezones)
    }

    @Test
    fun load_skipsTheTimezoneSync_whenItMatches() {
        createViewModel(deviceTimezone = "Europe/Istanbul")

        assertTrue(profileRepository.updatedTimezones.isEmpty())
    }

    @Test
    fun load_sendsAliasTimezonesSuchAsGmtToo() {
        createViewModel(deviceTimezone = "GMT")

        assertEquals(listOf("GMT"), profileRepository.updatedTimezones)
    }

    // Editing the name

    @Test
    fun editName_savesOnTheBackend_andUpdatesTheSessionName() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.EditProfileClicked)
        assertEquals("Backend Name", viewModel.uiState.value.editName?.name)
        viewModel.onEvent(ProfileEvent.EditNameChanged("  Yeni İsim  "))
        viewModel.onEvent(ProfileEvent.EditNameSaved)

        assertEquals(ProfileAction.ShowMessage(ProfileStrings.EDIT_NAME_SUCCESS), viewModel.actions.first())
        assertNull(viewModel.uiState.value.editName)
        assertEquals("Yeni İsim", viewModel.uiState.value.userName)
        assertEquals("Yeni İsim", authRepository.sessionName)
    }

    @Test
    fun editName_rejectsATooShortName_withoutCallingTheBackend() {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.EditProfileClicked)
        viewModel.onEvent(ProfileEvent.EditNameChanged("A"))
        viewModel.onEvent(ProfileEvent.EditNameSaved)

        assertEquals(ProfileStrings.EDIT_NAME_TOO_SHORT_ERROR, viewModel.uiState.value.editName?.errorMessage)
        assertEquals("Backend Name", profileRepository.profile.displayName)
    }

    @Test
    fun editName_failure_keepsTheDialogOpenWithAnError() {
        profileRepository.updateNameFailure = IOException("offline")
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.EditProfileClicked)
        viewModel.onEvent(ProfileEvent.EditNameChanged("Yeni İsim"))
        viewModel.onEvent(ProfileEvent.EditNameSaved)

        val dialog = viewModel.uiState.value.editName
        assertEquals(ProfileStrings.EDIT_NAME_ERROR, dialog?.errorMessage)
        assertEquals(false, dialog?.isSaving)
        assertEquals("Session Name", authRepository.sessionName)
    }

    // Deleting the account

    @Test
    fun deleteAccount_success_clearsTheLocalSession_leavesTheNotice_andNavigatesToLogin() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.DeleteAccountClicked)
        viewModel.onEvent(ProfileEvent.DeletePasswordChanged("correct-password"))
        viewModel.onEvent(ProfileEvent.DeleteAccountConfirmed)

        assertEquals(ProfileAction.NavigateToLogin, viewModel.actions.first())
        assertEquals(listOf("correct-password"), profileRepository.deletePasswords)
        assertEquals(1, authRepository.clearLocalSessionCalls)
        assertEquals(0, authRepository.clearSessionCalls)
        assertEquals(SignInNotice.ACCOUNT_DELETED, sessionExpiry.consumeSignInNotice())
        assertNull(viewModel.uiState.value.deleteAccount)
    }

    @Test
    fun deleteAccount_wrongPassword_showsTheErrorInTheDialog_andKeepsTheSession() {
        profileRepository.deleteFailure = IncorrectPasswordException()
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.DeleteAccountClicked)
        viewModel.onEvent(ProfileEvent.DeletePasswordChanged("wrong"))
        viewModel.onEvent(ProfileEvent.DeleteAccountConfirmed)

        val dialog = viewModel.uiState.value.deleteAccount
        assertEquals(ProfileStrings.DELETE_ACCOUNT_WRONG_PASSWORD, dialog?.errorMessage)
        assertEquals("", dialog?.password)
        assertEquals(false, dialog?.isDeleting)
        assertEquals(0, authRepository.clearLocalSessionCalls)
        assertNull(sessionExpiry.consumeSignInNotice())
    }

    @Test
    fun deleteAccount_rateLimited_showsTheTooManyAttemptsMessage() {
        profileRepository.deleteFailure = TooManyAttemptsException()
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.DeleteAccountClicked)
        viewModel.onEvent(ProfileEvent.DeletePasswordChanged("any"))
        viewModel.onEvent(ProfileEvent.DeleteAccountConfirmed)

        assertEquals(ProfileStrings.DELETE_ACCOUNT_TOO_MANY_ATTEMPTS, viewModel.uiState.value.deleteAccount?.errorMessage)
    }

    @Test
    fun deleteAccount_networkFailure_showsTheGenericError() {
        profileRepository.deleteFailure = IOException("offline")
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.DeleteAccountClicked)
        viewModel.onEvent(ProfileEvent.DeletePasswordChanged("any"))
        viewModel.onEvent(ProfileEvent.DeleteAccountConfirmed)

        assertEquals(ProfileStrings.DELETE_ACCOUNT_ERROR, viewModel.uiState.value.deleteAccount?.errorMessage)
    }

    @Test
    fun deleteAccount_emptyPassword_isRejectedWithoutCallingTheBackend() {
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.DeleteAccountClicked)
        viewModel.onEvent(ProfileEvent.DeleteAccountConfirmed)

        assertEquals(ProfileStrings.DELETE_ACCOUNT_PASSWORD_REQUIRED, viewModel.uiState.value.deleteAccount?.errorMessage)
        assertTrue(profileRepository.deletePasswords.isEmpty())
    }

    @Test
    fun deleteAccount_whileInProgress_ignoresASecondConfirmAndDismiss() = runTest {
        val gate = CompletableDeferred<Unit>()
        profileRepository.onDelete = { gate.await() }
        val viewModel = createViewModel()

        viewModel.onEvent(ProfileEvent.DeleteAccountClicked)
        viewModel.onEvent(ProfileEvent.DeletePasswordChanged("correct-password"))
        viewModel.onEvent(ProfileEvent.DeleteAccountConfirmed)
        viewModel.onEvent(ProfileEvent.DeleteAccountConfirmed)
        viewModel.onEvent(ProfileEvent.DeleteAccountDismissed)
        assertEquals(true, viewModel.uiState.value.deleteAccount?.isDeleting)
        gate.complete(Unit)

        assertEquals(ProfileAction.NavigateToLogin, viewModel.actions.first())
        assertEquals(1, profileRepository.deletePasswords.size)
    }
}
