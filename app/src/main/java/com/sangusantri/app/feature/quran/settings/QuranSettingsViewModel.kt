package com.sangusantri.app.feature.quran.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.audio.QuranAudioStore
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.QuranArabicFont
import com.sangusantri.app.domain.model.QuranDisplayMode
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.QuranRepository
import com.sangusantri.app.feature.quran.reader.toReaderUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val AL_FATIHAH_SURAH_NUMBER = 1

/** Owns Tampilan Al-Qur'an (QUR-FR-015): every change persists immediately and applies live to an
 * already-open reader through the shared [QuranReaderSettingsRepository] Flow — there is no
 * separate Save action or uncommitted draft state. */
@HiltViewModel
class QuranSettingsViewModel
@Inject
constructor(
    private val settingsRepository: QuranReaderSettingsRepository,
    private val audioStore: QuranAudioStore,
    quranRepository: QuranRepository,
) : ViewModel() {
    private val audioStorageSheetVisible = MutableStateFlow(false)

    val uiState: StateFlow<QuranSettingsUiState> =
        combine(
            settingsRepository.observe(),
            quranRepository.observeVersesBySurah(AL_FATIHAH_SURAH_NUMBER),
            quranRepository.observeSurahs(),
            audioStore.library,
            audioStorageSheetVisible,
        ) { settings, verses, surahs, library, storageSheetVisible ->
            val surahName = surahs.firstOrNull { it.number == AL_FATIHAH_SURAH_NUMBER }?.latinName.orEmpty()
            QuranSettingsUiState(
                displayMode = settings.displayMode,
                arabicFont = settings.arabicFont,
                arabicSizeSp = settings.arabicSizeSp,
                arabicLineSpacingMultiplier = settings.arabicLineSpacingMultiplier,
                translationSizeSp = settings.translationSizeSp,
                brightnessOverride = settings.brightnessOverride,
                storedAudioSurahCount = library.surahCount,
                storedAudioBytes = library.totalBytes,
                audioStorageSheetVisible = storageSheetVisible,
                previewAyat = verses.firstOrNull()?.toReaderUiModel(surahName),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            QuranSettingsUiState(brightnessOverride = null, previewAyat = null),
        )

    fun setDisplayMode(mode: QuranDisplayMode) {
        viewModelScope.launch { settingsRepository.setDisplayMode(mode) }
    }

    fun setArabicFont(font: QuranArabicFont) {
        viewModelScope.launch { settingsRepository.setArabicFont(font) }
    }

    fun setArabicSize(sp: Int) {
        viewModelScope.launch { settingsRepository.setArabicSize(sp) }
    }

    fun setArabicLineSpacing(multiplier: Float) {
        viewModelScope.launch { settingsRepository.setArabicLineSpacing(multiplier) }
    }

    fun setTranslationSize(sp: Int) {
        viewModelScope.launch { settingsRepository.setTranslationSize(sp) }
    }

    fun setBrightness(value: Float) {
        viewModelScope.launch { settingsRepository.setBrightnessOverride(value) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun openAudioStorage() {
        audioStorageSheetVisible.value = true
    }

    fun dismissAudioStorage() {
        audioStorageSheetVisible.value = false
    }

    /**
     * Clears the whole stored murottal library — reachable only from the storage sheet, which
     * states the amount and that Quran text is unaffected before offering it.
     *
     * Still whole-library rather than per-surah: a per-surah management list is not part of the
     * approved frames, and inventing one would be scope the design has not specified.
     */
    fun deleteAllAudio() {
        viewModelScope.launch {
            audioStore.deleteAll()
            audioStorageSheetVisible.value = false
        }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5000L
    }
}
