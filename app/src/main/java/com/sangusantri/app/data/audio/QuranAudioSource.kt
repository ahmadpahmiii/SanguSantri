package com.sangusantri.app.data.audio

/**
 * Where one ayah's recitation lives, remotely and locally.
 *
 * myquran's CDN addresses per-ayah audio purely positionally — `audio/ayah/089004.mp3` is Al-Fajr
 * ayat 4 — so the app derives every URL arithmetically and never calls an endpoint just to be told
 * a link it can already compute. `/quran/{surah}` does return an `audio_url` per ayah, but reading
 * it would cost a request per surah to learn nothing new.
 *
 * The service publishes exactly one recitation and documents no reciter anywhere in its OpenAPI
 * description, so [RECITER_NAME] is the product owner's own attribution, recorded here as the single
 * place to correct it. It is never presented as something the API asserted.
 */
object QuranAudioSource {
    const val RECITER_NAME = "Syaikh Misyari Rasyid Al-'Afasi"

    /** Widest single ayah on the CDN is Al-Baqarah 282 at ~2.3 MB; this leaves generous headroom
     * while still refusing a response that is clearly not one ayah of recitation. */
    const val MAX_AYAH_AUDIO_BYTES = 5L * 1024 * 1024

    fun ayahAudioUrl(
        surahNumber: Int,
        ayahNumber: Int,
    ): String = "$CDN_BASE/audio/ayah/${positionalKey(surahNumber, ayahNumber)}$MP3_SUFFIX"

    /** Local file name for one ayah — the same positional key as the remote path, so a directory
     * listing is directly readable as surah/ayah coverage without a parallel index. */
    fun ayahFileName(
        surahNumber: Int,
        ayahNumber: Int,
    ): String = "${positionalKey(surahNumber, ayahNumber)}$MP3_SUFFIX"

    /** `89`, `4` → `089004`. */
    private fun positionalKey(
        surahNumber: Int,
        ayahNumber: Int,
    ): String = "%03d%03d".format(surahNumber, ayahNumber)

    /** Parses a stored file name back to its surah/ayah pair, or `null` when the name is not one of
     * ours — a stray file in the directory must not be counted as downloaded audio. */
    fun parseFileName(fileName: String): Pair<Int, Int>? =
        fileName
            .removeSuffix(MP3_SUFFIX)
            .takeIf { it.length == POSITIONAL_KEY_LENGTH && it != fileName }
            ?.let { key ->
                val surah = key.take(3).toIntOrNull()?.takeIf { it in 1..TOTAL_SURAHS }
                val ayah = key.drop(3).toIntOrNull()?.takeIf { it >= 1 }
                if (surah == null || ayah == null) null else surah to ayah
            }

    private const val CDN_BASE = "https://cdn.myquran.com"
    private const val MP3_SUFFIX = ".mp3"
    private const val POSITIONAL_KEY_LENGTH = 6
    private const val TOTAL_SURAHS = 114
}
