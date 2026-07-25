package com.lifeos.app.core.network

/**
 * Contract the networking layer depends on to attach and refresh JWT bearer
 * tokens, per docs/15-api-design.md#authentication.
 *
 * This is intentionally just a seam: the Authentication feature (not part of
 * this bootstrap) provides the real implementation and binds it via Koin in
 * its own DI module. The network layer must never know how tokens are
 * obtained or refreshed, only how to ask for one.
 */
interface AuthTokenProvider {
    suspend fun currentAccessToken(): String?
    suspend fun refreshAccessToken(): String?
}

/**
 * No-op default so the app can compile and run before the Authentication
 * feature exists. Koin should override this binding once real auth lands.
 */
class NoOpAuthTokenProvider : AuthTokenProvider {
    override suspend fun currentAccessToken(): String? = null
    override suspend fun refreshAccessToken(): String? = null
}
