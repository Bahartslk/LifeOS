package com.lifeos.app.features.auth.data

import com.lifeos.app.features.auth.domain.model.SignInNotice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionExpiryRepositoryImplTest {

    private val repository = SessionExpiryRepositoryImpl()

    @Test
    fun notice_isConsumedOnce() {
        assertNull(repository.consumeSignInNotice())

        repository.markSessionExpired()

        assertEquals(SignInNotice.SESSION_EXPIRED, repository.consumeSignInNotice())
        assertNull(repository.consumeSignInNotice())
    }

    @Test
    fun accountDeleted_leavesItsOwnNotice_withoutAnExpiryEvent() = runTest {
        repository.markAccountDeleted()

        // The deletion flow navigates itself, so the session guard must not fire too.
        assertNull(withTimeoutOrNull(100) { repository.observeSessionExpired().first() })
        assertEquals(SignInNotice.ACCOUNT_DELETED, repository.consumeSignInNotice())
        assertNull(repository.consumeSignInNotice())
    }

    @Test
    fun expiry_waitsForACollectorThatStartsLater_andRepeatedExpiriesCollapseIntoOne() = runTest {
        repository.markSessionExpired()
        repository.markSessionExpired()

        repository.observeSessionExpired().first()

        assertNull(withTimeoutOrNull(100) { repository.observeSessionExpired().first() })
    }
}
