package com.lifeos.app.core.di

import com.lifeos.app.core.storage.DataStoreFactory
import com.lifeos.app.core.storage.PreferencesStorage
import com.lifeos.app.core.storage.ThemePreferenceStorage
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Shared local-storage bindings. Feature modules depend on
 * [PreferencesStorage], never on DataStore directly, per
 * docs/12-project-architecture.md#repository-pattern.
 */
val storageModule: Module = module {
    single { get<DataStoreFactory>().create() }
    single { PreferencesStorage(dataStore = get()) }
    single { ThemePreferenceStorage(preferencesStorage = get()) }
}
