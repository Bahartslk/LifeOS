package com.lifeos.app.features.auth.presentation.login

import com.lifeos.app.features.auth.data.SessionExpiryRepositoryImpl
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.auth.domain.usecase.ConsumeSignInNoticeUseCase
import com.lifeos.app.features.auth.domain.usecase.LoginUseCase
import com.lifeos.app.features.auth.presentation.AuthStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelSessionExpiredTest {

    private val sessionExpiry = SessionExpiryRepositoryImpl()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun afterSessionExpiry_showsTheExpiredMessageOnce() = runTest {
        sessionExpiry.markSessionExpired()

        val first = actionsOf(createViewModel())
        val second = actionsOf(createViewModel())

        assertEquals(listOf<LoginAction>(LoginAction.ShowMessage(AuthStrings.SESSION_EXPIRED_MESSAGE)), first)
        assertTrue(second.isEmpty())
    }

    @Test
    fun afterAccountDeletion_showsTheDeletedMessageOnce() = runTest {
        sessionExpiry.markAccountDeleted()

        val first = actionsOf(createViewModel())
        val second = actionsOf(createViewModel())

        assertEquals(listOf<LoginAction>(LoginAction.ShowMessage(AuthStrings.ACCOUNT_DELETED_MESSAGE)), first)
        assertTrue(second.isEmpty())
    }

    @Test
    fun normalSignIn_showsNoMessage() = runTest {
        assertTrue(actionsOf(createViewModel()).isEmpty())
    }

    private fun kotlinx.coroutines.test.TestScope.actionsOf(viewModel: LoginViewModel): List<LoginAction> {
        val actions = mutableListOf<LoginAction>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.actions.toList(actions) }
        return actions
    }

    private fun createViewModel() = LoginViewModel(
        login = LoginUseCase(UnusedAuthRepository),
        consumeSignInNotice = ConsumeSignInNoticeUseCase(sessionExpiry),
    )

    private object UnusedAuthRepository : AuthRepository {
        override suspend fun login(email: String, password: String): Result<AuthSession> = error("unused")
        override suspend fun register(fullName: String, email: String, password: String): Result<AuthSession> = error("unused")
        override suspend fun requestPasswordReset(email: String): Result<Unit> = error("unused")
        override suspend fun getSession(): AuthSession? = error("unused")
        override suspend fun clearSession() = error("unused")
        override suspend fun clearLocalSession() = error("unused")
        override suspend fun updateSessionDisplayName(fullName: String) = error("unused")
    }
}
