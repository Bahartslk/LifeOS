package com.lifeos.app.features.auth.domain.model

/** Why the user landed on the sign-in screen, when the screen should say so once. */
enum class SignInNotice {
    /** The backend rejected the refresh token; the session was cleared. */
    SESSION_EXPIRED,

    /** The user deleted their account. */
    ACCOUNT_DELETED,
}
