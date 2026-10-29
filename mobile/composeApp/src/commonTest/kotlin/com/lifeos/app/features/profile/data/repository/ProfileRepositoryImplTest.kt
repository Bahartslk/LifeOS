package com.lifeos.app.features.profile.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.core.storage.PreferencesStorage
import com.lifeos.app.core.storage.ThemeMode
import com.lifeos.app.core.storage.ThemePreferenceStorage
import com.lifeos.app.features.auth.domain.model.SessionTokens
import com.lifeos.app.features.profile.data.remote.UsersRemoteDataSource
import com.lifeos.app.features.profile.domain.model.IncorrectPasswordException
import com.lifeos.app.features.profile.domain.model.TooManyAttemptsException
import com.lifeos.app.features.profile.domain.model.UserProfile
import com.lifeos.app.testing.InMemoryPreferencesDataStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ProfileRepositoryImplTest {

    private val recordedRequests = mutableListOf<HttpRequestData>()
    private var respondWith: MockRequestHandleScope.() -> HttpResponseData = { respondJson(PROFILE_RESPONSE) }

    private val themeStorage = ThemePreferenceStorage(PreferencesStorage(InMemoryPreferencesDataStore()))

    private val repository = ProfileRepositoryImpl(
        themePreferenceStorage = themeStorage,
        remoteDataSource = UsersRemoteDataSource(
            HttpClientFactory.create(
                engine = MockEngine { request ->
                    recordedRequests += request
                    respondWith()
                },
                tokenProvider = object : AuthTokenProvider {
                    override suspend fun currentAccessToken(): String = "token"
                    override suspend fun refreshAccessToken(): String? = null
                },
            ),
        ),
    )

    @Test
    fun getProfile_readsUsersMe() = runTest {
        val profile = repository.getProfile()

        assertEquals(UserProfile(email = "bahar@example.com", displayName = "Bahar", timezone = "Europe/Istanbul"), profile)
        val request = recordedRequests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertTrue(request.url.encodedPath.endsWith("api/v1/users/me"))
    }

    @Test
    fun updateDisplayName_patchesOnlyTheName() = runTest {
        repository.updateDisplayName("Bahar T.")

        val request = recordedRequests.single()
        assertEquals(HttpMethod.Patch, request.method)
        assertTrue(request.url.encodedPath.endsWith("api/v1/users/me"))
        assertEquals("""{"displayName":"Bahar T."}""", request.bodyText())
    }

    @Test
    fun updateTimezone_patchesOnlyTheTimezone() = runTest {
        repository.updateTimezone("Europe/Berlin")

        val request = recordedRequests.single()
        assertTrue(request.url.encodedPath.endsWith("api/v1/users/preferences"))
        assertEquals("""{"timezone":"Europe/Berlin"}""", request.bodyText())
    }

    @Test
    fun setThemeMode_savesOnTheDevice_andCopiesItToTheBackend() = runTest {
        repository.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, themeStorage.observeThemeMode().first())
        val request = recordedRequests.single()
        assertTrue(request.url.encodedPath.endsWith("api/v1/users/preferences"))
        assertEquals("""{"themePreference":"DARK"}""", request.bodyText())
    }

    @Test
    fun setThemeMode_keepsTheDeviceSetting_whenTheBackendIsUnreachable() = runTest {
        respondWith = { respondJson(SERVER_ERROR, HttpStatusCode.InternalServerError) }

        repository.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, themeStorage.observeThemeMode().first())
    }

    @Test
    fun deleteAccount_sendsTheConfirmationPasswordInADeleteBody() = runTest {
        respondWith = { respondJson("""{"data":{"success":true}}""") }

        repository.deleteAccount("correct-password")

        val request = recordedRequests.single()
        assertEquals(HttpMethod.Delete, request.method)
        assertTrue(request.url.encodedPath.endsWith("api/v1/users/me"))
        assertEquals("""{"password":"correct-password"}""", request.bodyText())
    }

    @Test
    fun deleteAccount_wrongPassword403_becomesIncorrectPasswordException() = runTest {
        respondWith = {
            respondJson("""{"statusCode":403,"error":"FORBIDDEN","message":"Password is incorrect."}""", HttpStatusCode.Forbidden)
        }

        assertFailsWith<IncorrectPasswordException> { repository.deleteAccount("wrong") }
    }

    @Test
    fun deleteAccount_429_becomesTooManyAttemptsException() = runTest {
        respondWith = {
            respondJson(
                """{"statusCode":429,"error":"TOO_MANY_REQUESTS","message":"ThrottlerException: Too Many Requests"}""",
                HttpStatusCode.TooManyRequests,
            )
        }

        assertFailsWith<TooManyAttemptsException> { repository.deleteAccount("any") }
    }

    @Test
    fun deleteAccount_otherFailures_stayApiExceptions() = runTest {
        respondWith = { respondJson(SERVER_ERROR, HttpStatusCode.InternalServerError) }

        assertFailsWith<ApiException> { repository.deleteAccount("any") }
    }

    @Test
    fun changePassword_patchesBothPasswords_andReturnsTheNewTokens() = runTest {
        respondWith = { respondJson("""{"data":{"accessToken":"new-access","refreshToken":"new-refresh"}}""") }

        val tokens = repository.changePassword("old-password", "new-password")

        assertEquals(SessionTokens(accessToken = "new-access", refreshToken = "new-refresh"), tokens)
        val request = recordedRequests.single()
        assertEquals(HttpMethod.Patch, request.method)
        assertTrue(request.url.encodedPath.endsWith("api/v1/users/me/password"))
        assertEquals("""{"currentPassword":"old-password","newPassword":"new-password"}""", request.bodyText())
    }

    @Test
    fun changePassword_wrongCurrentPassword403_becomesIncorrectPasswordException() = runTest {
        respondWith = {
            respondJson(
                """{"statusCode":403,"error":"FORBIDDEN","message":"Current password is incorrect."}""",
                HttpStatusCode.Forbidden,
            )
        }

        assertFailsWith<IncorrectPasswordException> { repository.changePassword("wrong", "new-password") }
    }

    @Test
    fun changePassword_429_becomesTooManyAttemptsException() = runTest {
        respondWith = {
            respondJson("""{"statusCode":429,"error":"TOO_MANY_REQUESTS","message":"Too Many Requests"}""", HttpStatusCode.TooManyRequests)
        }

        assertFailsWith<TooManyAttemptsException> { repository.changePassword("any", "new-password") }
    }

    private fun HttpRequestData.bodyText(): String =
        (body as OutgoingContent.ByteArrayContent).bytes().decodeToString()

    private fun MockRequestHandleScope.respondJson(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData = respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private companion object {
        const val PROFILE_RESPONSE =
            """{"data":{"id":"7b0c","email":"bahar@example.com","displayName":"Bahar","avatarUrl":null,""" +
                """"role":"USER","createdAt":"2026-10-01T10:00:00.000Z","bio":null,"timezone":"Europe/Istanbul",""" +
                """"language":"tr","themePreference":"SYSTEM","notificationPreferences":{},"updatedAt":"2026-10-01T10:00:00.000Z"}}"""
        const val SERVER_ERROR = """{"statusCode":500,"error":"INTERNAL_SERVER_ERROR","message":"Internal server error"}"""
    }
}
