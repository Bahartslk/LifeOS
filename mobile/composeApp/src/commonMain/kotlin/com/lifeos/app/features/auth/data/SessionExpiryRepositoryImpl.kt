package com.lifeos.app.features.auth.data

import com.lifeos.app.features.auth.domain.repository.SessionExpiryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.concurrent.Volatile

/**
 * In-memory session-expiry signal, a Koin `single`. [AuthTokenProviderImpl]
 * calls [markSessionExpired] after the backend rejects a refresh; the root
 * navigation (its only collector) observes it and returns to sign-in.
 */
class SessionExpiryRepositoryImpl : SessionExpiryRepository {

    // A conflated channel, not a SharedFlow: the signal waits until it is
    // collected instead of being dropped when nobody listens at that moment,
    // several near-simultaneous expiries collapse into one, and a collector
    // that subscribes again later (e.g. after a configuration change) never
    // gets an old expiry replayed.
    private val expired = Channel<Unit>(Channel.CONFLATED)

    @Volatile
    private var pendingNotice = false

    fun markSessionExpired() {
        pendingNotice = true
        expired.trySend(Unit)
    }

    override fun observeSessionExpired(): Flow<Unit> = expired.receiveAsFlow()

    override fun consumeSessionExpiredNotice(): Boolean {
        val notice = pendingNotice
        pendingNotice = false
        return notice
    }
}
