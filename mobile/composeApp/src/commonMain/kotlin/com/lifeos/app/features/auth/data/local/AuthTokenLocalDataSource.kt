package com.lifeos.app.features.auth.data.local

import com.lifeos.app.core.storage.PreferencesStorage
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.first

/**
 * Persists the session locally so Splash can restore it without a network
 * call. Built on the shared [PreferencesStorage] (core infra), per
 * docs/12-project-architecture.md#repository-pattern-backend's mobile
 * equivalent — this class is the only place that knows the specific
 * preference keys for auth tokens.
 */
class AuthTokenLocalDataSource(private val preferencesStorage: PreferencesStorage) {

    suspend fun saveSession(session: AuthSession) {
        preferencesStorage.putString(KEY_ACCESS_TOKEN, session.accessToken)
        preferencesStorage.putString(KEY_REFRESH_TOKEN, session.refreshToken)
        preferencesStorage.putString(KEY_USER_EMAIL, session.user.email)
        preferencesStorage.putString(KEY_USER_FULL_NAME, session.user.fullName)
    }

    suspend fun getSession(): AuthSession? {
        val accessToken = preferencesStorage.observeString(KEY_ACCESS_TOKEN).first()
        val refreshToken = preferencesStorage.observeString(KEY_REFRESH_TOKEN).first()
        val email = preferencesStorage.observeString(KEY_USER_EMAIL).first()
        val fullName = preferencesStorage.observeString(KEY_USER_FULL_NAME).first()

        if (accessToken == null || refreshToken == null || email == null) {
            return null
        }
        return AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = AuthUser(email = email, fullName = fullName.orEmpty()),
        )
    }

    suspend fun clearSession() {
        preferencesStorage.remove(KEY_ACCESS_TOKEN)
        preferencesStorage.remove(KEY_REFRESH_TOKEN)
        preferencesStorage.remove(KEY_USER_EMAIL)
        preferencesStorage.remove(KEY_USER_FULL_NAME)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "auth_access_token"
        const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        const val KEY_USER_EMAIL = "auth_user_email"
        const val KEY_USER_FULL_NAME = "auth_user_full_name"
    }
}
