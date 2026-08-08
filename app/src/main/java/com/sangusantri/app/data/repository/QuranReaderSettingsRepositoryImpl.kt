package com.sangusantri.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranReaderSettings
import com.sangusantri.app.domain.model.QuranReaderSettings.Companion.coerceArabicLineSpacing
import com.sangusantri.app.domain.model.QuranReaderSettings.Companion.coerceArabicSize
import com.sangusantri.app.domain.model.QuranReaderSettings.Companion.coerceBrightness
import com.sangusantri.app.domain.model.QuranReaderSettings.Companion.coerceTranslationSize
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/** Mirrors [ReaderSettingsRepositoryImpl]'s corruption-safe coerce-on-read pattern, in the shared
 * preferences DataStore but under a `quran_`-namespaced key set of its own. */
class QuranReaderSettingsRepositoryImpl
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
) : QuranReaderSettingsRepository {
    override fun observe(): Flow<QuranReaderSettings> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }.map { preferences ->
                QuranReaderSettings(
                    displayMode = preferences[DISPLAY_MODE]?.let(::parseDisplayMode) ?: QuranDisplayMode.ARAB_ONLY,
                    arabicSizeSp =
                        coerceArabicSize(preferences[ARABIC_SIZE_SP] ?: QuranReaderSettings.DEFAULT_ARABIC_SIZE_SP),
                    arabicLineSpacingMultiplier =
                        coerceArabicLineSpacing(
                            preferences[ARABIC_LINE_SPACING] ?: QuranReaderSettings.DEFAULT_ARABIC_LINE_SPACING,
                        ),
                    translationSizeSp =
                        coerceTranslationSize(
                            preferences[TRANSLATION_SIZE_SP] ?: QuranReaderSettings.DEFAULT_TRANSLATION_SIZE_SP,
                        ),
                    brightnessOverride = preferences[BRIGHTNESS_OVERRIDE]?.let(::coerceBrightness),
                )
            }

    override suspend fun setDisplayMode(mode: QuranDisplayMode) {
        dataStore.edit { it[DISPLAY_MODE] = mode.name }
    }

    override suspend fun setArabicSize(sp: Int) {
        dataStore.edit { it[ARABIC_SIZE_SP] = coerceArabicSize(sp) }
    }

    override suspend fun setArabicLineSpacing(multiplier: Float) {
        dataStore.edit { it[ARABIC_LINE_SPACING] = coerceArabicLineSpacing(multiplier) }
    }

    override suspend fun setTranslationSize(sp: Int) {
        dataStore.edit { it[TRANSLATION_SIZE_SP] = coerceTranslationSize(sp) }
    }

    override suspend fun setBrightnessOverride(value: Float) {
        dataStore.edit { it[BRIGHTNESS_OVERRIDE] = coerceBrightness(value) }
    }

    private fun parseDisplayMode(value: String): QuranDisplayMode? =
        runCatching { QuranDisplayMode.valueOf(value) }.getOrNull()

    private companion object {
        val DISPLAY_MODE = stringPreferencesKey("quran_display_mode")
        val ARABIC_SIZE_SP = intPreferencesKey("quran_arabic_size_sp")
        val ARABIC_LINE_SPACING = floatPreferencesKey("quran_arabic_line_spacing")
        val TRANSLATION_SIZE_SP = intPreferencesKey("quran_translation_size_sp")
        val BRIGHTNESS_OVERRIDE = floatPreferencesKey("quran_brightness_override")
    }
}
