package com.sangusantri.app.domain.model

/** Cached tafsir for one ayat, fetched on demand by [remoteAyatId] (QUR-FR-013). */
data class QuranTafsir(
    val remoteAyatId: Long,
    val ringkas: String,
    val tahlili: String,
    val cachedAtEpochMillis: Long,
)
