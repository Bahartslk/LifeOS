package com.lifeos.app.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import okio.Path.Companion.toOkioPath

actual class DataStoreFactory(private val context: Context) {
    actual fun create(): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { File(context.filesDir, DATASTORE_FILE_NAME).toOkioPath() },
        )
}
