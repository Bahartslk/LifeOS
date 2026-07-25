package com.lifeos.app.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal const val DATASTORE_FILE_NAME = "lifeos.preferences_pb"

/**
 * Platform-specific construction of the DataStore instance. Android resolves
 * a file path via its application Context; iOS resolves its own sandboxed
 * documents directory. Both actual implementations are bound as a Koin
 * single via each platform's module in core/di/PlatformModule.kt, so common
 * code never needs to know which platform it is running on.
 */
expect class DataStoreFactory {
    fun create(): DataStore<Preferences>
}
