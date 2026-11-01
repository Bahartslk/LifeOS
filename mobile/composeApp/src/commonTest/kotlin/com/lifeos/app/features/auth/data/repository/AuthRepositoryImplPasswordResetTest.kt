package com.lifeos.app.features.auth.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.BearerTokenCache
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.core.storage.PreferencesStorage
import com.lifeos.app.features.auth.data.local.AuthTokenLocalDataSource
import com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource
import com.lifeos.app.features.auth.domain.model.InvalidResetCodeException
import com.lifeos.app.features.auth.domain.model.PasswordResetRateLimitedException
import com.lifeos.app.testing.InMemoryPreferencesDataStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthRepositoryImplPasswordResetTest {

    private val recordedRequests = mutableListOf<HttpRequestData>()
    private var respondWith: MockRequestHandleScope.() -> HttpResponseData = { json("""{"data":{"success":true}}""") }

    private val engine = MockEngine { request ->
        recordedRequests += request
        respondWith()
    }

    private val repository = AuthRepositoryImpl(
        remoteDataSource = AuthRemoteDataSource(HttpClientFactory.createUnauthenticated(engine)),
        tokenLocalDataSource = AuthTokenLocalDataSource(PreferencesStorage(InMemoryPreferencesDataStore())),
        bearerTokenCache = BearerTokenCache(
            HttpClientFactory.create(
                engine = engine,
                tokenProvider = object : AuthTokenProvider {
                    override suspend fun currentAccessToken(): String? = null
                    override suspend fun refreshAccessToken(): String? = null
                },
            ),
        ),
    )

    @Test
    fun requestPasswordReset_postsTheEmailToForgotPassword() = runTest {
        repository.requestPasswordReset("bahar@example.com").getOrThrow()

        val request = recordedRequests.single()
        assertTrue(request.url.encodedPath.endsWith("api/v1/auth/forgot-password"))
        assertEquals("""{"email":"bahar@example.com"}""", request.bodyText())
    }

    @Test
    fun resetPassword_postsEmailCodeAndNewPassword() = runTest {
        repository.resetPassword("bahar@example.com", "042817", "new-password").getOrThrow()

        val request = recordedRequests.single()
        assertTrue(request.url.encodedPath.endsWith("api/v1/auth/reset-password"))
        assertEquals(
            """{"email":"bahar@example.com","code":"042817","newPassword":"new-password"}""",
            request.bodyText(),
        )
    }

    @Test
    fun resetPassword_invalidCode400_becomesInvalidResetCodeException() = runTest {
        respondWith = {
            json(
                """{"statusCode":400,"error":"BAD_REQUEST","code":"INVALID_RESET_CODE","message":"The code is invalid or has expired."}""",
                HttpStatusCode.BadRequest,
            )
        }

        assertIs<InvalidResetCodeException>(repository.resetPassword("a@example.com", "000000", "new-password").exceptionOrNull())
    }

    @Test
    fun anyOther400_staysAnApiException() = runTest {
        respondWith = {
            json("""{"statusCode":400,"error":"BAD_REQUEST","message":["code must be 6 digits."]}""", HttpStatusCode.BadRequest)
        }

        assertIs<ApiException>(repository.resetPassword("a@example.com", "1", "new-password").exceptionOrNull())
    }

    @Test
    fun a429OnEitherRoute_becomesPasswordResetRateLimitedException() = runTest {
        respondWith = {
            json(
                """{"statusCode":429,"error":"TOO_MANY_REQUESTS","code":"PASSWORD_RESET_EMAIL_LIMIT","message":"Too many"}""",
                HttpStatusCode.TooManyRequests,
            )
        }

        assertIs<PasswordResetRateLimitedException>(repository.requestPasswordReset("a@example.com").exceptionOrNull())
        assertIs<PasswordResetRateLimitedException>(
            repository.resetPassword("a@example.com", "000000", "new-password").exceptionOrNull(),
        )
    }

    private fun HttpRequestData.bodyText(): String = (body as OutgoingContent.ByteArrayContent).bytes().decodeToString()

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
}
