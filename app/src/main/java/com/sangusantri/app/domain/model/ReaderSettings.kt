package com.sangusantri.app.domain.model

/**
 * Full Reader appearance preferences (FR-008, minus theme/background/guided-progression/language —
 * those are either not yet supported anywhere in the app or explicitly out of Milestone 3 scope).
 * Persisted in DataStore, never Room (PRD 11.2).
 */
data class ReaderSettings(
    val arabicFontSizeSp: Int = DEFAULT_ARABIC_FONT_SIZE_SP,
    val translationFontSizeSp: Int = DEFAULT_TRANSLATION_FONT_SIZE_SP,
    val arabicLineSpacingMultiplier: Float = DEFAULT_ARABIC_LINE_SPACING,
    val translationLineSpacingMultiplier: Float = DEFAULT_TRANSLATION_LINE_SPACING,
    val showTranslation: Boolean = true,
) {
    companion object {
        const val MIN_ARABIC_FONT_SIZE_SP = 20
        const val MAX_ARABIC_FONT_SIZE_SP = 40
        const val DEFAULT_ARABIC_FONT_SIZE_SP = 28
        const val ARABIC_FONT_SIZE_STEP_SP = 2

        const val MIN_TRANSLATION_FONT_SIZE_SP = 12
        const val MAX_TRANSLATION_FONT_SIZE_SP = 22
        const val DEFAULT_TRANSLATION_FONT_SIZE_SP = 16
        const val TRANSLATION_FONT_SIZE_STEP_SP = 1

        const val MIN_LINE_SPACING = 1.2f
        const val MAX_LINE_SPACING = 2.4f
        const val DEFAULT_ARABIC_LINE_SPACING = 1.9f
        const val DEFAULT_TRANSLATION_LINE_SPACING = 1.5f
        const val LINE_SPACING_STEP = 0.1f

        fun coerceArabicFontSize(sp: Int): Int = sp.coerceIn(MIN_ARABIC_FONT_SIZE_SP, MAX_ARABIC_FONT_SIZE_SP)

        fun coerceTranslationFontSize(sp: Int): Int =
            sp.coerceIn(MIN_TRANSLATION_FONT_SIZE_SP, MAX_TRANSLATION_FONT_SIZE_SP)

        fun coerceLineSpacing(multiplier: Float): Float = multiplier.coerceIn(MIN_LINE_SPACING, MAX_LINE_SPACING)
    }
}
