package com.lifeos.app.features.profile.data.remote

import com.lifeos.app.core.network.ApiConfig
import com.lifeos.app.core.network.dataOrThrow
import com.lifeos.app.features.profile.data.dto.ChangePasswordRequestDto
import com.lifeos.app.features.profile.data.dto.DeleteAccountRequestDto
import com.lifeos.app.features.profile.data.dto.DeleteAccountResponseDto
import com.lifeos.app.features.profile.data.dto.SessionTokensDto
import com.lifeos.app.features.profile.data.dto.UpdateDisplayNameRequestDto
import com.lifeos.app.features.profile.data.dto.UpdateThemePreferenceRequestDto
import com.lifeos.app.features.profile.data.dto.UpdateTimezoneRequestDto
import com.lifeos.app.features.profile.data.dto.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Calls the backend's `/api/v1/users` routes with the shared authenticated client. */
class UsersRemoteDataSource(private val httpClient: HttpClient) {

    suspend fun getMe(): UserProfileDto =
        httpClient.get(endpoint("users/me")).dataOrThrow()

    suspend fun updateDisplayName(request: UpdateDisplayNameRequestDto): UserProfileDto =
        httpClient.patch(endpoint("users/me")) { jsonBody(request) }.dataOrThrow()

    suspend fun updateThemePreference(request: UpdateThemePreferenceRequestDto): UserProfileDto =
        httpClient.patch(endpoint("users/preferences")) { jsonBody(request) }.dataOrThrow()

    suspend fun updateTimezone(request: UpdateTimezoneRequestDto): UserProfileDto =
        httpClient.patch(endpoint("users/preferences")) { jsonBody(request) }.dataOrThrow()

    /** `DELETE` with a body: the backend needs the password to confirm the deletion. */
    suspend fun deleteMe(request: DeleteAccountRequestDto): DeleteAccountResponseDto =
        httpClient.delete(endpoint("users/me")) { jsonBody(request) }.dataOrThrow()

    suspend fun changePassword(request: ChangePasswordRequestDto): SessionTokensDto =
        httpClient.patch(endpoint("users/me/password")) { jsonBody(request) }.dataOrThrow()

    private inline fun <reified T> io.ktor.client.request.HttpRequestBuilder.jsonBody(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private fun endpoint(path: String): String = "${ApiConfig.BASE_URL}/$path"
}
