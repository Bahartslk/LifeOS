package com.lifeos.app.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Generic, feature-agnostic key-value persistence built on AndroidX
 * DataStore (Preferences), per docs/12-project-architecture.md#mobile-architecture.
 *
 * This is core infrastructure, not a feature repository: it has no concept
 * of a "user", "trip", or "task" — it only knows how to persist and retrieve
 * strings by key. Features build their own repository abstractions on top of
 * this (e.g. an Authentication token repository), they never depend on
 * DataStore directly.
 */
class PreferencesStorage(private val dataStore: DataStore<Preferences>) {

    fun observeString(key: String): Flow<String?> {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data.map { preferences -> preferences[prefKey] }
    }

    suspend fun putString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences -> preferences[prefKey] = value }
    }

    suspend fun remove(key: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences -> preferences.remove(prefKey) }
    }

    fun observeBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        val prefKey = booleanPreferencesKey(key)
        return dataStore.data.map { preferences -> preferences[prefKey] ?: defaultValue }
    }

    suspend fun putBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        dataStore.edit { preferences -> preferences[prefKey] = value }
    }

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.clear() }
    }
}
