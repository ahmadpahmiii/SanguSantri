package com.sangusantri.app.feature.reader

import androidx.compose.ui.test.isToggleable
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
import com.sangusantri.app.data.local.content.BundledContentBootstrapper
import com.sangusantri.app.data.local.dao.ContentDao
import com.sangusantri.app.data.local.dao.ContentStepDao
import com.sangusantri.app.data.local.entity.ContentEntity
import com.sangusantri.app.data.local.entity.ContentStepEntity
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
 * Exercises the Full Reader against the real Hilt graph, reached the same way a user does: tap an
 * amaliyah card on Serambi, then choose **Bacaan Lengkap** from the Milestone 4 reading-mode gate.
 * A dedicated, clearly-fixture-labelled content item is inserted directly via the injected DAOs
 * (independent of whichever content the bundled catalog itself currently ships) — guarded by a
 * `getById` check so reruns on the same emulator stay idempotent, mirroring
 * [com.sangusantri.app.data.content.ContentImporter]'s own idempotency pattern.
 *
 * Reader preferences live in the real, shared preferences DataStore — not a fake — so every test
 * clears it in `@Before` (not just resets the one field it touches) to guarantee the mode gate
 * always shows the chooser instead of skipping to a mode remembered by a previous test method
 * (method order is not guaranteed).
 */
@HiltAndroidTest
@RunWith(JUnit4::class)
class ReaderScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var contentDao: ContentDao

    @Inject
    lateinit var contentStepDao: ContentStepDao

    @Inject
    lateinit var bundledContentBootstrapper: BundledContentBootstrapper

    @Inject
    lateinit var preferencesDataStore: DataStore<Preferences>

    @Before
    fun seedFixtures() {
        hiltRule.inject()
        runBlocking {
            preferencesDataStore.edit { it.clear() }
            bundledContentBootstrapper.bootstrap()
            seedFixtureIfMissing()
            seedEmptyFixtureIfMissing()
        }
    }

    @Test
    fun openingTheContentFromSerambiDisplaysOrderedContentSteps() {
        waitForCard(FIXTURE_TITLE)

        composeRule.onNodeWithText(FIXTURE_TITLE).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.reader_mode_full_title)).performClick()

        composeRule.onNodeWithText(FIXTURE_ARABIC_TEXT).assertExists()
        composeRule.onNodeWithText(FIXTURE_TRANSLATION_TEXT).assertExists()
    }

    @Test
    fun hidingTranslationFromSettingsRemovesItFromTheReader() {
        waitForCard(FIXTURE_TITLE)
        composeRule.onNodeWithText(FIXTURE_TITLE).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.reader_mode_full_title)).performClick()
        composeRule.onNodeWithText(FIXTURE_TRANSLATION_TEXT).assertExists()

        // Reader appearance settings are reached through the overflow menu, not a standalone
        // top-bar icon (decision F, design product-alignment pass).
        composeRule
            .onNodeWithContentDescription(
                composeRule.activity.getString(R.string.reader_overflow_content_description),
            ).performClick()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.reader_open_settings_action))
            .performClick()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.reader_settings_show_translation))
            .assertExists()
        composeRule.onNode(isToggleable()).performClick()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.reader_settings_done_action))
            .performClick()

        composeRule.onNodeWithText(FIXTURE_TRANSLATION_TEXT).assertDoesNotExist()
    }

    @Test
    fun contentUnavailableStateRendersForAContentItemWithNoSteps() {
        waitForCard(EMPTY_FIXTURE_TITLE)

        // A catalog item with zero content_steps rows — ADR 0015 has no DRAFT/PUBLISHED status any
        // more, so "unavailable" now means exactly this: a content row with no readable steps.
        composeRule.onNodeWithText(EMPTY_FIXTURE_TITLE).performClick()

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.reader_content_unavailable))
            .assertExists()
    }

    private fun waitForCard(title: String) {
        composeRule.waitUntil(timeoutMillis = SEED_IMPORT_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private suspend fun seedFixtureIfMissing() {
        if (contentDao.getById(FIXTURE_CONTENT_ID) != null) return

        contentDao.upsert(
            ContentEntity(
                id = FIXTURE_CONTENT_ID,
                title = FIXTURE_TITLE,
                description = "[FIXTURE] Reader test fixture",
                imageUrl = null,
                category = "AMALIYAH",
                version = 1,
                order = 0,
                isActive = true,
                sourceName = "[FIXTURE]",
                sourceUrl = "https://example.invalid/fixture",
            ),
        )
        contentStepDao.insertAll(
            listOf(
                ContentStepEntity(
                    id = "$FIXTURE_CONTENT_ID-step-1",
                    contentId = FIXTURE_CONTENT_ID,
                    position = 1,
                    arabicText = FIXTURE_ARABIC_TEXT,
                    translation = FIXTURE_TRANSLATION_TEXT,
                    repeatTarget = 1,
                ),
            ),
        )
    }

    private suspend fun seedEmptyFixtureIfMissing() {
        if (contentDao.getById(EMPTY_FIXTURE_CONTENT_ID) != null) return

        contentDao.upsert(
            ContentEntity(
                id = EMPTY_FIXTURE_CONTENT_ID,
                title = EMPTY_FIXTURE_TITLE,
                description = "[FIXTURE] Reader test fixture with no steps",
                imageUrl = null,
                category = "AMALIYAH",
                version = 1,
                order = 1,
                isActive = true,
                sourceName = "[FIXTURE]",
                sourceUrl = "https://example.invalid/fixture",
            ),
        )
    }

    private companion object {
        const val SEED_IMPORT_TIMEOUT_MILLIS = 10_000L
        const val FIXTURE_CONTENT_ID = "reader-test-fixture"
        const val FIXTURE_TITLE = "[TEST] Reader Fixture"
        const val FIXTURE_ARABIC_TEXT = "[FIXTURE-AR] بِسْمِ اللَّهِ"
        const val FIXTURE_TRANSLATION_TEXT = "[FIXTURE] Terjemahan uji."
        const val EMPTY_FIXTURE_CONTENT_ID = "reader-test-empty-fixture"
        const val EMPTY_FIXTURE_TITLE = "[TEST] Empty Reader Fixture"
    }
}
