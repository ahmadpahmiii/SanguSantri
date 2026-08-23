package com.sangusantri.app.domain.model

/**
 * Reader preferences (FR-008 appearance, minus theme/background/language — either not yet
 * supported anywhere in the app or out of scope). Extended in Milestone 4 with the last-selected
 * reader mode (PRD 8.2: "the chosen mode may be saved as the default later") and the Guided Reader
 * progression preference (FR-005) — both preferences, so both belong in the same DataStore-backed
 * model rather than a second store. Persisted in DataStore, never Room (PRD 11.2).
 *
 * [sholawatTwoColumn]/[sholawatBaitGap] are Sholawat-reader display preferences. They live here
 * rather than in a second store because the product decision is one reading preference across every
 * Arabic reading surface — changing the Arabic size in Sholawat changes it in Tahlil too — and a
 * separate store for two booleans would buy nothing but a second migration.
 *
 * [arabicFont] is the one field here not sourced from this model's own DataStore key: it mirrors
 * [QuranReaderSettings.themeMode] — a single app-wide font choice, shared with the Quran reader
 * through [com.sangusantri.app.domain.repository.QuranReaderSettingsRepository], read into this
 * model by the owning ViewModel rather than duplicated into a second store.
 */
data class ReaderSettings(
    val arabicFont: QuranArabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH,
    val arabicFontSizeSp: Int = DEFAULT_ARABIC_FONT_SIZE_SP,
    val translationFontSizeSp: Int = DEFAULT_TRANSLATION_FONT_SIZE_SP,
    val arabicLineSpacingMultiplier: Float = DEFAULT_ARABIC_LINE_SPACING,
    val translationLineSpacingMultiplier: Float = DEFAULT_TRANSLATION_LINE_SPACING,
    val showTranslation: Boolean = true,
    val lastReaderMode: ReaderMode? = null,
    val guidedProgressionMode: GuidedProgressionMode = GuidedProgressionMode.MANUAL,
    /**
     * Sholawat reader only: whether a qasidah the CMS marked `bayt` is actually drawn in two
     * columns. The CMS flag says an item *can* be paired; this says the reader *should* pair it.
     * Only ever narrows — an item the CMS calls prose is never paired, whatever this holds, because
     * two columns split prose sentences across columns.
     */
    val sholawatTwoColumn: Boolean = true,
    /** Sholawat reader only: extra breathing room between baits, versus a tight continuous block. */
    val sholawatBaitGap: Boolean = true,
) {
    companion object {
        // 16/22 set by the product owner on 2026-08-24 after reading on a real device: the
        // previous 20sp floor was still large, and the 28sp default was noticeably bigger than the
        // Quran reader's 27sp in practice. Both readers share these, so Sholawat and Amaliyah move
        // together. Every value on the 2sp step grid from 16 stays reachable, 22 included.
        const val MIN_ARABIC_FONT_SIZE_SP = 16
        const val MAX_ARABIC_FONT_SIZE_SP = 40
        const val DEFAULT_ARABIC_FONT_SIZE_SP = 22
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
