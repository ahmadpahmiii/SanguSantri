package com.sangusantri.app.data.audio

import com.sangusantri.app.domain.model.QuranAudioDownloadProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs the one surah download the design allows at a time, outside any ViewModel.
 *
 * The download block appears in both the murottal panel and the hub's surah rows, and a download
 * must not die because the screen that started it was left — so the running job lives here rather
 * than in a `viewModelScope`.
 */
@Singleton
class QuranAudioDownloadManager
@Inject
constructor(
    private val downloader: QuranAudioDownloader,
    private val store: QuranAudioStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _progress = MutableStateFlow<QuranAudioDownloadProgress?>(null)

    /** Non-null only while a surah download is actually running. */
    val progress: StateFlow<QuranAudioDownloadProgress?> = _progress.asStateFlow()

    private var job: Job? = null

    fun start(
        surahNumber: Int,
        ayatCount: Int,
    ) {
        if (job?.isActive == true) return
        job =
            scope.launch {
                downloader
                    .downloadSurah(surahNumber, ayatCount)
                    .collect { _progress.value = it }
                _progress.value = null
                store.refresh()
            }
    }

    /** "Batalkan" — whatever already landed stays on disk and stays playable. */
    fun cancel() {
        job?.cancel()
        job = null
        _progress.value = null
        scope.launch { store.refresh() }
    }
}
