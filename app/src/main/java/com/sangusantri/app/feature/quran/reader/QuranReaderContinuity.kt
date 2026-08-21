package com.sangusantri.app.feature.quran.reader

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The little that has to outlive one surah's reader.
 *
 * Reading straight through the Qur'an crosses surah boundaries, and each crossing builds an entirely
 * new reader. Anything the reader keeps to itself is therefore silently discarded mid-read, which the
 * reader experiences as the app interrupting them:
 *
 * * **Chrome visibility.** Mushaf immersion is a decision about how someone wants to read, not about
 *   the surah open when they made it. Screen-local, a hidden title bar popped back into view on the
 *   very swipe that carried the reader onward, shifting the page beneath it.
 * * **Loaded surahs.** A new reader starts with nothing and waits for Room, leaving at least one
 *   frame with no page drawn — on a page turn that reads as a blink. The reader being left behind has
 *   already loaded the surah being entered, because it draws its neighbours' boundary pages, so that
 *   work is kept here instead of thrown away and redone. Room replaces it moments later with the
 *   identical content, invisibly.
 *
 * Deliberately small: a flag, and the surahs either side of the one being read.
 */
@Singleton
class QuranReaderContinuity
@Inject
constructor() {
    private val _chromeVisible = MutableStateFlow(true)
    val chromeVisible: StateFlow<Boolean> = _chromeVisible.asStateFlow()

    fun toggleChrome() {
        _chromeVisible.value = !_chromeVisible.value
    }

    /** Chrome is only ever hidden in mushaf mode; leaving that mode restores the bar. */
    fun showChrome() {
        _chromeVisible.value = true
    }

    private val snapshots =
        object : LinkedHashMap<Int, QuranReaderRoomData>(SNAPSHOT_LIMIT, LOAD_FACTOR, true) {
            override fun removeEldestEntry(eldest: Map.Entry<Int, QuranReaderRoomData>): Boolean =
                size > SNAPSHOT_LIMIT
        }

    @Synchronized
    fun putSnapshot(
        surahNumber: Int,
        data: QuranReaderRoomData,
    ) {
        if (data.verses.isEmpty()) return
        snapshots[surahNumber] = data
    }

    @Synchronized
    fun snapshot(surahNumber: Int): QuranReaderRoomData? = snapshots[surahNumber]

    private companion object {
        const val SNAPSHOT_LIMIT = 3
        const val LOAD_FACTOR = 0.75f
    }
}
