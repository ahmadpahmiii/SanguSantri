package com.sangusantri.app.feature.tasbih.components

import com.sangusantri.app.domain.model.TasbihTargetPreset
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The custom-target dialog's only guard. It runs before the dialog can be dismissed
 * (`docs/design/ACCESSIBILITY.md`'s numeric-input rule), so anything it lets through becomes a live
 * counter target.
 */
class CustomTargetValidationTest {
    @Test
    fun plainNumberIsValid() {
        assertEquals(CustomTargetValidation.VALID, validateCustomTarget("33"))
    }

    @Test
    fun surroundingWhitespaceIsTrimmedBeforeValidating() {
        assertEquals(CustomTargetValidation.VALID, validateCustomTarget("  100  "))
    }

    @Test
    fun emptyAndWhitespaceOnlyInputAreReportedAsEmpty() {
        assertEquals(CustomTargetValidation.EMPTY, validateCustomTarget(""))
        assertEquals(CustomTargetValidation.EMPTY, validateCustomTarget("   "))
    }

    @Test
    fun nonNumericInputIsRejected() {
        listOf("abc", "3.5", "1e3", "12a", "33 33").forEach { raw ->
            assertEquals("input '$raw'", CustomTargetValidation.NON_NUMERIC, validateCustomTarget(raw))
        }
    }

    @Test
    fun arabicIndicDigitsAreAcceptedAsTheNumberTheySpell() {
        // Kotlin's toIntOrNull goes through Character.digit, which understands non-ASCII digits —
        // so a reader on an Arabic-numeral keyboard gets a working target rather than a rejection.
        // Documented here because it is easy to assume the opposite and "fix" it away.
        assertEquals(CustomTargetValidation.VALID, validateCustomTarget("\u0663\u0663"))
    }

    @Test
    fun overflowingIntegerIsNonNumericRatherThanAccepted() {
        // toIntOrNull returns null past Int.MAX_VALUE — the important part is that it does not wrap
        // around into a small, apparently-valid target.
        assertEquals(CustomTargetValidation.NON_NUMERIC, validateCustomTarget("9999999999"))
    }

    @Test
    fun zeroIsCalledOutSeparatelyFromNegative() {
        assertEquals(CustomTargetValidation.ZERO, validateCustomTarget("0"))
    }

    @Test
    fun negativeInputIsRejected() {
        assertEquals(CustomTargetValidation.NEGATIVE, validateCustomTarget("-5"))
    }

    @Test
    fun boundsAreInclusive() {
        assertEquals(
            CustomTargetValidation.VALID,
            validateCustomTarget(TasbihTargetPreset.MIN_CUSTOM_TARGET.toString()),
        )
        assertEquals(
            CustomTargetValidation.VALID,
            validateCustomTarget(TasbihTargetPreset.MAX_CUSTOM_TARGET.toString()),
        )
    }

    @Test
    fun oneAboveTheCeilingIsRejected() {
        assertEquals(
            CustomTargetValidation.TOO_LARGE,
            validateCustomTarget((TasbihTargetPreset.MAX_CUSTOM_TARGET + 1).toString()),
        )
    }

    @Test
    fun realIstighosahRepetitionCountFitsUnderTheCeiling() {
        // The bundled Istighosah content records a 30,000x repetition; the ceiling exists to reject
        // pathological input, not real amaliyah.
        assertEquals(CustomTargetValidation.VALID, validateCustomTarget("30000"))
    }
}
