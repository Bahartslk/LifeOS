package com.lifeos.app.core.di

import com.lifeos.app.core.network.AuthTokenProvider
import com.lifeos.app.core.network.HttpClientFactory
import com.lifeos.app.core.network.NoOpAuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Shared networking bindings, consumed by every feature's remote data
 * source. See docs/12-project-architecture.md#repository-pattern for how
 * this HttpClient is meant to be used (only from data-layer classes, never
 * from a ViewModel or Composable directly).
 */
val networkModule: Module = module {

    // Placeholder binding: the Authentication feature overrides this with a
    // real implementation backed by PreferencesStorage once it exists.
    single<AuthTokenProvider> { NoOpAuthTokenProvider() }

    single<HttpClient> {
        HttpClientFactory.create(
            engine = get<HttpClientEngine>(),
            tokenProvider = get(),
        )
    }
}
