package com.sangusantri.app.domain.repository

import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.model.QuranMurottalSpeed
import com.sangusantri.app.domain.model.QuranReaderSettings
import kotlinx.coroutines.flow.Flow

/** Quran reader appearance settings (QUR-FR-015), DataStore-backed like the existing
 * [ReaderSettingsRepository] but a separate preference namespace — Quran is a distinct feature
 * with its own default values and ranges, not a shared setting with the amaliyah reader. */
// One setter per independently persisted preference is this interface's whole job; the murottal
// additions follow the same one-key-one-method shape as the type and theme settings above.
@Suppress("TooManyFunctions")
interface QuranReaderSettingsRepository {
    fun observe(): Flow<QuranReaderSettings>

    suspend fun setDisplayMode(mode: QuranDisplayMode)

    suspend fun setArabicFont(font: QuranArabicFont)

    suspend fun setArabicSize(sp: Int)

    suspend fun setArabicLineSpacing(multiplier: Float)

    suspend fun setTranslationSize(sp: Int)

    suspend fun setBrightnessOverride(value: Float)

    /** App-wide since the Beranda/Quran revamp, despite living in this Quran-namespaced store —
     * see [com.sangusantri.app.domain.model.QuranReaderSettings.themeMode]. Callers always pass a
     * concrete mode: the top-bar toggle reads the resolved
     * [com.sangusantri.app.core.designsystem.theme.LocalAppThemeMode] and sends its opposite, which
     * is also what turns an unset (system-following) value into an explicit choice. */
    suspend fun setThemeMode(mode: AppThemeMode)

    suspend fun setMurottalSpeed(speed: QuranMurottalSpeed)

    suspend fun setMurottalContinueAcrossSurah(enabled: Boolean)

    suspend fun setMurottalKeepScreenOn(enabled: Boolean)
}
