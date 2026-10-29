package com.lifeos.app.features.auth.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Tells the app that the signed-in session can no longer be renewed (the
 * backend rejected the refresh token) and has been cleared, so the user must
 * sign in again. Network errors during a renewal never end the session.
 */
interface SessionExpiryRepository {

    /** Emits when the session expires while the app is running. Meant for a single collector (the root navigation). */
    fun observeSessionExpired(): Flow<Unit>

    /**
     * `true` once after an expiry, so the sign-in screen can say why the user
     * landed there; later calls return `false` until the next expiry.
     */
    fun consumeSessionExpiredNotice(): Boolean
}
