package com.sangusantri.app.domain.model

/**
 * Quran reader appearance settings (QUR-FR-015). Persisted immediately on every change, applied
 * live to an already-open reader — there is no separate Save action or draft state.
 *
 * [brightnessOverride] is `null` when the Quran window should follow the system/app brightness
 * (never overridden); once the user moves the brightness slider it holds `0f..1f` and is
 * re-applied every time the Quran window opens, restoring the prior window value on exit
 * (QUR-FR-015).
 *
 * [themeMode] is likewise `null` until the user picks a mode — the app follows the system setting
 * until then. It is app-wide since the Beranda/Quran revamp, not a Quran-only surface setting; it
 * still lives here because this is where it is already persisted, and a separate key would need a
 * migration to buy nothing.
 *
 * [arabicFont] is the same kind of exception: a single app-wide typeface choice, also applied to
 * the amaliyah reader's Arabic text (`ReaderSettings.arabicFont`), read from this store rather
 * than duplicated into a second one.
 */
data class QuranReaderSettings(
    val displayMode: QuranDisplayMode = QuranDisplayMode.ARAB_ONLY,
    val arabicFont: QuranArabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH,
    val arabicSizeSp: Int = DEFAULT_ARABIC_SIZE_SP,
    val arabicLineSpacingMultiplier: Float = DEFAULT_ARABIC_LINE_SPACING,
    val translationSizeSp: Int = DEFAULT_TRANSLATION_SIZE_SP,
    val brightnessOverride: Float? = null,
    val themeMode: AppThemeMode? = null,
    val murottalSpeed: QuranMurottalSpeed = QuranMurottalSpeed.NORMAL,
    /** "Lanjut otomatis antarsurah" — on by default, per the murottal panel's design. */
    val murottalContinueAcrossSurah: Boolean = true,
    /** "Layar tetap menyala" while a recitation is playing. */
    val murottalKeepScreenOn: Boolean = false,
) {
    companion object {
        // 27sp / 2.4x are the revamp's reader defaults (handoff §Typography). The design lists a
        // slightly larger 29sp / 2.55x for mushaf mode; that is deliberately collapsed onto this
        // one user-controlled pair rather than splitting the size and line-spacing sliders per
        // display mode, which would double the settings surface for a two-step difference.
        const val DEFAULT_ARABIC_SIZE_SP = 27
        const val MIN_ARABIC_SIZE_SP = 14
        const val MAX_ARABIC_SIZE_SP = 52

        const val DEFAULT_ARABIC_LINE_SPACING = 2.40f
        const val MIN_ARABIC_LINE_SPACING = 1.50f
        const val MAX_ARABIC_LINE_SPACING = 5.00f

        const val DEFAULT_TRANSLATION_SIZE_SP = 16
        const val MIN_TRANSLATION_SIZE_SP = 14
        const val MAX_TRANSLATION_SIZE_SP = 24

        fun coerceArabicSize(sp: Int): Int = sp.coerceIn(MIN_ARABIC_SIZE_SP, MAX_ARABIC_SIZE_SP)

        fun coerceArabicLineSpacing(multiplier: Float): Float =
            multiplier.coerceIn(MIN_ARABIC_LINE_SPACING, MAX_ARABIC_LINE_SPACING)

        fun coerceTranslationSize(sp: Int): Int = sp.coerceIn(MIN_TRANSLATION_SIZE_SP, MAX_TRANSLATION_SIZE_SP)

        fun coerceBrightness(value: Float): Float = value.coerceIn(0f, 1f)
    }
}
