package com.lifeos.app.features.profile.presentation

import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.features.auth.data.SessionExpiryRepositoryImpl
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.AuthUser
import com.lifeos.app.features.auth.domain.repository.AuthRepository
import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.auth.domain.usecase.LogoutUseCase
import com.lifeos.app.features.profile.domain.model.UserProfile
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import com.lifeos.app.features.profile.domain.usecase.DeleteAccountUseCase
import com.lifeos.app.features.profile.domain.usecase.GetUserProfileUseCase
import com.lifeos.app.features.profile.domain.usecase.ObserveThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SetThemeModeUseCase
import com.lifeos.app.features.profile.domain.usecase.SyncDeviceTimezoneUseCase
import com.lifeos.app.features.profile.domain.usecase.UpdateDisplayNameUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal fun createProfileViewModel(
    authRepository: FakeAuthRepository = FakeAuthRepository(),
    profileRepository: FakeProfileRepository = FakeProfileRepository(),
    sessionExpiry: SessionExpiryRepositoryImpl = SessionExpiryRepositoryImpl(),
    deviceTimezone: String = "Europe/Istanbul",
) = ProfileViewModel(
    getSession = GetSessionUseCase(authRepository),
    getUserProfile = GetUserProfileUseCase(profileRepository),
    syncDeviceTimezone = SyncDeviceTimezoneUseCase(profileRepository, deviceTimezone = { deviceTimezone }),
    observeThemeMode = ObserveThemeModeUseCase(profileRepository),
    setThemeMode = SetThemeModeUseCase(profileRepository),
    updateDisplayName = UpdateDisplayNameUseCase(profileRepository, authRepository),
    deleteAccount = DeleteAccountUseCase(profileRepository, authRepository, sessionExpiry),
    logout = LogoutUseCase(authRepository),
)

internal class FakeAuthRepository : AuthRepository {
    var sessionName = "Session Name"
    var clearSessionCalls = 0
    var clearLocalSessionCalls = 0
    var onClearSession: suspend () -> Unit = {}

    override suspend fun login(email: String, password: String): Result<AuthSession> = error("unused")
    override suspend fun register(fullName: String, email: String, password: String): Result<AuthSession> =
        error("unused")
    override suspend fun requestPasswordReset(email: String): Result<Unit> = error("unused")
    override suspend fun getSession(): AuthSession =
        AuthSession(accessToken = "a", refreshToken = "r", user = AuthUser(email = "u@example.com", fullName = sessionName))

    override suspend fun clearSession() {
        clearSessionCalls++
        onClearSession()
    }

    override suspend fun clearLocalSession() {
        clearLocalSessionCalls++
    }

    override suspend fun updateSessionDisplayName(fullName: String) {
        sessionName = fullName
    }
}

internal class FakeProfileRepository : ProfileRepository {
    var profile = UserProfile(email = "u@example.com", displayName = "Backend Name", timezone = "Europe/Istanbul")
    var getProfileFailure: Exception? = null
    var updateNameFailure: Exception? = null
    var deleteFailure: Exception? = null
    var onDelete: suspend () -> Unit = {}
    val deletePasswords = mutableListOf<String>()
    val updatedTimezones = mutableListOf<String>()
    private val mode = MutableStateFlow(ThemeMode.SYSTEM)

    override fun observeThemeMode(): Flow<ThemeMode> = mode

    override suspend fun setThemeMode(mode: ThemeMode) {
        this.mode.value = mode
    }

    override suspend fun getProfile(): UserProfile {
        getProfileFailure?.let { throw it }
        return profile
    }

    override suspend fun updateDisplayName(displayName: String): UserProfile {
        updateNameFailure?.let { throw it }
        profile = profile.copy(displayName = displayName)
        return profile
    }

    override suspend fun updateTimezone(timezone: String) {
        updatedTimezones += timezone
    }

    override suspend fun deleteAccount(password: String) {
        deletePasswords += password
        onDelete()
        deleteFailure?.let { throw it }
    }
}
