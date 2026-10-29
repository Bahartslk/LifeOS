package com.lifeos.app.features.profile.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.core.storage.ThemePreferenceStorage
import com.lifeos.app.features.profile.data.dto.DeleteAccountRequestDto
import com.lifeos.app.features.profile.data.dto.UpdateDisplayNameRequestDto
import com.lifeos.app.features.profile.data.dto.UpdateThemePreferenceRequestDto
import com.lifeos.app.features.profile.data.dto.UpdateTimezoneRequestDto
import com.lifeos.app.features.profile.data.dto.UserProfileDto
import com.lifeos.app.features.profile.data.remote.UsersRemoteDataSource
import com.lifeos.app.features.profile.domain.model.IncorrectPasswordException
import com.lifeos.app.features.profile.domain.model.TooManyAttemptsException
import com.lifeos.app.features.profile.domain.model.UserProfile
import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * [ProfileRepository] backed by the backend's `/users` routes via
 * [UsersRemoteDataSource]. The theme stays device-first through
 * [ThemePreferenceStorage], so it still applies offline; the backend copy
 * is only kept in step.
 */
class ProfileRepositoryImpl(
    private val themePreferenceStorage: ThemePreferenceStorage,
    private val remoteDataSource: UsersRemoteDataSource,
) : ProfileRepository {

    override fun observeThemeMode(): Flow<ThemeMode> = themePreferenceStorage.observeThemeMode()

    override suspend fun setThemeMode(mode: ThemeMode) {
        themePreferenceStorage.setThemeMode(mode)
        // Best effort: the device setting is what the app uses, so a failed
        // sync must not undo or block the change the user just made.
        try {
            remoteDataSource.updateThemePreference(UpdateThemePreferenceRequestDto(themePreference = mode.name))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    override suspend fun getProfile(): UserProfile = remoteDataSource.getMe().toDomain()

    override suspend fun updateDisplayName(displayName: String): UserProfile =
        remoteDataSource.updateDisplayName(UpdateDisplayNameRequestDto(displayName = displayName)).toDomain()

    override suspend fun updateTimezone(timezone: String) {
        remoteDataSource.updateTimezone(UpdateTimezoneRequestDto(timezone = timezone))
    }

    override suspend fun deleteAccount(password: String) {
        try {
            remoteDataSource.deleteMe(DeleteAccountRequestDto(password = password))
        } catch (e: ApiException) {
            throw when (e.statusCode) {
                HttpStatusCode.Forbidden.value -> IncorrectPasswordException()
                HttpStatusCode.TooManyRequests.value -> TooManyAttemptsException()
                else -> e
            }
        }
    }

    private fun UserProfileDto.toDomain() = UserProfile(
        email = email,
        displayName = displayName,
        timezone = timezone,
    )
}
