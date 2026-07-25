package com.sangusantri.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sangusantri.app.domain.model.GuidedProgressionMode
import com.sangusantri.app.domain.model.ReaderMode
import com.sangusantri.app.domain.model.ReaderSettings
import com.sangusantri.app.domain.model.ReaderSettings.Companion.coerceArabicFontSize
import com.sangusantri.app.domain.model.ReaderSettings.Companion.coerceLineSpacing
import com.sangusantri.app.domain.model.ReaderSettings.Companion.coerceTranslationFontSize
import com.sangusantri.app.domain.repository.ReaderSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * Reads and writes Full Reader settings via the shared preferences DataStore (PRD 11.2). Every
 * read coerces stored values back into range, so a value persisted by a wider bound in a future
 * release (or corrupted on disk) always falls back to a safe, currently valid value instead of
 * being rendered as-is.
 */
class ReaderSettingsRepositoryImpl
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
) : ReaderSettingsRepository {
    override fun observe(): Flow<ReaderSettings> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }.map { preferences ->
                ReaderSettings(
                    arabicFontSizeSp =
                        coerceArabicFontSize(
                            preferences[ARABIC_FONT_SIZE_SP] ?: ReaderSettings.DEFAULT_ARABIC_FONT_SIZE_SP,
                        ),
                    translationFontSizeSp =
                        coerceTranslationFontSize(
                            preferences[TRANSLATION_FONT_SIZE_SP]
                                ?: ReaderSettings.DEFAULT_TRANSLATION_FONT_SIZE_SP,
                        ),
                    arabicLineSpacingMultiplier =
                        coerceLineSpacing(
                            preferences[ARABIC_LINE_SPACING] ?: ReaderSettings.DEFAULT_ARABIC_LINE_SPACING,
                        ),
                    translationLineSpacingMultiplier =
                        coerceLineSpacing(
                            preferences[TRANSLATION_LINE_SPACING]
                                ?: ReaderSettings.DEFAULT_TRANSLATION_LINE_SPACING,
                        ),
                    showTranslation = preferences[SHOW_TRANSLATION] ?: true,
                    lastReaderMode = preferences[LAST_READER_MODE]?.let(::parseReaderMode),
                    guidedProgressionMode =
                        preferences[GUIDED_PROGRESSION_MODE]?.let(::parseProgressionMode)
                            ?: GuidedProgressionMode.MANUAL,
                )
            }

    override suspend fun setArabicFontSize(sp: Int) {
        dataStore.edit { it[ARABIC_FONT_SIZE_SP] = coerceArabicFontSize(sp) }
    }

    override suspend fun setTranslationFontSize(sp: Int) {
        dataStore.edit { it[TRANSLATION_FONT_SIZE_SP] = coerceTranslationFontSize(sp) }
    }

    override suspend fun setArabicLineSpacing(multiplier: Float) {
        dataStore.edit { it[ARABIC_LINE_SPACING] = coerceLineSpacing(multiplier) }
    }

    override suspend fun setTranslationLineSpacing(multiplier: Float) {
        dataStore.edit { it[TRANSLATION_LINE_SPACING] = coerceLineSpacing(multiplier) }
    }

    override suspend fun setShowTranslation(show: Boolean) {
        dataStore.edit { it[SHOW_TRANSLATION] = show }
    }

    override suspend fun setLastReaderMode(mode: ReaderMode) {
        dataStore.edit { it[LAST_READER_MODE] = mode.name }
    }

    override suspend fun setGuidedProgressionMode(mode: GuidedProgressionMode) {
        dataStore.edit { it[GUIDED_PROGRESSION_MODE] = mode.name }
    }

    // A future stored value outside the current enum's names (e.g. after a renamed constant) falls
    // back to null/MANUAL instead of crashing, the same corruption-safety net as the coerce* numeric
    // bounds above.
    private fun parseReaderMode(value: String): ReaderMode? = runCatching { ReaderMode.valueOf(value) }.getOrNull()

    private fun parseProgressionMode(value: String): GuidedProgressionMode? =
        runCatching { GuidedProgressionMode.valueOf(value) }.getOrNull()

    private companion object {
        val ARABIC_FONT_SIZE_SP = intPreferencesKey("reader_arabic_font_size_sp")
        val TRANSLATION_FONT_SIZE_SP = intPreferencesKey("reader_translation_font_size_sp")
        val ARABIC_LINE_SPACING = floatPreferencesKey("reader_arabic_line_spacing")
        val TRANSLATION_LINE_SPACING = floatPreferencesKey("reader_translation_line_spacing")
        val SHOW_TRANSLATION = booleanPreferencesKey("reader_show_translation")
        val LAST_READER_MODE = stringPreferencesKey("reader_last_mode")
        val GUIDED_PROGRESSION_MODE = stringPreferencesKey("guided_progression_mode")
    }
}
