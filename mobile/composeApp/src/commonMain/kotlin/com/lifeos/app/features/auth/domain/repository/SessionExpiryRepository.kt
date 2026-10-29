package com.lifeos.app.features.auth.domain.repository

import com.lifeos.app.features.auth.domain.model.SignInNotice
import kotlinx.coroutines.flow.Flow

/**
 * Tells the app that the signed-in session has ended without the user
 * signing out: the backend rejected the refresh token, or the user deleted
 * their account. Network errors during a renewal never end the session.
 */
interface SessionExpiryRepository {

    /** Emits when the session expires while the app is running. Meant for a single collector (the root navigation). */
    fun observeSessionExpired(): Flow<Unit>

    /** Records that the account was deleted, so the sign-in screen can say so. Navigation is up to the caller. */
    fun markAccountDeleted()

    /**
     * The reason the user landed on sign-in, once; later calls return `null`
     * until the next expiry or deletion.
     */
    fun consumeSignInNotice(): SignInNotice?
}
