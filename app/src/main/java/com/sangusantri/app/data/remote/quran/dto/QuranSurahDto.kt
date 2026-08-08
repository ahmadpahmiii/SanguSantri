package com.sangusantri.app.data.remote.quran.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One `GET /surah/local/{first}/{count}` item, field names exactly as observed from LPMQ Kemenag. */
@Serializable
data class QuranSurahDto(
    val id: Int,
    val nama: String,
    val arabic: String,
    val arti: String,
    @SerialName("kategori_ar") val kategoriAr: String,
    val kategori: String,
    @SerialName("jmlAyat") val jumlahAyat: Int,
)
