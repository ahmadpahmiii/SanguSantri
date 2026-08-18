package com.sangusantri.app.feature.quran.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangusantri.app.data.audio.QuranAudioDownloadManager
import com.sangusantri.app.data.audio.QuranAudioStore
import com.sangusantri.app.data.audio.QuranMurottalPlayer
import com.sangusantri.app.domain.model.AppThemeMode
import com.sangusantri.app.domain.model.QuranBookmark
import com.sangusantri.app.domain.model.QuranMurottalSpeed
import com.sangusantri.app.domain.model.QuranMurottalState
import com.sangusantri.app.domain.model.QuranReaderSettings
import com.sangusantri.app.domain.model.QuranSurah
import com.sangusantri.app.domain.model.QuranTafsirResult
import com.sangusantri.app.domain.model.QuranVerse
import com.sangusantri.app.domain.repository.QuranReaderSettingsRepository
import com.sangusantri.app.domain.repository.QuranRepository
import com.sangusantri.app.feature.quran.murottal.QuranMurottalPanelUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    private val murottalPlayer: QuranMurottalPlayer,
    private val audioStore: QuranAudioStore,
    private val audioDownloadManager: QuranAudioDownloadManager,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(surahNumber: Int): QuranReaderViewModel
    }

    private val selectedAyatId = MutableStateFlow<Long?>(null)
    private val tafsirSheetOpen = MutableStateFlow(false)
    private val murottalPanelOpen = MutableStateFlow(false)
    private var startAyatNumber: Int? = null
    private var lastVisibleAyatNumber: Int? = null
    private var sessionRecorded = false
    private var tafsirJob: Job? = null

    private val _tafsirUiState = MutableStateFlow<QuranTafsirUiState>(QuranTafsirUiState.Loading)
    val tafsirUiState: StateFlow<QuranTafsirUiState> = _tafsirUiState

    /** The basmalah the tenang surah header renders is Al-Fatihah ayat 1 read straight from Room —
     * the exact official Kemenag string, so the app never carries its own copy of Quran Arabic. Left
     * blank when the dataset has not been prepared; the header then draws no basmalah rather than
     * substituting anything. */
    private val basmalahArabic =
        quranRepository
            .observeVersesBySurah(AL_FATIHAH_SURAH_NUMBER)
            .map { verses -> verses.firstOrNull()?.arabicText.orEmpty() }

    private val roomData =
        combine(
            quranRepository.observeSurahs(),
            quranRepository.observeVersesBySurah(surahNumber),
            settingsRepository.observe(),
            quranRepository.observeBookmarks(),
            basmalahArabic,
        ) { surahs, verses, settings, bookmarks, basmalah ->
            QuranReaderRoomData(surahs, verses, settings, bookmarks, basmalah)
        }

    val uiState: StateFlow<QuranReaderUiState> =
        combine(roomData, selectedAyatId, tafsirSheetOpen) { data, selected, tafsirOpen ->
            buildState(data, selected, tafsirOpen)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            QuranReaderUiState.Loading,
        )

    /** Playback state is its own flow rather than a field of [QuranReaderUiState.Content]: the
     * in-ayah position ticks five times a second, and folding that into the surah state would
     * rebuild every ayat model on each tick. */
    val murottalState: StateFlow<QuranMurottalState> = murottalPlayer.state

    val murottalPanelUiState: StateFlow<QuranMurottalPanelUiState?> =
        combine(
            murottalPanelOpen,
            settingsRepository.observe(),
            audioStore.library,
            audioDownloadManager.progress,
            quranRepository.observeSurahs(),
        ) { panelOpen, settings, library, download, surahs ->
            if (!panelOpen) return@combine null
            val surah = surahs.firstOrNull { it.number == surahNumber } ?: return@combine null
            val player = murottalPlayer.state.value
            // The queue must read from whatever is actually being recited, which is not necessarily
            // the surah this reader is open on.
            val playingSurahName =
                player.surahNumber
                    ?.let { playing -> surahs.firstOrNull { it.number == playing }?.latinName }
                    .orEmpty()
            QuranMurottalPanelUiState(
                surahNumber = surahNumber,
                surahName = surah.latinName,
                ayatCount = surah.ayatCount,
                nextSurahName = surahs.firstOrNull { it.number == surahNumber + 1 }?.latinName,
                speed = settings.murottalSpeed,
                continueAcrossSurah = settings.murottalContinueAcrossSurah,
                keepScreenOn = settings.murottalKeepScreenOn,
                storedAyahCount = library.storedAyahCount(surahNumber),
                storedBytes = library.bytes(surahNumber),
                download = download?.takeIf { it.surahNumber == surahNumber },
                queueDisplayNames =
                    if (playingSurahName.isBlank()) {
                        emptyList()
                    } else {
                        listOf(playingSurahName) + player.queuedSurahNames
                    },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS), null)

    init {
        audioStore.ensureDirectory()
        viewModelScope.launch { audioStore.refresh() }
        // The player owns playback across screens, so persisted murottal preferences are pushed into
        // it here rather than read by it — it has no scope of its own to observe DataStore in.
        viewModelScope.launch {
            settingsRepository.observe().collect { settings ->
                murottalPlayer.continueAcrossSurah = settings.murottalContinueAcrossSurah
                if (murottalPlayer.state.value.speed != settings.murottalSpeed) {
                    murottalPlayer.setSpeed(settings.murottalSpeed)
                }
            }
        }
    }

    /** Tapping an ayah number: play it, then continue through the surah (addendum item 1). */
    fun onPlayAyat(ayat: QuranReaderAyatUiModel) {
        murottalPlayer.play(surahNumber, ayat.ayatNumber)
    }

    fun onPlayFromHere(ayat: QuranReaderAyatUiModel) {
        murottalPlayer.play(surahNumber, ayat.ayatNumber)
        onDismissActionSheet()
    }

    fun onPlaySingleAyat(ayat: QuranReaderAyatUiModel) {
        murottalPlayer.play(surahNumber, ayat.ayatNumber, singleAyahOnly = true)
        onDismissActionSheet()
    }

    fun onRepeatAyat(ayat: QuranReaderAyatUiModel) {
        murottalPlayer.play(surahNumber, ayat.ayatNumber, repeatCount = REPEAT_AYAT_COUNT)
        onDismissActionSheet()
    }

    fun onTogglePlayPause() = murottalPlayer.togglePlayPause()

    fun onSkipToNextAyat() = murottalPlayer.skipToNext()

    fun onSkipToPreviousAyat() = murottalPlayer.skipToPrevious()

    /** Both "Batal" during preparation and "close" on the player bar — either way playback ends. */
    fun onStopPlayback() = murottalPlayer.stop()

    fun onRetryPlayback() {
        val current = murottalPlayer.state.value
        val ayah = current.ayahNumber ?: return
        murottalPlayer.play(current.surahNumber ?: surahNumber, ayah)
    }

    fun onOpenMurottalPanel() {
        murottalPanelOpen.value = true
    }

    fun onDismissMurottalPanel() {
        murottalPanelOpen.value = false
    }

    fun onSelectMurottalSpeed(speed: QuranMurottalSpeed) {
        viewModelScope.launch { settingsRepository.setMurottalSpeed(speed) }
    }

    fun onToggleContinueAcrossSurah(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMurottalContinueAcrossSurah(enabled) }
    }

    fun onToggleKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMurottalKeepScreenOn(enabled) }
    }

    fun onDownloadSurahAudio() {
        val ayatCount = (uiState.value as? QuranReaderUiState.Content)?.ayatCount ?: return
        audioDownloadManager.start(surahNumber, ayatCount)
    }

    fun onCancelSurahAudioDownload() = audioDownloadManager.cancel()

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

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
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
            surahArabicName = surah.arabicName,
            basmalahArabic = data.basmalahArabic,
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
        const val AL_FATIHAH_SURAH_NUMBER = 1
        const val SUBSCRIPTION_TIMEOUT_MILLIS = 5000L

        /** "Ulangi 3×" — the count the design's chip states. */
        const val REPEAT_AYAT_COUNT = 3
        val TAFSIR_STALE_THRESHOLD_MILLIS = TimeUnit.DAYS.toMillis(7)
    }
}

private data class QuranReaderRoomData(
    val surahs: List<QuranSurah>,
    val verses: List<QuranVerse>,
    val settings: QuranReaderSettings,
    val bookmarks: List<QuranBookmark>,
    val basmalahArabic: String,
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
