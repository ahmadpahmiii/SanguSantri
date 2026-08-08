package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranReaderSettings
import kotlinx.coroutines.flow.Flow

/** Quran reader appearance settings (QUR-FR-015), DataStore-backed like the existing
 * [ReaderSettingsRepository] but a separate preference namespace — Quran is a distinct feature
 * with its own default values and ranges, not a shared setting with the amaliyah reader. */
interface QuranReaderSettingsRepository {
    fun observe(): Flow<QuranReaderSettings>

    suspend fun setDisplayMode(mode: QuranDisplayMode)

    suspend fun setArabicSize(sp: Int)

    suspend fun setArabicLineSpacing(multiplier: Float)

    suspend fun setTranslationSize(sp: Int)

    suspend fun setBrightnessOverride(value: Float)
}
