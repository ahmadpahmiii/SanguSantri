package com.sangusantri.app.feature.home

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

    @Before
    fun seedRoom() {
        hiltRule.inject()
        runBlocking { seedContentImporter.importSeedContent() }
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

        // Milestone 3: tapping Tahlil now opens the real Full Reader. The reader settings action
        // only exists on the reader's top bar (Serambi's own actions are Setelan/Tentang), so its
        // presence is an unambiguous signal that navigation reached the reader destination.
        val readerSettingsDescription =
            composeRule.activity.getString(R.string.reader_settings_content_description)
        composeRule.waitUntil(timeoutMillis = SEED_IMPORT_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithContentDescription(readerSettingsDescription)
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
