package com.lifeos.app.features.auth.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionExpiryRepositoryImplTest {

    private val repository = SessionExpiryRepositoryImpl()

    @Test
    fun notice_isConsumedOnce() {
        assertFalse(repository.consumeSessionExpiredNotice())

        repository.markSessionExpired()

        assertTrue(repository.consumeSessionExpiredNotice())
        assertFalse(repository.consumeSessionExpiredNotice())
    }

    @Test
    fun expiry_waitsForACollectorThatStartsLater_andRepeatedExpiriesCollapseIntoOne() = runTest {
        repository.markSessionExpired()
        repository.markSessionExpired()

        repository.observeSessionExpired().first()

        assertNull(withTimeoutOrNull(100) { repository.observeSessionExpired().first() })
    }
}
