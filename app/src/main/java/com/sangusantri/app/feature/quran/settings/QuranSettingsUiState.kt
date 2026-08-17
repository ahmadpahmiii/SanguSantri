package com.sangusantri.app.feature.quran.settings

import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranReaderSettings
import com.sangusantri.app.domain.model.QuranSurahHeaderVariant
import com.sangusantri.app.feature.quran.reader.QuranReaderAyatUiModel

data class QuranSettingsUiState(
    val displayMode: QuranDisplayMode = QuranDisplayMode.ARAB_ONLY,
    val arabicFont: QuranArabicFont = QuranArabicFont.LPMQ_ISEP_MISBAH,
    val arabicSizeSp: Int = QuranReaderSettings.DEFAULT_ARABIC_SIZE_SP,
    val arabicLineSpacingMultiplier: Float = QuranReaderSettings.DEFAULT_ARABIC_LINE_SPACING,
    val translationSizeSp: Int = QuranReaderSettings.DEFAULT_TRANSLATION_SIZE_SP,
    val brightnessOverride: Float?,
    val surahHeaderVariant: QuranSurahHeaderVariant = QuranSurahHeaderVariant.TENANG,
    /** A real, currently locally stored verse (Al-Fatihah ayat 1 when available) used for the live
     * settings preview — never invented text (`docs/design/QURAN_DESIGN_SYSTEM.md` §4: "the exact
     * same verified Kemenag ayat fragment"). `null` before local preparation completes. */
    val previewAyat: QuranReaderAyatUiModel?,
)
