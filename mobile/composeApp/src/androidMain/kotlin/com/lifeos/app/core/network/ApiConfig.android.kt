package com.lifeos.app.core.network

import com.lifeos.app.BuildConfig

/** Sourced from `composeApp/build.gradle.kts`'s per-build-type `API_BASE_URL` `buildConfigField`. */
internal actual fun provideApiBaseUrl(): String = BuildConfig.API_BASE_URL
