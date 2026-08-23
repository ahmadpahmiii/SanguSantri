package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.GuidedProgressionMode
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.ReaderSettings
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes reader preferences: Full Reader appearance (FR-008), the last-selected reader
 * mode (PRD 8.2), the Guided Reader progression preference (FR-005), and the Sholawat reader's
 * two-column/bait-gap display preferences (FR-SHL-013). Backed by DataStore, not Room.
 */
interface ReaderSettingsRepository {
    fun observe(): Flow<ReaderSettings>

    suspend fun setArabicFontSize(sp: Int)

    suspend fun setTranslationFontSize(sp: Int)

    suspend fun setArabicLineSpacing(multiplier: Float)

    suspend fun setTranslationLineSpacing(multiplier: Float)

    suspend fun setShowTranslation(show: Boolean)

    suspend fun setLastReaderMode(mode: ReaderMode)

    suspend fun setGuidedProgressionMode(mode: GuidedProgressionMode)

    suspend fun setSholawatTwoColumn(enabled: Boolean)

    suspend fun setSholawatBaitGap(enabled: Boolean)
}
