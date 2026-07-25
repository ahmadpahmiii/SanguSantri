package com.sangusantri.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Single canonical DataStore for user preferences (PRD 11.2: preferences live
 * in DataStore, never Room). Reader and locale preference keys are added
 * alongside the settings feature that reads and writes them.
 */
private const val PREFERENCES_DATASTORE_NAME = "sangusantri_preferences"

val Context.sanguSantriPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_DATASTORE_NAME,
)
