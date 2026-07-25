package com.lifeos.app.core.di

import org.koin.core.module.Module

/**
 * Platform-only Koin bindings: the Ktor HTTP client engine and the DataStore
 * factory, both of which require platform-specific construction (Android
 * needs a Context; iOS resolves its own sandbox paths). Everything else is
 * shared and lives in [networkModule] / [storageModule].
 *
 * Actual implementations live in androidMain and iosMain.
 */
expect fun platformModule(): Module
