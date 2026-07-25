package com.sangusantri.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sangusantri.app.data.local.dao.AppMetadataDao
import com.sangusantri.app.data.local.database.SanguSantriDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Verifies the Hilt dependency graph wired in Milestone 0 actually resolves:
 * [SanguSantriDatabase], its DAO, and the preferences [DataStore] all inject
 * successfully from [SingletonComponent][dagger.hilt.components.SingletonComponent].
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltModulesInstrumentedTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: SanguSantriDatabase

    @Inject
    lateinit var appMetadataDao: AppMetadataDao

    @Inject
    lateinit var preferencesDataStore: DataStore<Preferences>

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun hiltProvidesTheDatabaseDaoAndDataStore() {
        assertNotNull(database)
        assertNotNull(appMetadataDao)
        assertNotNull(preferencesDataStore)
    }
}
