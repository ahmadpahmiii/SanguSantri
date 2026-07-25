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
import com.sangusantri.app.data.local.dao.AmaliyahDao
import com.sangusantri.app.data.local.dao.AmaliyahStepDao
import com.sangusantri.app.data.local.dao.AmaliyahVariantDao
import com.sangusantri.app.data.local.dao.AmaliyahVersionDao
import com.sangusantri.app.data.local.dao.ApprovalDao
import com.sangusantri.app.data.local.entity.AmaliyahEntity
import com.sangusantri.app.data.local.entity.AmaliyahStepEntity
import com.sangusantri.app.data.local.entity.AmaliyahVariantEntity
import com.sangusantri.app.data.local.entity.AmaliyahVersionEntity
import com.sangusantri.app.data.local.entity.ApprovalEntity
import com.sangusantri.app.data.local.seed.SeedContentImporter
import com.sangusantri.app.domain.model.AmaliyahVersionStatus
import com.sangusantri.app.domain.model.ApprovalStatus
import com.sangusantri.app.domain.model.OwnerType
import com.sangusantri.app.domain.model.StepType
import com.sangusantri.app.domain.model.Visibility
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
 * The app's bundled seed content stays DRAFT/PENDING (Milestone 1 scope) and is never returned by
 * `ContentRepository.getDefaultVersionDetail`, so a dedicated, clearly-fixture-labelled PUBLISHED
 * test amaliyah is inserted directly via the injected DAOs — guarded by `existsById` so reruns on
 * the same emulator stay idempotent, mirroring
 * [com.sangusantri.app.data.local.seed.SeedContentImporter]'s own idempotency pattern.
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
    lateinit var amaliyahDao: AmaliyahDao

    @Inject
    lateinit var amaliyahVariantDao: AmaliyahVariantDao

    @Inject
    lateinit var approvalDao: ApprovalDao

    @Inject
    lateinit var amaliyahVersionDao: AmaliyahVersionDao

    @Inject
    lateinit var amaliyahStepDao: AmaliyahStepDao

    @Inject
    lateinit var seedContentImporter: SeedContentImporter

    @Inject
    lateinit var preferencesDataStore: DataStore<Preferences>

    @Before
    fun seedPublishedTestFixture() {
        hiltRule.inject()
        runBlocking {
            preferencesDataStore.edit { it.clear() }
            seedContentImporter.importSeedContent()
            seedFixtureIfMissing()
        }
    }

    @Test
    fun openingTheAmaliyahFromSerambiDisplaysOrderedContentSteps() {
        waitForFixtureCard()

        composeRule.onNodeWithText(FIXTURE_TITLE).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.reader_mode_full_title)).performClick()

        composeRule.onNodeWithText(FIXTURE_HEADING_TITLE).assertExists()
        composeRule.onNodeWithText(FIXTURE_ARABIC_TEXT).assertExists()
        composeRule.onNodeWithText(FIXTURE_TRANSLATION_TEXT).assertExists()
    }

    @Test
    fun hidingTranslationFromSettingsRemovesItFromTheReader() {
        waitForFixtureCard()
        composeRule.onNodeWithText(FIXTURE_TITLE).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.reader_mode_full_title)).performClick()
        composeRule.onNodeWithText(FIXTURE_TRANSLATION_TEXT).assertExists()

        composeRule
            .onNodeWithContentDescription(
                composeRule.activity.getString(R.string.reader_settings_content_description),
            ).performClick()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.reader_settings_show_translation))
            .assertExists()
        composeRule.onNode(isToggleable()).performClick()
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.reader_settings_close_action))
            .performClick()

        composeRule.onNodeWithText(FIXTURE_TRANSLATION_TEXT).assertDoesNotExist()
    }

    @Test
    fun contentUnavailableStateRendersForAnAmaliyahWithoutAPublishedVersion() {
        waitForFixtureCard()

        // The bundled seed content (Tahlil/Istighosah) is DRAFT — opening it must show the
        // content-unavailable state, not crash or render nothing.
        composeRule.onNodeWithText("Tahlil").performClick()

        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.reader_content_unavailable))
            .assertExists()
    }

    private fun waitForFixtureCard() {
        composeRule.waitUntil(timeoutMillis = SEED_IMPORT_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIXTURE_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private suspend fun seedFixtureIfMissing() {
        if (amaliyahDao.existsById(FIXTURE_AMALIYAH_ID)) return

        amaliyahDao.insert(
            AmaliyahEntity(
                id = FIXTURE_AMALIYAH_ID,
                slug = FIXTURE_SLUG,
                titleId = FIXTURE_TITLE,
                titleAr = "[FIXTURE-AR] $FIXTURE_TITLE",
                descriptionId = null,
                descriptionAr = null,
                category = "AMALIYAH",
            ),
        )
        amaliyahVariantDao.insert(
            AmaliyahVariantEntity(
                id = "$FIXTURE_AMALIYAH_ID-umum",
                amaliyahId = FIXTURE_AMALIYAH_ID,
                slug = "umum",
                nameId = "Umum",
                nameAr = "[FIXTURE-AR] Umum",
                ownerType = OwnerType.PUBLIC,
                pondokId = null,
                visibility = Visibility.PUBLIC,
                isDefault = true,
            ),
        )
        approvalDao.insert(
            ApprovalEntity(
                id = "$FIXTURE_AMALIYAH_ID-approval",
                approverName = "[FIXTURE]",
                approverRole = "[FIXTURE]",
                institutionName = null,
                approvalDate = "2026-01-01",
                approvalScope = "[FIXTURE]",
                publicDocumentStorageKey = null,
                documentReferenceNumber = null,
                status = ApprovalStatus.APPROVED,
            ),
        )
        amaliyahVersionDao.insert(
            AmaliyahVersionEntity(
                id = "$FIXTURE_AMALIYAH_ID-v1",
                variantId = "$FIXTURE_AMALIYAH_ID-umum",
                versionNumber = 1,
                schemaVersion = 1,
                status = AmaliyahVersionStatus.PUBLISHED,
                sourceName = "[FIXTURE]",
                sourceReference = "[FIXTURE]",
                approvalId = "$FIXTURE_AMALIYAH_ID-approval",
                checksumSha256 = "test-fixture-checksum",
                minimumAppVersionCode = 1,
                publishedAt = "2026-01-01T00:00:00Z",
                revokedAt = null,
            ),
        )
        amaliyahStepDao.insertAll(
            listOf(
                AmaliyahStepEntity(
                    id = "$FIXTURE_AMALIYAH_ID-v1-step-1",
                    versionId = "$FIXTURE_AMALIYAH_ID-v1",
                    position = 1,
                    stepType = StepType.HEADING,
                    titleId = FIXTURE_HEADING_TITLE,
                    titleAr = null,
                    arabicText = null,
                    translationId = null,
                    instructionId = null,
                    instructionAr = null,
                    repeatTarget = null,
                    quranSurahNumber = null,
                    quranAyahStart = null,
                    quranAyahEnd = null,
                    audioGroupId = null,
                ),
                AmaliyahStepEntity(
                    id = "$FIXTURE_AMALIYAH_ID-v1-step-2",
                    versionId = "$FIXTURE_AMALIYAH_ID-v1",
                    position = 2,
                    stepType = StepType.ARABIC_TEXT,
                    titleId = null,
                    titleAr = null,
                    arabicText = FIXTURE_ARABIC_TEXT,
                    translationId = FIXTURE_TRANSLATION_TEXT,
                    instructionId = null,
                    instructionAr = null,
                    repeatTarget = null,
                    quranSurahNumber = null,
                    quranAyahStart = null,
                    quranAyahEnd = null,
                    audioGroupId = null,
                ),
            ),
        )
    }

    private companion object {
        const val SEED_IMPORT_TIMEOUT_MILLIS = 10_000L
        const val FIXTURE_AMALIYAH_ID = "reader-test-fixture"
        const val FIXTURE_SLUG = "reader-test-fixture"
        const val FIXTURE_TITLE = "[TEST] Reader Fixture"
        const val FIXTURE_HEADING_TITLE = "[TEST] Pembukaan"
        const val FIXTURE_ARABIC_TEXT = "[FIXTURE-AR] بِسْمِ اللَّهِ"
        const val FIXTURE_TRANSLATION_TEXT = "[FIXTURE] Terjemahan uji."
    }
}
