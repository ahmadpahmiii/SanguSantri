package com.sangusantri.app.domain.model

/** Playback speeds offered by the murottal panel's segmented control (design frame `4c`). */
enum class QuranMurottalSpeed(
    val multiplier: Float,
) {
    SLOW(0.75f),
    NORMAL(1f),
    FAST(1.25f),
}

enum class QuranMurottalStatus {
    IDLE,

    /** Fetching and storing the ayah before it can play — the design's spinner state (`4e`). */
    PREPARING,
    PLAYING,
    PAUSED,

    /** The ayah could not be fetched or decoded; the player bar reports it and offers a retry. */
    ERROR,
}

/**
 * What the murottal player is doing right now, as every surface reads it: the reader's active-ayah
 * treatment, the mini player bar, the mushaf follow-scroll, and the hub's "Sedang diputar" block.
 */
data class QuranMurottalState(
    val status: QuranMurottalStatus = QuranMurottalStatus.IDLE,
    val surahNumber: Int? = null,
    val surahName: String = "",
    val ayahNumber: Int? = null,
    /** The ayah that will play when this one ends, or `null` when this is the end of the queue. */
    val nextAyahNumber: Int? = null,
    /** Position inside the current ayah, `0f..1f` — the 2dp line between Arabic and translation. */
    val positionFraction: Float = 0f,
    val speed: QuranMurottalSpeed = QuranMurottalSpeed.NORMAL,
    /** `true` while the preparing ayah is actually being fetched, as opposed to being already
     * stored — only then does the bar say "Mengunduh · disimpan offline". */
    val isDownloading: Boolean = false,
    /** Remaining plays of the current ayah including this one, from "Ulangi 3×". */
    val repeatRemaining: Int = 1,
    /** Set by "Putar ayat ini saja" — playback stops at the end of this ayah. */
    val singleAyahOnly: Boolean = false,
    /** Latin names of the surahs queued after the current one, for the panel's "Antrean:" line. */
    val queuedSurahNames: List<String> = emptyList(),
) {
    val isActive: Boolean get() = status != QuranMurottalStatus.IDLE

    /** `true` when [ayahNumber] of [surahNumber] is the ayah this state refers to. */
    fun isCurrent(
        surahNumber: Int,
        ayahNumber: Int,
    ): Boolean = this.surahNumber == surahNumber && this.ayahNumber == ayahNumber
}
