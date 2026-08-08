package com.sangusantri.app.domain.model

/**
 * Quran reader appearance settings (QUR-FR-015). Persisted immediately on every change, applied
 * live to an already-open reader — there is no separate Save action or draft state.
 *
 * [brightnessOverride] is `null` when the Quran window should follow the system/app brightness
 * (never overridden); once the user moves the brightness slider it holds `0f..1f` and is
 * re-applied every time the Quran window opens, restoring the prior window value on exit
 * (QUR-FR-015).
 */
data class QuranReaderSettings(
    val displayMode: QuranDisplayMode = QuranDisplayMode.ARAB_ONLY,
    val arabicSizeSp: Int = DEFAULT_ARABIC_SIZE_SP,
    val arabicLineSpacingMultiplier: Float = DEFAULT_ARABIC_LINE_SPACING,
    val translationSizeSp: Int = DEFAULT_TRANSLATION_SIZE_SP,
    val brightnessOverride: Float? = null,
) {
    companion object {
        const val DEFAULT_ARABIC_SIZE_SP = 34
        const val MIN_ARABIC_SIZE_SP = 24
        const val MAX_ARABIC_SIZE_SP = 52

        const val DEFAULT_ARABIC_LINE_SPACING = 1.75f
        const val MIN_ARABIC_LINE_SPACING = 1.45f
        const val MAX_ARABIC_LINE_SPACING = 2.20f

        const val DEFAULT_TRANSLATION_SIZE_SP = 17
        const val MIN_TRANSLATION_SIZE_SP = 14
        const val MAX_TRANSLATION_SIZE_SP = 24

        fun coerceArabicSize(sp: Int): Int = sp.coerceIn(MIN_ARABIC_SIZE_SP, MAX_ARABIC_SIZE_SP)

        fun coerceArabicLineSpacing(multiplier: Float): Float =
            multiplier.coerceIn(MIN_ARABIC_LINE_SPACING, MAX_ARABIC_LINE_SPACING)

        fun coerceTranslationSize(sp: Int): Int = sp.coerceIn(MIN_TRANSLATION_SIZE_SP, MAX_TRANSLATION_SIZE_SP)

        fun coerceBrightness(value: Float): Float = value.coerceIn(0f, 1f)
    }
}
