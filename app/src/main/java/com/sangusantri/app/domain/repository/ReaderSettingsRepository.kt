package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.ReaderSettings
import kotlinx.coroutines.flow.Flow

/** Reads and writes Full Reader appearance preferences (FR-008). Backed by DataStore, not Room. */
interface ReaderSettingsRepository {
    fun observe(): Flow<ReaderSettings>

    suspend fun setArabicFontSize(sp: Int)

    suspend fun setTranslationFontSize(sp: Int)

    suspend fun setArabicLineSpacing(multiplier: Float)

    suspend fun setTranslationLineSpacing(multiplier: Float)

    suspend fun setShowTranslation(show: Boolean)
}
