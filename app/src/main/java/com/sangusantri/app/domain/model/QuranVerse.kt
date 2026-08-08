package com.sangusantri.app.domain.model

/**
 * One official LPMQ Kemenag ayat record, keyed by the stable local `(surahNumber, ayatNumber)`
 * identity. [remoteId] is the API's own ayat identifier, used only as the tafsir lookup key
 * (`docs/product/QURAN_PRD.md` QUR-FR-013) — never as the local reading identity, since bookmarks
 * and the last-read position must survive a refresh even if remote ids were to change.
 *
 * The API's `teks` Latin transliteration field is deliberately not represented here — it must
 * never be persisted or displayed (QUR-FR-009, `CLAUDE.md` Content safety).
 */
data class QuranVerse(
    val surahNumber: Int,
    val ayatNumber: Int,
    val remoteId: Long,
    val juz: Int,
    val page: Int,
    val arabicText: String,
    val arabicTextNoHarakat: String,
    val translation: String,
    val note: String,
    val footnoteNumber: String,
    val footnoteText: String,
)
