package com.sangusantri.app.feature.home

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sangusantri.app.MainActivity
import com.sangusantri.app.R
import com.sangusantri.app.data.local.seed.SeedContentImporter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.inject.Inject

/**
 * Exercises Serambi against the real Hilt graph and bundled non-production fixtures — the same
 * offline-first path a fresh install takes (FR-001, FR-002): Serambi renders from Room, with no
 * network involved. [HiltTestRunner][com.sangusantri.app.HiltTestRunner] swaps in
 * `HiltTestApplication`, so the real `SanguSantriApplication.onCreate()` seed-import wiring never
 * runs here — the test seeds Room itself via the same injected [SeedContentImporter] instead.
 *
 * Reader preferences (including the Milestone 4 remembered reading mode) live in the real, shared
 * preferences DataStore, so `@Before` clears it — otherwise a mode remembered by a previous test
 * method would make the mode gate skip straight past its chooser.
 */
@HiltAndroidTest
@RunWith(JUnit4::class)
class SerambiScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var seedContentImporter: SeedContentImporter

    @Inject
    lateinit var preferencesDataStore: DataStore<Preferences>

    @Before
    fun seedRoom() {
        hiltRule.inject()
        runBlocking {
            preferencesDataStore.edit { it.clear() }
            seedContentImporter.importSeedContent()
        }
    }

    @Test
    fun tahlilAndIstighosahCardsRenderFromSeededRoomContent() {
        waitForSeededContent()

        composeRule.onNodeWithText("Tahlil").assertExists()
        composeRule.onNodeWithText("Istighosah").assertExists()
    }

    @Test
    fun tappingAnAmaliyahCardNavigatesToItsDestination() {
        waitForSeededContent()

        composeRule.onNodeWithText("Tahlil").performClick()

        // Milestone 4: tapping Tahlil now opens the reading-mode gate first. Tahlil's bundled
        // fixture is still DRAFT (Milestone 1), so the gate's own content-availability check
        // short-circuits straight to its content-unavailable state without ever offering Bacaan
        // Lengkap/Panduan — still an unambiguous signal that navigation left Serambi and reached
        // the gate destination (Serambi's own actions are Setelan/Tentang, never this message).
        composeRule.waitUntil(timeoutMillis = SEED_IMPORT_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithText(composeRule.activity.getString(R.string.reader_content_unavailable))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun openingSetelanFromSerambiNavigatesToItsDestination() {
        waitForSeededContent()

        composeRule
            .onNodeWithContentDescription(
                composeRule.activity.getString(R.string.serambi_setelan_content_description),
            ).performClick()

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.setelan_placeholder_message))
            .assertExists()
    }

    /** [MainActivity] launches before [seedRoom]; wait for Room's post-import Flow emission to recompose. */
    private fun waitForSeededContent() {
        composeRule.waitUntil(timeoutMillis = SEED_IMPORT_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText("Tahlil").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val SEED_IMPORT_TIMEOUT_MILLIS = 10_000L
    }
}
