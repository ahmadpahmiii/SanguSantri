package com.sangusantri.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderSettingsTest {
    @Test
    fun defaultsAreWithinBounds() {
        val settings = ReaderSettings()

        assertEquals(ReaderSettings.DEFAULT_ARABIC_FONT_SIZE_SP, settings.arabicFontSizeSp)
        assertEquals(ReaderSettings.DEFAULT_TRANSLATION_FONT_SIZE_SP, settings.translationFontSizeSp)
        assertEquals(ReaderSettings.DEFAULT_ARABIC_LINE_SPACING, settings.arabicLineSpacingMultiplier, 0.0f)
        assertEquals(ReaderSettings.DEFAULT_TRANSLATION_LINE_SPACING, settings.translationLineSpacingMultiplier, 0.0f)
        assertTrue(settings.showTranslation)
    }

    @Test
    fun coerceArabicFontSizeClampsBelowMinimum() {
        assertEquals(ReaderSettings.MIN_ARABIC_FONT_SIZE_SP, ReaderSettings.coerceArabicFontSize(0))
    }

    @Test
    fun coerceArabicFontSizeClampsAboveMaximum() {
        assertEquals(ReaderSettings.MAX_ARABIC_FONT_SIZE_SP, ReaderSettings.coerceArabicFontSize(999))
    }

    @Test
    fun coerceArabicFontSizePassesThroughValidValue() {
        val valid = ReaderSettings.MIN_ARABIC_FONT_SIZE_SP + 2
        assertEquals(valid, ReaderSettings.coerceArabicFontSize(valid))
    }

    @Test
    fun coerceTranslationFontSizeClampsToBounds() {
        assertEquals(ReaderSettings.MIN_TRANSLATION_FONT_SIZE_SP, ReaderSettings.coerceTranslationFontSize(-5))
        assertEquals(ReaderSettings.MAX_TRANSLATION_FONT_SIZE_SP, ReaderSettings.coerceTranslationFontSize(500))
    }

    @Test
    fun coerceLineSpacingClampsToBounds() {
        assertEquals(ReaderSettings.MIN_LINE_SPACING, ReaderSettings.coerceLineSpacing(0.1f), 0.0f)
        assertEquals(ReaderSettings.MAX_LINE_SPACING, ReaderSettings.coerceLineSpacing(10f), 0.0f)
    }
}
