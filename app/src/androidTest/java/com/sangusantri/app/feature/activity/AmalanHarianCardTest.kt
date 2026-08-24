package com.sangusantri.app.feature.activity

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sangusantri.app.R
import com.sangusantri.app.core.designsystem.theme.SanguSantriTheme
import com.sangusantri.app.domain.model.AmalanDay
import com.sangusantri.app.domain.model.AmalanDayState
import com.sangusantri.app.domain.model.AmalanHarian
import com.sangusantri.app.feature.activity.components.AmalanHarianCard
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Every state the design page defines for the Amalan Harian card
 * (`docs/design/STREAK_GAMIFICATION_CONCEPT.md`), asserted on the copy a user actually reads —
 * including the two that are hard to reach by hand on a device: a completed 2-of-2 day, and a
 * broken streak with its record intact.
 *
 * The Hilt rule is not for injection — this card takes no dependencies. It exists because the app's
 * `@AndroidEntryPoint` boot receiver fires on every test-run reinstall, and without a created
 * component it takes the whole instrumentation process down before a single test starts.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AmalanHarianCardTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun firstRunInvitesRatherThanScores() {
        setCard(state(dzikirDone = false, pages = 0, streak = 0, record = 0, everCompleted = false))

        composeRule.onNodeWithText(context.getString(R.string.amalan_first_run)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.amalan_row_dzikir)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.amalan_row_quran_target, QURAN_TARGET))
            .assertIsDisplayed()
    }

    /** Design state 1: "1 dari 2 — tinggal 1 halaman lagi". */
    @Test
    fun onePageShortSaysExactlyWhatIsLeft() {
        setCard(state(dzikirDone = true, pages = 2, streak = 7, record = 21))

        composeRule
            .onNodeWithText(context.getString(R.string.amalan_progress_left_quran, 1, 1))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.amalan_value_done)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.amalan_value_pages, 2, QURAN_TARGET))
            .assertIsDisplayed()
    }

    /** Design state 2: both targets landed. */
    @Test
    fun completingBothTargetsSaysAlhamdulillah() {
        setCard(state(dzikirDone = true, pages = 3, streak = 8, record = 21))

        composeRule.onNodeWithText(context.getString(R.string.amalan_complete)).assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.amalan_value_pages, 3, QURAN_TARGET))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.amalan_streak_record, 21))
            .assertIsDisplayed()
    }

    /** Design state 3: the Qur'an target is suspended, never shown as failed. */
    @Test
    fun udzurSuspendsTheQuranRowAndSaysWhy() {
        setCard(state(dzikirDone = true, pages = 0, streak = 12, record = 21, udzur = true))

        composeRule.onNodeWithText(context.getString(R.string.amalan_udzur_banner)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.amalan_value_paused)).assertIsDisplayed()
    }

    /** Design state 4: no shame — the record survives and today is offered again. */
    @Test
    fun aBrokenStreakKeepsTheRecordAndOffersToday() {
        setCard(state(dzikirDone = false, pages = 0, streak = 0, record = 21))

        composeRule
            .onNodeWithText(context.getString(R.string.amalan_streak_restart, 21))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.amalan_streak_record, 21))
            .assertIsDisplayed()
    }

    @Test
    fun eachGoalRowOpensTheFeatureThatSatisfiesIt() {
        var dzikirClicks = 0
        var quranClicks = 0
        setCard(
            state(dzikirDone = false, pages = 0, streak = 0, record = 0),
            onDzikirClick = { dzikirClicks++ },
            onQuranClick = { quranClicks++ },
        )

        composeRule
            .onNodeWithContentDescription(
                context.getString(
                    R.string.amalan_row_dzikir_content_description,
                    context.getString(R.string.amalan_value_pending),
                ),
            ).performClick()
        composeRule
            .onNodeWithContentDescription(
                context.getString(
                    R.string.amalan_row_quran_content_description,
                    context.getString(R.string.amalan_value_pages, 0, QURAN_TARGET),
                ),
            ).performClick()

        assertEquals(1, dzikirClicks)
        assertEquals(1, quranClicks)
    }

    @Test
    fun theUdzurToggleReportsItsChange() {
        var requested: Boolean? = null
        setCard(state(dzikirDone = true, pages = 0, streak = 0, record = 0), onUdzurChange = { requested = it })

        composeRule.onNodeWithText(context.getString(R.string.amalan_udzur_toggle)).assertIsDisplayed()
        composeRule.onNode(isToggleable()).performClick()

        assertTrue(requested == true)
    }

    private fun setCard(
        state: AmalanHarian,
        onDzikirClick: () -> Unit = {},
        onQuranClick: () -> Unit = {},
        onUdzurChange: (Boolean) -> Unit = {},
    ) {
        composeRule.setContent {
            SanguSantriTheme {
                AmalanHarianCard(
                    state = state,
                    onDzikirClick = onDzikirClick,
                    onQuranClick = onQuranClick,
                    onUdzurChange = onUdzurChange,
                )
            }
        }
    }

    @Suppress("LongParameterList")
    private fun state(
        dzikirDone: Boolean,
        pages: Int,
        streak: Int,
        record: Int,
        udzur: Boolean = false,
        everCompleted: Boolean = record > 0,
    ): AmalanHarian {
        val today = LocalDate.now()
        return AmalanHarian(
            dzikirDone = dzikirDone,
            quranPagesToday = pages,
            isUdzurToday = udzur,
            currentStreakDays = streak,
            longestStreakDays = record,
            week =
                (WEEK_LENGTH - 1 downTo 0).map { back ->
                    AmalanDay(
                        date = today.minusDays(back.toLong()),
                        state = if (back == 0) AmalanDayState.PENDING else AmalanDayState.COMPLETE,
                    )
                },
            hasEverCompleted = everCompleted,
        )
    }

    private companion object {
        const val QURAN_TARGET = AmalanHarian.QURAN_PAGE_TARGET
        const val WEEK_LENGTH = 7
    }
}
