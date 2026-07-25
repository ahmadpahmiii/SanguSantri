package com.sangusantri.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SanguSantriPreferencesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testKey = stringPreferencesKey("test_key")

    @After
    fun clearPreferences() =
        runTest {
            context.sanguSantriPreferencesDataStore.edit { it.clear() }
        }

    @Test
    fun writtenValueIsReadBack() =
        runTest {
            context.sanguSantriPreferencesDataStore.edit { preferences ->
                preferences[testKey] = "foundation"
            }

            val storedValue = context.sanguSantriPreferencesDataStore.data.first()[testKey]

            assertEquals("foundation", storedValue)
        }
}
