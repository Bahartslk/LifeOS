package com.lifeos.app.core.network

/**
 * Central place for API connection settings. Matches the versioned REST
 * contract defined in docs/15-api-design.md.
 *
 * [BASE_URL] is sourced from build-time configuration ([provideApiBaseUrl],
 * `expect`/`actual` per platform) rather than a single hardcoded value, so a
 * release build can point at a real deployed backend (Railway/Render/etc.)
 * without editing source — see `composeApp/build.gradle.kts`'s
 * `API_BASE_URL` `buildConfigField` for how the Android `release` value is
 * supplied. The Android `debug` value and the iOS value both still default
 * to `http://10.0.2.2:3000/api/v1` (the Android emulator's alias for the
 * host machine's `localhost` — the emulator is its own network namespace, so
 * a literal `localhost` would resolve to the emulator itself, not the
 * backend running on the host; see docs/12-project-architecture.md's
 * local-dev topology), unchanged from before this became configurable.
 */
object ApiConfig {
    val BASE_URL: String = provideApiBaseUrl()
    const val REQUEST_TIMEOUT_MILLIS: Long = 30_000
    const val CONNECT_TIMEOUT_MILLIS: Long = 15_000
}

internal expect fun provideApiBaseUrl(): String
