package com.lifeos.app.core.network

/**
 * Unchanged from this project's previous single hardcoded value — iOS has no
 * Gradle `BuildConfig` equivalent and is not part of this task's "prepare
 * the APK for external testing" scope. Update this directly if an iOS build
 * ever needs to point elsewhere.
 */
internal actual fun provideApiBaseUrl(): String = "http://10.0.2.2:3000/api/v1"
