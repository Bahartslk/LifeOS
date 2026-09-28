package com.lifeos.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider

/**
 * Drops the in-memory bearer tokens held by the shared authenticated
 * [HttpClient] built in [HttpClientFactory.create].
 *
 * Ktor's `bearer { loadTokens { ... } }` runs once and caches its result for
 * the client's whole lifetime, and that client is a Koin `single`. Clearing
 * the persisted session alone would therefore leave it attaching the
 * signed-out user's still-valid access token to every request until that
 * token expired — including requests made after a different user signs in
 * within the same process. [clear] makes the next request call `loadTokens`
 * again, which reads whatever session is persisted at that moment.
 */
class BearerTokenCache(private val httpClient: HttpClient) {

    fun clear() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
