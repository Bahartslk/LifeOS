package com.lifeos.app.features.auth.data.remote

import com.lifeos.app.core.network.ApiConfig
import com.lifeos.app.core.network.dataOrThrow
import com.lifeos.app.features.auth.data.dto.LoginRequestDto
import com.lifeos.app.features.auth.data.dto.LoginResponseDto
import com.lifeos.app.features.auth.data.dto.LogoutResponseDto
import com.lifeos.app.features.auth.data.dto.RefreshTokenRequestDto
import com.lifeos.app.features.auth.data.dto.RegisterRequestDto
import com.lifeos.app.features.auth.data.dto.TokenPairDto
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Calls the real `/api/v1/auth` endpoints, per
 * docs/15-api-design.md#authentication. Returns the raw wire DTOs;
 * [com.lifeos.app.features.auth.data.repository.AuthRepositoryImpl] maps
 * them into domain types and decides how to interpret failures — this class
 * only knows HTTP, never [com.lifeos.app.features.auth.domain.model.AuthSession].
 *
 * Deliberately injected with `core/network/HttpClientFactory`'s
 * *unauthenticated* client (`createUnauthenticated`, no [Auth][io.ktor.client.plugins.auth.Auth]
 * plugin), not the shared authenticated one every other feature will use —
 * seeing its own KDoc there for why that avoids a circular dependency.
 * `login`/`register`/`refresh` don't need a bearer token at all
 * (docs/15-api-design.md's "Auth Required: No" for all three); `logout`
 * *is* guarded (`JwtAuthGuard`) so it attaches one explicitly, the one place
 * in this class that needs to.
 */
class AuthRemoteDataSource(private val httpClient: HttpClient) {

    suspend fun login(email: String, password: String): LoginResponseDto {
        val response = httpClient.post(endpoint("auth/login")) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email = email, password = password))
        }
        return response.dataOrThrow()
    }

    suspend fun register(displayName: String, email: String, password: String): TokenPairDto {
        val response = httpClient.post(endpoint("auth/register")) {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(email = email, password = password, displayName = displayName))
        }
        return response.dataOrThrow()
    }

    suspend fun refresh(refreshToken: String): TokenPairDto {
        val response = httpClient.post(endpoint("auth/refresh")) {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
        }
        return response.dataOrThrow()
    }

    suspend fun logout(accessToken: String, refreshToken: String): LogoutResponseDto {
        val response = httpClient.post(endpoint("auth/logout")) {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
        }
        return response.dataOrThrow()
    }

    /**
     * Full absolute URL built by plain concatenation rather than relying on
     * Ktor's relative-path resolution against `defaultRequest`'s base
     * (RFC 3986 relative-reference rules would drop `/v1` from
     * [ApiConfig.BASE_URL] since it has no trailing slash) — unambiguous and
     * trivially correct instead.
     */
    private fun endpoint(path: String): String = "${ApiConfig.BASE_URL}/$path"
}
