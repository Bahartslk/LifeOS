package com.lifeos.app.core.network

/**
 * Central place for API connection settings. Values are placeholders for the
 * bootstrap phase; once environment-specific builds are introduced, these
 * should be sourced from build-time configuration rather than hardcoded here.
 *
 * Matches the versioned REST contract defined in docs/15-api-design.md.
 *
 * `10.0.2.2` is the Android emulator's alias for the host machine's
 * `localhost` (the emulator is its own network namespace, so a literal
 * `localhost` would resolve to the emulator itself, not the backend running
 * on the host) — see docs/12-project-architecture.md's local-dev topology.
 * iOS simulators share the host's network namespace directly, so this value
 * only needs adjusting there if that target is ever run against a local
 * backend.
 */
object ApiConfig {
    const val BASE_URL: String = "http://10.0.2.2:3000/api/v1"
    const val REQUEST_TIMEOUT_MILLIS: Long = 30_000
    const val CONNECT_TIMEOUT_MILLIS: Long = 15_000
}
