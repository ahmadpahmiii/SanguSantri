package com.sangusantri.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the single predicate that decides whether an item offers Panduan mode. Four surfaces read
 * it (the reading-mode gate, the mode-switch pill, the overflow entry, the per-step counter), so a
 * regression here silently offers an empty tasbih walk-through for Sholawat.
 */
class ContentStepGuidedModeTest {
    @Test
    fun stepWithPositiveTargetKeepsIt() {
        assertEquals(33, step(repeatTarget = 33).effectiveRepeatTarget)
    }

    @Test
    fun nullTargetMeansNoCounter() {
        assertNull(step(repeatTarget = null).effectiveRepeatTarget)
    }

    /** A stale row or hand-edited asset can still carry 0; it means the same as null to a reader. */
    @Test
    fun zeroAndNegativeTargetsAlsoMeanNoCounter() {
        assertNull(step(repeatTarget = 0).effectiveRepeatTarget)
        assertNull(step(repeatTarget = -1).effectiveRepeatTarget)
    }

    @Test
    fun contentWithNoCountedStepHasNoGuidedMode() {
        val steps = listOf(step("s1", repeatTarget = null), step("s2", repeatTarget = 0))

        assertFalse(steps.hasGuidedMode())
    }

    @Test
    fun contentWithAtLeastOneCountedStepHasGuidedMode() {
        val steps = listOf(step("s1", repeatTarget = null), step("s2", repeatTarget = 3))

        assertTrue(steps.hasGuidedMode())
    }

    @Test
    fun contentWithNoStepsHasNoGuidedMode() {
        assertFalse(emptyList<ContentStep>().hasGuidedMode())
    }

    private fun step(
        id: String = "s1",
        repeatTarget: Int?,
    ) = ContentStep(
        id = id,
        contentId = "preview-content",
        position = 1,
        arabicText = "[FIXTURE-AR]",
        translation = "[FIXTURE]",
        repeatTarget = repeatTarget,
    )
}
