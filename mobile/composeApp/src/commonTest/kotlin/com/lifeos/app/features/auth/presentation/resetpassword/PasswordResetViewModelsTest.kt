package com.lifeos.app.features.auth.presentation.resetpassword

import com.lifeos.app.features.auth.data.SessionExpiryRepositoryImpl
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.InvalidResetCodeException
import com.lifeos.app.features.auth.domain.model.PasswordResetRateLimitedException
import com.lifeos.app.features.auth.domain.model.SessionTokens
import com.lifeos.app.features.auth.domain.model.SignInNotice
import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.auth.domain.usecase.RequestPasswordResetUseCase
import com.lifeos.app.features.auth.domain.usecase.ResetPasswordUseCase
import com.lifeos.app.features.auth.presentation.AuthStrings
import com.lifeos.app.features.auth.presentation.forgotpassword.ForgotPasswordAction
import com.lifeos.app.features.auth.presentation.forgotpassword.ForgotPasswordEvent
import com.lifeos.app.features.auth.presentation.forgotpassword.ForgotPasswordViewModel
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
class PasswordResetViewModelsTest {

    private val authRepository = FakeResetAuthRepository()
    private val sessionExpiry = SessionExpiryRepositoryImpl()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun forgotViewModel() = ForgotPasswordViewModel(RequestPasswordResetUseCase(authRepository))

    private fun resetViewModel() = ResetPasswordViewModel(
        email = "bahar@example.com",
        resetPassword = ResetPasswordUseCase(authRepository, sessionExpiry),
        requestPasswordReset = RequestPasswordResetUseCase(authRepository),
    )

    // Forgot Password

    @Test
    fun forgot_requestsACode_andMovesOnWithTheTrimmedEmail() = runTest {
        val viewModel = forgotViewModel()

        viewModel.onEvent(ForgotPasswordEvent.EmailChanged(" bahar@example.com "))
        viewModel.onEvent(ForgotPasswordEvent.SubmitClicked)

        assertEquals(ForgotPasswordAction.NavigateToResetPassword("bahar@example.com"), viewModel.actions.first())
        assertEquals(listOf("bahar@example.com"), authRepository.requestedEmails)
    }

    @Test
    fun forgot_rateLimited_showsTheMessage_andStays() {
        authRepository.requestFailure = PasswordResetRateLimitedException()
        val viewModel = forgotViewModel()

        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("bahar@example.com"))
        viewModel.onEvent(ForgotPasswordEvent.SubmitClicked)

        assertEquals(AuthStrings.PASSWORD_RESET_TOO_MANY_ATTEMPTS, viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun forgot_invalidEmail_isRejectedWithoutARequest() {
        val viewModel = forgotViewModel()

        viewModel.onEvent(ForgotPasswordEvent.EmailChanged("not-an-email"))
        viewModel.onEvent(ForgotPasswordEvent.SubmitClicked)

        assertTrue(viewModel.uiState.value.emailError != null)
        assertTrue(authRepository.requestedEmails.isEmpty())
    }

    // Reset Password

    private fun ResetPasswordViewModel.fill(code: String = "042817", new: String = "new-password", confirm: String = new) {
        onEvent(ResetPasswordEvent.CodeChanged(code))
        onEvent(ResetPasswordEvent.NewPasswordChanged(new))
        onEvent(ResetPasswordEvent.ConfirmPasswordChanged(confirm))
    }

    @Test
    fun reset_success_leavesThePasswordResetNotice_andGoesToSignIn() = runTest {
        val viewModel = resetViewModel()

        viewModel.fill()
        viewModel.onEvent(ResetPasswordEvent.SubmitClicked)

        assertEquals(ResetPasswordAction.NavigateToLogin, viewModel.actions.first())
        assertEquals(listOf(Triple("bahar@example.com", "042817", "new-password")), authRepository.resets)
        assertEquals(SignInNotice.PASSWORD_RESET, sessionExpiry.consumeSignInNotice())
    }

    @Test
    fun reset_codeFieldKeepsOnlySixDigits() {
        val viewModel = resetViewModel()

        viewModel.onEvent(ResetPasswordEvent.CodeChanged("04 28-17 99"))

        assertEquals("042817", viewModel.uiState.value.code)
    }

    @Test
    fun reset_validatesCodeLengthAndThePasswordRules_withoutARequest() {
        val viewModel = resetViewModel()

        viewModel.fill(code = "123", new = "a".repeat(73), confirm = "different")
        viewModel.onEvent(ResetPasswordEvent.SubmitClicked)

        val state = viewModel.uiState.value
        assertEquals(AuthStrings.RESET_PASSWORD_CODE_INVALID_FORMAT, state.codeError)
        assertEquals("Şifre en fazla 72 karakter olabilir.", state.newPasswordError)
        assertEquals("Şifreler eşleşmiyor.", state.confirmPasswordError)
        assertTrue(authRepository.resets.isEmpty())
    }

    @Test
    fun reset_wrongCode_isShownUnderTheCodeField_andNoNoticeIsLeft() {
        authRepository.resetFailure = InvalidResetCodeException()
        val viewModel = resetViewModel()

        viewModel.fill()
        viewModel.onEvent(ResetPasswordEvent.SubmitClicked)

        assertEquals(AuthStrings.RESET_PASSWORD_CODE_WRONG, viewModel.uiState.value.codeError)
        assertEquals("", viewModel.uiState.value.code)
        assertNull(sessionExpiry.consumeSignInNotice())
    }

    @Test
    fun reset_rateLimitedOrOffline_showsAFormMessage() {
        authRepository.resetFailure = PasswordResetRateLimitedException()
        val limited = resetViewModel().apply {
            fill()
            onEvent(ResetPasswordEvent.SubmitClicked)
        }
        assertEquals(AuthStrings.PASSWORD_RESET_TOO_MANY_ATTEMPTS, limited.uiState.value.errorMessage)

        authRepository.resetFailure = IOException("offline")
        val offline = resetViewModel().apply {
            fill()
            onEvent(ResetPasswordEvent.SubmitClicked)
        }
        assertEquals(AuthStrings.RESET_PASSWORD_ERROR, offline.uiState.value.errorMessage)
    }

    @Test
    fun resend_requestsANewCode_andConfirmsIt() {
        val viewModel = resetViewModel()
        viewModel.onEvent(ResetPasswordEvent.CodeChanged("111111"))

        viewModel.onEvent(ResetPasswordEvent.ResendClicked)

        assertEquals(listOf("bahar@example.com"), authRepository.requestedEmails)
        assertEquals(AuthStrings.RESET_PASSWORD_RESENT, viewModel.uiState.value.infoMessage)
        assertEquals("", viewModel.uiState.value.code)
    }
}

private class FakeResetAuthRepository : AuthRepository {
    val requestedEmails = mutableListOf<String>()
    val resets = mutableListOf<Triple<String, String, String>>()
    var requestFailure: Exception? = null
    var resetFailure: Exception? = null

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        requestedEmails += email
        return requestFailure?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> {
        resets += Triple(email, code, newPassword)
        return resetFailure?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    override suspend fun login(email: String, password: String): Result<AuthSession> = error("unused")
    override suspend fun register(fullName: String, email: String, password: String): Result<AuthSession> = error("unused")
    override suspend fun getSession(): AuthSession? = error("unused")
    override suspend fun clearSession() = error("unused")
    override suspend fun clearLocalSession() = error("unused")
    override suspend fun updateSessionDisplayName(fullName: String) = error("unused")
    override suspend fun replaceSessionTokens(tokens: SessionTokens) = error("unused")
}
