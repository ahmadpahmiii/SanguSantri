package com.sangusantri.app.feature.quran.reader

/**
 * What murottal playback is doing, as one ayat row needs to see it (`4a`/`4e`).
 *
 * Ayat numbers rather than remote ids: the player tracks surah/ayah positions, which is also what
 * the CDN addresses audio by, so translating to a Room id at this boundary would buy nothing.
 */
data class QuranAyatPlaybackState(
    val playingAyatNumber: Int? = null,
    val nextAyatNumber: Int? = null,
    val preparingAyatNumber: Int? = null,
    /** Position inside the playing ayah, `0f..1f`. */
    val positionFraction: Float = 0f,
)
