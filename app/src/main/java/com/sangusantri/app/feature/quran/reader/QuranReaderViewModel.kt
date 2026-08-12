package com.sangusantri.app.feature.quran.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranReaderSettings
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranTafsirResult
import com.sangusantri.app.domain.model.QuranVerse
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.QuranRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Owns one Quran surah reading session (QUR-FR-008/009/010/011/012/014/017): loads the surah's
 * verses from Room, applies live reader settings, tracks long-press ayat selection for the action
 * sheet, and records reading-position/session progress only after the visible ayat actually
 * changes (QUR-FR-011/017) — never on mere open/close.
 */
@Suppress("TooManyFunctions")
@HiltViewModel(assistedFactory = QuranReaderViewModel.Factory::class)
class QuranReaderViewModel
@AssistedInject
constructor(
    @Assisted private val surahNumber: Int,
    private val quranRepository: QuranRepository,
    private val settingsRepository: QuranReaderSettingsRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(surahNumber: Int): QuranReaderViewModel
    }

    private val selectedAyatId = MutableStateFlow<Long?>(null)
    private val tafsirSheetOpen = MutableStateFlow(false)
    private var startAyatNumber: Int? = null
    private var lastVisibleAyatNumber: Int? = null
    private var sessionRecorded = false
    private var tafsirJob: Job? = null

    private val _tafsirUiState = MutableStateFlow<QuranTafsirUiState>(QuranTafsirUiState.Loading)
    val tafsirUiState: StateFlow<QuranTafsirUiState> = _tafsirUiState

    private val roomData =
        combine(
            quranRepository.observeSurahs(),
            quranRepository.observeVersesBySurah(surahNumber),
            settingsRepository.observe(),
            quranRepository.observeBookmarks(),
        ) { surahs, verses, settings, bookmarks -> QuranReaderRoomData(surahs, verses, settings, bookmarks) }

    val uiState: StateFlow<QuranReaderUiState> =
        combine(roomData, selectedAyatId, tafsirSheetOpen) { data, selected, tafsirOpen ->
            buildState(data, selected, tafsirOpen)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            QuranReaderUiState.Loading,
        )

    fun onAyatLongPress(ayat: QuranReaderAyatUiModel) {
        selectedAyatId.value = ayat.remoteId
        tafsirSheetOpen.value = false
    }

    fun onDismissActionSheet() {
        selectedAyatId.value = null
    }

    fun onOpenTafsir(ayat: QuranReaderAyatUiModel) {
        selectedAyatId.value = ayat.remoteId
        tafsirSheetOpen.value = true
        loadTafsir(ayat.remoteId)
    }

    fun onDismissTafsirSheet() {
        tafsirJob?.cancel()
        tafsirSheetOpen.value = false
        selectedAyatId.value = null
    }

    fun onRetryTafsir(remoteAyatId: Long) {
        loadTafsir(remoteAyatId)
    }

    private fun loadTafsir(remoteAyatId: Long) {
        tafsirJob?.cancel()
        tafsirJob =
            viewModelScope.launch {
                val cached = quranRepository.getCachedTafsir(remoteAyatId)
                if (cached == null) {
                    _tafsirUiState.value = QuranTafsirUiState.Loading
                    when (val result = quranRepository.fetchTafsir(remoteAyatId)) {
                        is QuranTafsirResult.Success ->
                            _tafsirUiState.value = QuranTafsirUiState.Loaded(result.tafsir, isRefreshing = false)

                        is QuranTafsirResult.Failure ->
                            _tafsirUiState.value = QuranTafsirUiState.Unavailable(result.retryable)
                    }
                    return@launch
                }

                val stale = System.currentTimeMillis() - cached.cachedAtEpochMillis >= TAFSIR_STALE_THRESHOLD_MILLIS
                _tafsirUiState.value = QuranTafsirUiState.Loaded(cached, isRefreshing = stale)
                if (stale) {
                    when (val result = quranRepository.fetchTafsir(remoteAyatId)) {
                        is QuranTafsirResult.Success ->
                            _tafsirUiState.value = QuranTafsirUiState.Loaded(result.tafsir, isRefreshing = false)
                        // Refresh failed — keep showing the still-valid stale cache, just stop spinning.
                        is QuranTafsirResult.Failure ->
                            _tafsirUiState.value = QuranTafsirUiState.Loaded(cached, isRefreshing = false)
                    }
                }
            }
    }

    fun onToggleBookmark(ayat: QuranReaderAyatUiModel) {
        viewModelScope.launch { quranRepository.toggleBookmark(surahNumber, ayat.ayatNumber) }
    }

    fun onMarkLastRead(ayat: QuranReaderAyatUiModel) {
        viewModelScope.launch { quranRepository.setLastRead(surahNumber, ayat.ayatNumber, ayat.page) }
        onDismissActionSheet()
    }

    fun toggleTheme() {
        viewModelScope.launch { settingsRepository.toggleThemeMode() }
    }

    /** Called as the visible ayat changes while scrolling (QUR-FR-011) — the first call seeds
     * the session's starting position, every call updates the last-seen position. */
    fun onVisiblePositionChanged(ayatNumber: Int) {
        if (startAyatNumber == null) startAyatNumber = ayatNumber
        lastVisibleAyatNumber = ayatNumber
        val page =
            (uiState.value as? QuranReaderUiState.Content)
                ?.ayats
                ?.firstOrNull {
                    it.ayatNumber == ayatNumber
                }?.page
        if (page != null) {
            viewModelScope.launch { quranRepository.setLastRead(surahNumber, ayatNumber, page) }
        }
    }

    /** Called when the reader closes (QUR-FR-017) — writes one session only if the position
     * genuinely advanced from where it started. */
    fun recordSessionIfAdvanced() {
        val start = startAyatNumber
        val last = lastVisibleAyatNumber
        if (!sessionRecorded && start != null && last != null) {
            if (last > start) {
                sessionRecorded = true
                viewModelScope.launch { quranRepository.recordReadingSession(surahNumber, start, last) }
            }
        }
    }

    @Suppress("ReturnCount")
    private fun buildState(
        data: QuranReaderRoomData,
        selectedAyatId: Long?,
        tafsirOpen: Boolean,
    ): QuranReaderUiState {
        val surah = data.surahs.firstOrNull { it.number == surahNumber } ?: return QuranReaderUiState.Unavailable
        if (data.verses.isEmpty()) return QuranReaderUiState.Loading

        val settings = data.settings
        val ayats = data.verses.map { it.toReaderUiModel(surah.latinName) }
        val selectedAyat = ayats.firstOrNull { it.remoteId == selectedAyatId }
        val isSelectedBookmarked =
            selectedAyat != null &&
                data.bookmarks.any { it.surahNumber == surahNumber && it.ayatNumber == selectedAyat.ayatNumber }

        return QuranReaderUiState.Content(
            surahNumber = surah.number,
            surahName = surah.latinName,
            category = surah.category,
            ayatCount = surah.ayatCount,
            displayMode = settings.displayMode,
            arabicFont = settings.arabicFont,
            ayats = ayats,
            pages = ayats.groupBy { it.page }.values.toList(),
            selectedAyat = selectedAyat,
            isSelectedBookmarked = isSelectedBookmarked,
            arabicSizeSp = settings.arabicSizeSp,
            arabicLineHeightSp = (settings.arabicSizeSp * settings.arabicLineSpacingMultiplier).roundToInt(),
            translationSizeSp = settings.translationSizeSp,
            brightnessOverride = settings.brightnessOverride,
            tafsirSheetOpen = tafsirOpen,
        )
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5000L
        val TAFSIR_STALE_THRESHOLD_MILLIS = TimeUnit.DAYS.toMillis(7)
    }
}

private data class QuranReaderRoomData(
    val surahs: List<QuranSurah>,
    val verses: List<QuranVerse>,
    val settings: QuranReaderSettings,
    val bookmarks: List<QuranBookmark>,
)

internal fun QuranVerse.toReaderUiModel(surahName: String): QuranReaderAyatUiModel =
    QuranReaderAyatUiModel(
        remoteId = remoteId,
        surahName = surahName,
        ayatNumber = ayatNumber,
        juz = juz,
        page = page,
        arabicText = arabicText,
        translation = translation,
        note = note,
        footnoteNumber = footnoteNumber,
        footnoteText = footnoteText,
    )
