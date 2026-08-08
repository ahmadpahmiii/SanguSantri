package com.sangusantri.app.data.remote.quran.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One `GET /ayat/local/{no_surah}` item, field names exactly as observed from LPMQ Kemenag.
 *
 * [teksLatin] is the API's Latin transliteration (wire name `teks`). It is intentionally decoded
 * here (dropping it silently would make an envelope-shape regression invisible) but MUST NOT be
 * mapped into [com.sangusantri.app.data.local.entity.QuranVerseEntity] or any domain/UI model
 * (QUR-FR-009, `CLAUDE.md` Content safety).
 */
@Serializable
data class QuranAyatDto(
    val id: Long,
    val surah: Int,
    val ayat: Int,
    val juz: Int,
    val halaman: Int,
    @SerialName("teks_msi_usmani") val teksMsiUsmani: String,
    @SerialName("teks_gundul") val teksGundul: String,
    @SerialName("teks") val teksLatin: String,
    val keterangan: String,
    val terjemah: String,
    @SerialName("no_foot") val noFoot: String,
    @SerialName("teks_foot") val teksFoot: String,
)
