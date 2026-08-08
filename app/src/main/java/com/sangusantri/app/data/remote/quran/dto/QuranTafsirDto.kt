package com.sangusantri.app.data.remote.quran.dto

import kotlinx.serialization.Serializable

/** One `GET /ayat/local/tafsir/{ayat_id}` item — [teks] here is the concise "Ringkas" tafsir, a
 * different field to [QuranAyatDto.teks] (that one is a Latin transliteration; this one is
 * Indonesian tafsir text, both named `teks` by the source API). */
@Serializable
data class QuranTafsirDto(
    val id: Long,
    val surah: Int,
    val ayat: Int,
    val juz: Int,
    val teks: String,
    val tahlili: String,
)
