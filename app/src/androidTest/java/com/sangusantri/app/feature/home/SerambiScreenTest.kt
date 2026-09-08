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
import com.sangusantri.app.data.content.ContentLocalDataSource
import com.sangusantri.app.data.content.ContentValidator
import com.sangusantri.app.data.content.dto.ContentDetailDto
import com.sangusantri.app.data.content.dto.ContentStepDto
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
 * Exercises Serambi against the real Hilt graph — the offline-first path (FR-001, FR-002): Serambi
 * renders from Room, with no network involved.
 *
 * Content is seeded through the injected [ContentLocalDataSource] with clearly-fixture-labelled items
 * rather than by letting the CMS sync run, so the assertions never depend on what the CMS happens
 * to publish today or on the emulator having a network at all.
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
    lateinit var localDataSource: ContentLocalDataSource

    @Inject
    lateinit var preferencesDataStore: DataStore<Preferences>

    @Before
    fun seedRoom() {
        hiltRule.inject()
        runBlocking {
            preferencesDataStore.edit { it.clear() }
            localDataSource.saveDetail(fixture(FIRST_FIXTURE_TITLE, order = 0))
            localDataSource.saveDetail(fixture(SECOND_FIXTURE_TITLE, order = 1))
        }
    }

    @Test
    fun amaliyahCardsRenderFromSeededRoomContent() {
        waitForSeededContent()

        composeRule.onNodeWithText(FIRST_FIXTURE_TITLE).assertExists()
        composeRule.onNodeWithText(SECOND_FIXTURE_TITLE).assertExists()
    }

    @Test
    fun tappingAnAmaliyahCardNavigatesToItsDestination() {
        waitForSeededContent()

        composeRule.onNodeWithText(FIRST_FIXTURE_TITLE).performClick()

        // Milestone 4: tapping a card opens the reading-mode gate first. The fixture has real
        // steps, so the gate's availability check passes and offers the Bacaan Lengkap/Panduan
        // chooser — an unambiguous signal that navigation left Serambi and reached the gate
        // destination (Serambi's own actions are Setelan/Tentang, never this chooser).
        composeRule.waitUntil(timeoutMillis = SEED_IMPORT_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithText(composeRule.activity.getString(R.string.reader_mode_chooser_title))
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
            composeRule.onAllNodesWithText(FIRST_FIXTURE_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun fixture(
        title: String,
        order: Int,
    ) = ContentDetailDto(
        schemaVersion = ContentValidator.SUPPORTED_REMOTE_SCHEMA_VERSION,
        id = "serambi-test-fixture-$order",
        title = title,
        description = "[FIXTURE] Serambi test fixture",
        imageUrl = null,
        category = "Amaliyah",
        order = order,
        sourceName = "[FIXTURE]",
        sourceUrl = "https://example.invalid/fixture",
        steps =
            listOf(
                ContentStepDto(
                    id = "serambi-test-fixture-$order-step-1",
                    arabicText = "[FIXTURE-AR] بِسْمِ اللَّهِ",
                    translation = "[FIXTURE] Terjemahan uji.",
                    repeatTarget = 1,
                ),
            ),
    )

    private companion object {
        const val SEED_IMPORT_TIMEOUT_MILLIS = 10_000L
        const val FIRST_FIXTURE_TITLE = "[TEST] Serambi Fixture Satu"
        const val SECOND_FIXTURE_TITLE = "[TEST] Serambi Fixture Dua"
    }
}
